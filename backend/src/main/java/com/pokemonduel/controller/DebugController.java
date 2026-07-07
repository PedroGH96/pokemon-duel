package com.pokemonduel.controller;

import com.pokemonduel.model.*;
import com.pokemonduel.model.PokemonFigure.FigureState;
import com.pokemonduel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Endpoints de depuração — permitem testar o jogo sem precisar de dois jogadores.
 *
 * ATENÇÃO: estes endpoints NÃO devem existir em produção.
 * Eles criam partidas com um bot simples (IA aleatória) para testar
 * o tabuleiro, movimentação, batalhas e a roleta sem precisar de duas instâncias.
 *
 * Endpoints:
 *  POST /debug/partida          → cria partida Player vs. Bot com decks padrão
 *  POST /debug/partida/{id}/bot → executa UM turno do bot (movimento aleatório)
 *  POST /debug/batalha          → testa batalha entre dois Pokémon específicos
 *  GET  /debug/roleta/{id}      → simula N giros da roleta de um Pokémon
 */
@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired private RoomService        roomService;
    @Autowired private GameService        gameService;
    @Autowired private BattleService      battleService;
    @Autowired private PokemonCatalogService catalog;

    // ── Criar partida de teste ────────────────────────────────────────────────

    /**
     * Cria uma partida Player vs. Bot sem passar pelo Lobby.
     * Corpo (opcional): { "deckP1": ["pikachu","charizard",...], "deckP2": [...] }
     * Se não informado, usa os starters padrão para ambos.
     *
     * Retorna o estado inicial da partida + os IDs dos jogadores.
     */
    @PostMapping("/partida")
    public ResponseEntity<Map<String, Object>> criarPartidaTeste(
            @RequestBody(required = false) Map<String, Object> body) {

        List<String> deckP1 = extractDeck(body, "deckP1");
        List<String> deckP2 = extractDeck(body, "deckP2");

        // IDs determinísticos para teste fácil
        String p1 = "debug-player-1";
        String p2 = "debug-bot-2";

        List<PokemonFigure> figs1 = buildFigures(deckP1, 1);
        List<PokemonFigure> figs2 = buildFigures(deckP2, 2);

        String matchId = UUID.randomUUID().toString();
        // Usa sala fictícia "debug"
        GameState gs = GameState.create(matchId, "debug-room", p1, p2, figs1, figs2);

        // Injeta diretamente no RoomService
        roomService.injectMatchForDebug(matchId, gs);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("matchId",   matchId);
        resp.put("player1Id", p1);
        resp.put("player2Id", p2);
        resp.put("dica",      "Use player1Id nas chamadas /partidas/{id}/entrar e /mover. " +
                              "Use POST /debug/partida/{id}/bot para o turno do bot.");
        resp.put("estado",    roomService.matchSnapshot(gs));
        return ResponseEntity.ok(resp);
    }

    // ── Turno do bot ──────────────────────────────────────────────────────────

    /**
     * Executa UM turno do bot (jogador 2) com IA aleatória simples:
     * 1. Se tiver figura no banco → entra no tabuleiro
     * 2. Senão → move a primeira figura ativa para um nó aleatório alcançável
     *
     * Isso permite testar batalhas e movimentação sem precisar de dois jogadores.
     */
    @PostMapping("/partida/{id}/bot")
    public ResponseEntity<?> turnoBot(@PathVariable String id) {
        Optional<GameState> optGs = roomService.findMatch(id);
        if (optGs.isEmpty()) return ResponseEntity.notFound().build();

        GameState gs = optGs.get();
        if (gs.getStatus() != GameState.Status.ACTIVE)
            return ResponseEntity.badRequest().body("Partida não está ativa");

        if (gs.getCurrentTurn() != 2)
            return ResponseEntity.badRequest().body("Não é o turno do bot (turno=" + gs.getCurrentTurn() + ")");

        String botId = gs.getPlayer2Id();
        GameService.MoveResult result;

        String pendingFigureId = gs.getPendingActionFigureId();
        if (pendingFigureId != null) {
            // O bot já entrou/moveu uma figura neste turno e sobrou PM — só pode
            // continuar movendo ESSA figura (com o PM restante) ou passar a vez.
            Optional<PokemonFigure> optPending = gs.findFigure(pendingFigureId);
            PokemonFigure fig = optPending.orElse(null);
            Set<Integer> reachable = Collections.emptySet();
            if (fig != null) {
                Set<Integer> friendly = gs.activeNodesOf(2);
                friendly.remove(fig.getNodeId());
                reachable = gs.getBoard().reachableNodes(
                        fig.getNodeId(), gs.getPendingActionBudget(), friendly, gs.activeNodesOf(1));
            }

            if (fig == null || reachable.isEmpty()) {
                result = gameService.passarVez(gs, botId);
            } else {
                final Set<Integer> finalReachable = reachable;
                Set<Integer> enemyNodes = gs.activeNodesOf(1);
                Optional<Integer> attackTarget = finalReachable.stream()
                        .filter(enemyNodes::contains).findFirst();
                int dest = attackTarget.orElseGet(() ->
                        finalReachable.stream().toList().get(new Random().nextInt(finalReachable.size())));
                result = gameService.moveFigure(gs, fig.getFigureId(), dest, botId);
            }
        } else if (gs.figuresOf(2).stream().anyMatch(f -> f.getState() == FigureState.BENCH)) {
            // 1. Tentar entrar com figura do banco
            PokemonFigure bench = gs.figuresOf(2).stream()
                    .filter(f -> f.getState() == FigureState.BENCH)
                    .findFirst().orElseThrow();
            result = gameService.enterBoard(gs, bench.getFigureId(), botId);
        } else {
            // 2. Mover uma figura ativa aleatoriamente
            List<PokemonFigure> active = gs.figuresOf(2).stream()
                    .filter(f -> f.getState() == FigureState.ACTIVE && f.canMove())
                    .toList();

            if (active.isEmpty())
                return ResponseEntity.badRequest().body("Bot sem movimentos disponíveis");

            // Escolhe figura aleatória
            PokemonFigure fig = active.get(new Random().nextInt(active.size()));
            Set<Integer> friendly = gs.activeNodesOf(2);
            friendly.remove(fig.getNodeId());
            Set<Integer> reachable = gs.getBoard().reachableNodes(
                    fig.getNodeId(), fig.getEffectivePm(), friendly, gs.activeNodesOf(1));

            if (reachable.isEmpty())
                return ResponseEntity.badRequest().body("Figura do bot sem alcance");

            // Prefere nós com inimigos (ataque) — IA simples
            Set<Integer> enemyNodes = gs.activeNodesOf(1);
            Optional<Integer> attackTarget = reachable.stream()
                    .filter(enemyNodes::contains).findFirst();
            int dest = attackTarget.orElseGet(() ->
                    reachable.stream().toList().get(new Random().nextInt(reachable.size())));

            result = gameService.moveFigure(gs, fig.getFigureId(), dest, botId);
        }

        if (!result.success)
            return ResponseEntity.badRequest().body("Bot falhou: " + result.error);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success",   true);
        resp.put("matchOver", result.matchOver);
        resp.put("estado",    roomService.matchSnapshot(gs));
        if (result.battle != null) {
            resp.put("battle", formatBattleForClient(result.battle,
                    result.attackerFigureId, result.defenderFigureId));
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Formata uma batalha no MESMO schema usado por MatchController
     * (chaves em inglês + figureIds) — é o formato que o BattleOverlay/
     * BattleWheel do cliente Godot espera. Usado pelo turno automático do
     * bot (Modo Solo), que precisa acionar a mesma tela de transição de
     * batalha que o modo multiplayer usa.
     */
    private Map<String, Object> formatBattleForClient(BattleResult battle,
                                                        String attackerFigureId,
                                                        String defenderFigureId) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("outcome",          battle.getOutcome().name());
        b.put("attackerFigureId", attackerFigureId);
        b.put("defenderFigureId", defenderFigureId);
        b.put("attackerMoveName", battle.getAttackerMove().getName());
        b.put("attackerColor",    battle.getAttackerMove().getColor().name());
        b.put("attackerDamage",   battle.getAttackerMove().getDamage());
        b.put("attackerAngle",    battle.getAttackerWheelAngle());
        b.put("defenderMoveName", battle.getDefenderMove().getName());
        b.put("defenderColor",    battle.getDefenderMove().getColor().name());
        b.put("defenderDamage",   battle.getDefenderMove().getDamage());
        b.put("defenderAngle",    battle.getDefenderWheelAngle());
        b.put("statusApplied",    battle.getStatusApplied() != null
                ? battle.getStatusApplied().name() : "NONE");
        b.put("statusTarget",     battle.getStatusTarget());
        b.put("destinyBond",      battle.isDestinyBondTriggered());
        return b;
    }

    // ── Testar batalha específica ─────────────────────────────────────────────

    /**
     * Simula N batalhas entre dois Pokémon e retorna estatísticas.
     * Corpo: { "pokemon1": "pikachu", "pokemon2": "charizard", "vezes": 10 }
     */
    @PostMapping("/batalha")
    public ResponseEntity<?> testarBatalha(@RequestBody Map<String, Object> body) {
        String id1 = (String) body.getOrDefault("pokemon1", "pikachu");
        String id2 = (String) body.getOrDefault("pokemon2", "charizard");
        int vezes  = ((Number) body.getOrDefault("vezes", 1)).intValue();
        vezes = Math.min(vezes, 100); // máximo 100 batalhas

        Pokemon p1 = catalog.get(id1);
        Pokemon p2 = catalog.get(id2);

        if (p1 == null) return ResponseEntity.badRequest().body("Pokémon não encontrado: " + id1);
        if (p2 == null) return ResponseEntity.badRequest().body("Pokémon não encontrado: " + id2);

        List<Map<String, Object>> resultados = new ArrayList<>();
        int vitorias1 = 0, vitorias2 = 0, empates = 0;

        for (int i = 0; i < vezes; i++) {
            PokemonFigure f1 = new PokemonFigure(UUID.randomUUID().toString(), p1, 1);
            PokemonFigure f2 = new PokemonFigure(UUID.randomUUID().toString(), p2, 2);
            BattleResult battle = battleService.resolve(f1, f2);

            Map<String, Object> r = formatBattle(battle);
            r.put("rodada", i + 1);
            resultados.add(r);

            switch (battle.getOutcome()) {
                case ATTACKER_WINS, COMPARE_ATTACKER_WINS -> vitorias1++;
                case DEFENDER_WINS, COMPARE_DEFENDER_WINS -> vitorias2++;
                default -> empates++;
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("pokemon1",  p1.getName() + " (ID-" + p1.getDexId() + ")");
        resp.put("pokemon2",  p2.getName() + " (ID-" + p2.getDexId() + ")");
        resp.put("vitorias1", vitorias1);
        resp.put("vitorias2", vitorias2);
        resp.put("empates",   empates);
        if (vezes == 1) resp.put("batalha", resultados.get(0));
        else            resp.put("batalhas", resultados);
        return ResponseEntity.ok(resp);
    }

    // ── Testar roleta ─────────────────────────────────────────────────────────

    /**
     * Simula N giros da roleta de um Pokémon e retorna frequências.
     * GET /debug/roleta/{pokemonId}?vezes=100
     */
    @GetMapping("/roleta/{pokemonId}")
    public ResponseEntity<?> testarRoleta(
            @PathVariable String pokemonId,
            @RequestParam(defaultValue = "20") int vezes) {

        Pokemon poke = catalog.get(pokemonId);
        if (poke == null) return ResponseEntity.notFound().build();
        vezes = Math.min(vezes, 1000);

        Map<String, Integer> freq = new LinkedHashMap<>();
        List<String> giros = new ArrayList<>();
        for (int i = 0; i < vezes; i++) {
            Move m = poke.spin();
            String key = m.getName() + " (" + m.getColor() + ")";
            freq.merge(key, 1, Integer::sum);
            giros.add(key);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("pokemon",    poke.getName() + " (ID-" + poke.getDexId() + ")");
        resp.put("wheelTotal", poke.getWheelTotal());
        resp.put("giros",      vezes);
        resp.put("frequencia", freq);
        resp.put("sequencia",  giros);

        List<Map<String, Object>> movesInfo = new ArrayList<>();
        int total = poke.getWheelTotal();
        for (Move m : poke.getMoves()) {
            Map<String, Object> mi = new LinkedHashMap<>();
            mi.put("nome",           m.getName());
            mi.put("cor",            m.getColor());
            mi.put("peso",           m.getPercentage());
            mi.put("probabilidade",  String.format("%.1f%%", m.getPercentage() * 100.0 / total));
            mi.put("dano",           m.getDamage());
            mi.put("efeito",         m.getStatusEffect());
            movesInfo.add(mi);
        }
        resp.put("roleta", movesInfo);
        return ResponseEntity.ok(resp);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<String> extractDeck(Map<String, Object> body, String key) {
        if (body != null && body.containsKey(key)) {
            return (List<String>) body.get(key);
        }
        return PokemonCatalogService.STARTER_IDS;
    }

    private List<PokemonFigure> buildFigures(List<String> ids, int player) {
        List<PokemonFigure> figs = new ArrayList<>();
        for (String id : ids) {
            Pokemon p = catalog.get(id);
            if (p != null) figs.add(new PokemonFigure(UUID.randomUUID().toString(), p, player));
        }
        return figs;
    }

    private Map<String, Object> formatBattle(BattleResult battle) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("outcome",          battle.getOutcome().name());
        b.put("atacante",         battle.getAttackerMove().getName()
                                  + " (" + battle.getAttackerMove().getColor() + ")"
                                  + (battle.getAttackerMove().getDamage() > 0
                                     ? " " + battle.getAttackerMove().getDamage() + " dano" : ""));
        b.put("defensor",         battle.getDefenderMove().getName()
                                  + " (" + battle.getDefenderMove().getColor() + ")"
                                  + (battle.getDefenderMove().getDamage() > 0
                                     ? " " + battle.getDefenderMove().getDamage() + " dano" : ""));
        b.put("anguloAtacante",   Math.round(battle.getAttackerWheelAngle()));
        b.put("anguloDefensor",   Math.round(battle.getDefenderWheelAngle()));
        b.put("statusAplicado",   battle.getStatusApplied() != null
                                  ? battle.getStatusApplied().name() : "NONE");
        b.put("destinyBond",      battle.isDestinyBondTriggered());
        return b;
    }
}
