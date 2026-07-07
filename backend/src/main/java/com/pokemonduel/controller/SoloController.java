package com.pokemonduel.controller;

import com.pokemonduel.model.*;
import com.pokemonduel.model.PokemonFigure.FigureState;
import com.pokemonduel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Endpoints do Modo Solo — campanha de 1 jogador contra NPCs.
 *
 *  GET  /solo/niveis              → metadados dos 6 níveis (5 inimigos + 1 chefe cada)
 *  POST /solo/partida             → cria uma partida contra um oponente específico do nível
 *
 * Simplificação do projeto: todos os inimigos e chefes de todos os níveis
 * usam o mesmo deck fixo: Pokémon com dexId 2–7
 * (bulbasaur, ivysaur, venusaur, charmander, charmeleon, charizard).
 *
 * O turno do bot é executado via POST /debug/partida/{id}/bot,
 * que já funciona com qualquer matchId injetado no RoomService,
 * sem precisar duplicar a lógica de IA aqui.
 */
@RestController
@RequestMapping("/solo")
@CrossOrigin(origins = "*")
public class SoloController {

    @Autowired private RoomService        roomService;
    @Autowired private PokemonCatalogService catalog;
    @Autowired private PlayerService      playerService;

    // Deck fixo de todos os NPCs do modo solo (dexId 2–7)
    private static final List<String> NPC_DECK = List.of(
        "bulbasaur", "ivysaur", "venusaur",
        "charmander", "charmeleon", "charizard"
    );

    // ── GET /solo/niveis ────────────────────────────────────────────────────────

    /**
     * Retorna a estrutura dos 6 níveis do Modo Solo.
     * Cada nível tem 5 inimigos (slots 0–4) e 1 chefe (slot 5).
     *
     * O campo "avatar" é o nome do arquivo PNG (sem caminho)
     * que o Godot carrega de res://assets/sprites/npc/.
     */
    @GetMapping("/niveis")
    public ResponseEntity<List<Map<String, Object>>> niveis() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int lvl = 1; lvl <= 6; lvl++) {
            Map<String, Object> nivel = new LinkedHashMap<>();
            nivel.put("nivel", lvl);
            nivel.put("nome", "Nível " + lvl);

            List<Map<String, Object>> oponentes = new ArrayList<>();

            // 5 inimigos (avatar: inimigo_1.png .. inimigo_5.png — rotativos)
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> oponente = new LinkedHashMap<>();
                oponente.put("slot", i - 1);
                oponente.put("tipo", "INIMIGO");
                oponente.put("nome", "Inimigo " + i);
                oponente.put("avatar", "inimigo_" + i + ".png");
                oponentes.add(oponente);
            }

            // 1 chefe (avatar: chefe_1.png .. chefe_6.png — específico por nível)
            Map<String, Object> chefe = new LinkedHashMap<>();
            chefe.put("slot", 5);
            chefe.put("tipo", "CHEFE");
            chefe.put("nome", "Chefe do Nível " + lvl);
            chefe.put("avatar", "chefe_" + lvl + ".png");
            oponentes.add(chefe);

            nivel.put("oponentes", oponentes);
            result.add(nivel);
        }

        return ResponseEntity.ok(result);
    }

    // ── POST /solo/partida ──────────────────────────────────────────────────────

    /**
     * Cria uma partida Player vs. NPC para o Modo Solo.
     *
     * Corpo esperado:
     * {
     *   "nivel":         1–6  (obrigatório),
     *   "slot":          0–5  (0–4=inimigo, 5=chefe; obrigatório),
     *   "jogadorId":     "uuid" (obrigatório),
     *   "deckJogador":   ["bulbasaur",...] (opcional — usa STARTER_IDS se omitido)
     * }
     *
     * Retorna o mesmo formato que /debug/partida:
     * { matchId, player1Id (jogador), player2Id (npc-solo), estado {...} }
     */
    @PostMapping("/partida")
    public ResponseEntity<Map<String, Object>> criarPartida(
            @RequestBody Map<String, Object> body) {

        int nivel  = ((Number) body.getOrDefault("nivel", 1)).intValue();
        int slot   = ((Number) body.getOrDefault("slot",  0)).intValue();
        String jogadorId = (String) body.getOrDefault("jogadorId", "solo-player-1");

        @SuppressWarnings("unchecked")
        List<String> deckJogador = body.containsKey("deckJogador")
                ? (List<String>) body.get("deckJogador")
                : PokemonCatalogService.STARTER_IDS;

        // Valida nível e slot
        if (nivel < 1 || nivel > 6)
            return ResponseEntity.badRequest().build();
        if (slot < 0 || slot > 5)
            return ResponseEntity.badRequest().build();

        // Monta nome do NPC para identificação
        boolean ehChefe = slot == 5;
        String npcNome  = ehChefe ? "Chefe N" + nivel : "Inimigo " + (slot + 1) + " N" + nivel;
        String npcId    = "npc-solo-" + UUID.randomUUID().toString().substring(0, 8);

        // Constrói figuras (figuras do jogador entram como shiny se ele já
        // tiver aberto essa versão em uma caixa de recompensa)
        List<PokemonFigure> figsJogador = buildFigures(deckJogador, 1, jogadorId);
        List<PokemonFigure> figsNpc     = buildFigures(NPC_DECK, 2, null);

        String matchId = UUID.randomUUID().toString();
        GameState gs = GameState.create(matchId, "solo-room-" + UUID.randomUUID().toString().substring(0,8),
                jogadorId, npcId, figsJogador, figsNpc);

        roomService.injectMatchForDebug(matchId, gs);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("matchId",    matchId);
        resp.put("player1Id",  jogadorId);
        resp.put("player2Id",  npcId);
        resp.put("npcNome",    npcNome);
        resp.put("ehChefe",    ehChefe);
        resp.put("nivel",      nivel);
        resp.put("slot",       slot);
        resp.put("dicaBot",    "Use POST /debug/partida/" + matchId + "/bot para o turno do NPC.");
        resp.put("estado",     roomService.matchSnapshot(gs));
        return ResponseEntity.ok(resp);
    }

    // ── Recompensa (caixa) ───────────────────────────────────────────────────────

    /**
     * Abre uma caixa de recompensa para o jogador. Chamado pelo cliente Godot
     * ao concluir um nível do Modo Solo (ou seja, ao vencer o slot 5 / chefe).
     *
     * Corpo esperado: { "jogadorId": "uuid", "nivel": 1 }
     * Retorna: { pokemonId, pokemonName, shiny, novo }
     */
    @PostMapping("/recompensa")
    public ResponseEntity<Map<String, Object>> abrirRecompensa(
            @RequestBody Map<String, Object> body) {

        String jogadorId = (String) body.getOrDefault("jogadorId", "solo-player-1");

        PlayerService.RewardResult reward = playerService.openRewardBox(jogadorId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("pokemonId",   reward.pokemonId);
        resp.put("pokemonName", reward.pokemonName);
        resp.put("shiny",       reward.shiny);
        resp.put("novo",        reward.isNew);
        return ResponseEntity.ok(resp);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private List<PokemonFigure> buildFigures(List<String> ids, int player, String ownerPlayerId) {
        List<PokemonFigure> figs = new ArrayList<>();
        for (String id : ids) {
            Pokemon p = catalog.get(id);
            if (p == null) continue;
            boolean shiny = ownerPlayerId != null && playerService.hasShiny(ownerPlayerId, id);
            figs.add(new PokemonFigure(UUID.randomUUID().toString(), p, player, shiny));
        }
        return figs;
    }
}
