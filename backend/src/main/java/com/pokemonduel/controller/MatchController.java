package com.pokemonduel.controller;

import com.pokemonduel.model.BattleResult;
import com.pokemonduel.model.GameState;
import com.pokemonduel.model.PokemonFigure;
import com.pokemonduel.model.PokemonFigure.FigureState;
import com.pokemonduel.service.BattleService;
import com.pokemonduel.service.GameService;
import com.pokemonduel.service.GameService.MoveResult;
import com.pokemonduel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Endpoints de partida.
 *
 *  GET  /partidas/{id}/estado        → snapshot do estado atual (polling Godot)
 *  POST /partidas/{id}/entrar        → entra com figura do banco no tabuleiro
 *  POST /partidas/{id}/mover         → move figura ativa para nó destino
 *  POST /partidas/{id}/batalhar      → força batalha manual (usado pelo Godot
 *                                      quando a figura já está no nó inimigo)
 *  GET  /partidas/{id}/movimentos    → nós alcançáveis por uma figura
 *  GET  /partidas/{id}/resultado     → resultado final da partida
 */
@RestController
@RequestMapping("/partidas")
@CrossOrigin(origins = "*")
public class MatchController {

    @Autowired private RoomService   roomService;
    @Autowired private GameService   gameService;
    @Autowired private BattleService battleService;

    // ── Estado (polling) ──────────────────────────────────────────────────────

    @GetMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> estado(@PathVariable String id) {
        return roomService.findMatch(id)
                .map(gs -> ResponseEntity.ok(roomService.matchSnapshot(gs)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Entrar no tabuleiro ───────────────────────────────────────────────────

    /**
     * Corpo esperado:
     * { "figuraId": "uuid-da-figura", "jogadorId": "uuid-do-jogador" }
     */
    /**
     * Corpo esperado:
     * { "figuraId": "uuid", "jogadorId": "uuid", "nodeId": 21 }
     * "nodeId" é o nó ENTRY (canto) que o jogador clicou — precisa ser dono
     * dele e estar livre. Se omitido (compatibilidade), o servidor escolhe
     * automaticamente a primeira entrada livre do jogador.
     */
    @PostMapping("/{id}/entrar")
    public ResponseEntity<?> entrar(@PathVariable String id,
                                     @RequestBody Map<String, Object> body) {
        Optional<GameState> optGs = roomService.findMatch(id);
        if (optGs.isEmpty()) return ResponseEntity.notFound().build();

        String figureId = body.get("figuraId") == null ? null : body.get("figuraId").toString();
        String playerId = body.get("jogadorId") == null ? null : body.get("jogadorId").toString();
        if (figureId == null || playerId == null)
            return ResponseEntity.badRequest().body("figuraId e jogadorId são obrigatórios");

        int nodeId = -1;
        if (body.get("nodeId") != null) {
            nodeId = ((Number) body.get("nodeId")).intValue();
        }

        MoveResult result = gameService.enterBoard(optGs.get(), figureId, playerId, nodeId);
        return buildMoveResponse(result, optGs.get());
    }

    // ── Passar (encerrar turno sem usar o PM restante) ─────────────────────────

    /**
     * Corpo esperado: { "jogadorId": "uuid" }
     * Só é aceito quando existe uma ação pendente (figura que entrou/moveu e
     * ainda tinha PM sobrando neste turno, mas o jogador optou por não usar).
     */
    @PostMapping("/{id}/passar")
    public ResponseEntity<?> passar(@PathVariable String id,
                                     @RequestBody Map<String, String> body) {
        Optional<GameState> optGs = roomService.findMatch(id);
        if (optGs.isEmpty()) return ResponseEntity.notFound().build();

        String playerId = body.get("jogadorId");
        if (playerId == null)
            return ResponseEntity.badRequest().body("jogadorId é obrigatório");

        MoveResult result = gameService.passarVez(optGs.get(), playerId);
        return buildMoveResponse(result, optGs.get());
    }

    // ── Mover figura ──────────────────────────────────────────────────────────

    /**
     * Corpo esperado:
     * { "figuraId": "uuid", "destino": { "nodeId": 7 }, "jogadorId": "uuid" }
     */
    @PostMapping("/{id}/mover")
    public ResponseEntity<?> mover(@PathVariable String id,
                                    @RequestBody Map<String, Object> body) {
        Optional<GameState> optGs = roomService.findMatch(id);
        if (optGs.isEmpty()) return ResponseEntity.notFound().build();

        String figureId = (String) body.get("figuraId");
        String playerId = (String) body.get("jogadorId");

        @SuppressWarnings("unchecked")
        Map<String, Object> destMap = (Map<String, Object>) body.get("destino");
        if (figureId == null || playerId == null || destMap == null)
            return ResponseEntity.badRequest().body("figuraId, jogadorId e destino são obrigatórios");

        int nodeId = ((Number) destMap.get("nodeId")).intValue();

        MoveResult result = gameService.moveFigure(optGs.get(), figureId, nodeId, playerId);
        return buildMoveResponse(result, optGs.get());
    }

    // ── Movimentos disponíveis ────────────────────────────────────────────────

    /**
     * Retorna os nós alcançáveis por uma figura.
     * Usado pelo Godot para destacar os nós clicáveis.
     *
     * Query param: ?figuraId=uuid&jogadorId=uuid
     */
    @GetMapping("/{id}/movimentos")
    public ResponseEntity<?> movimentos(@PathVariable String id,
                                         @RequestParam String figuraId,
                                         @RequestParam String jogadorId) {
        Optional<GameState> optGs = roomService.findMatch(id);
        if (optGs.isEmpty()) return ResponseEntity.notFound().build();

        GameState gs = optGs.get();
        int player = jogadorId.equals(gs.getPlayer1Id()) ? 1 : 2;

        Optional<PokemonFigure> optFig = gs.findFigure(figuraId);
        if (optFig.isEmpty()) return ResponseEntity.badRequest().body("Figura não encontrada");

        PokemonFigure fig = optFig.get();
        if (fig.getState() != FigureState.ACTIVE)
            return ResponseEntity.badRequest().body("Figura não está em campo");

        int budget = fig.getEffectivePm();
        if (figuraId.equals(gs.getPendingActionFigureId())) {
            budget = gs.getPendingActionBudget();
        }

        Set<Integer> friendly = gs.activeNodesOf(player);
        friendly.remove(fig.getNodeId());
        int opponent = player == 1 ? 2 : 1;
        Set<Integer> enemyNodes = gs.activeNodesOf(opponent);
        Set<Integer> reachable = gs.getBoard().reachableNodes(
                fig.getNodeId(), budget, friendly, enemyNodes);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("figuraId", figuraId);
        resp.put("nodeAtual", fig.getNodeId());
        resp.put("pm", budget);
        resp.put("nodosAlcancaveis", reachable);
        return ResponseEntity.ok(resp);
    }

    // ── Resultado final ───────────────────────────────────────────────────────

    @GetMapping("/{id}/resultado")
    public ResponseEntity<?> resultado(@PathVariable String id) {
        Optional<GameState> optGs = roomService.findMatch(id);
        if (optGs.isEmpty()) return ResponseEntity.notFound().build();

        GameState gs = optGs.get();
        if (gs.getStatus() != GameState.Status.FINISHED)
            return ResponseEntity.badRequest().body("Partida ainda em andamento");

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("matchId",   gs.getMatchId());
        resp.put("winnerId",  gs.getWinnerId());
        resp.put("winReason", gs.getWinReason());
        resp.put("player1Id", gs.getPlayer1Id());
        resp.put("player2Id", gs.getPlayer2Id());
        resp.put("turnNumber", gs.getTurnNumber());
        return ResponseEntity.ok(resp);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<?> buildMoveResponse(MoveResult result, GameState gs) {
        if (!result.success)
            return ResponseEntity.badRequest().body(result.error);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success",    true);
        resp.put("matchOver",  result.matchOver);
        resp.put("estado",     roomService.matchSnapshot(gs));

        if (result.battle != null) {
            Map<String, Object> battleMap = new LinkedHashMap<>();
            battleMap.put("outcome",          result.battle.getOutcome().name());
            battleMap.put("attackerFigureId", result.attackerFigureId);
            battleMap.put("defenderFigureId", result.defenderFigureId);
            battleMap.put("attackerMoveName", result.battle.getAttackerMove().getName());
            battleMap.put("attackerColor",    result.battle.getAttackerMove().getColor().name());
            battleMap.put("attackerDamage",   result.battle.getAttackerMove().getDamage());
            battleMap.put("attackerAngle",    result.battle.getAttackerWheelAngle());
            battleMap.put("defenderMoveName", result.battle.getDefenderMove().getName());
            battleMap.put("defenderColor",    result.battle.getDefenderMove().getColor().name());
            battleMap.put("defenderDamage",   result.battle.getDefenderMove().getDamage());
            battleMap.put("defenderAngle",    result.battle.getDefenderWheelAngle());
            battleMap.put("statusApplied",    result.battle.getStatusApplied() != null
                    ? result.battle.getStatusApplied().name() : "NONE");
            battleMap.put("statusTarget",     result.battle.getStatusTarget());
            battleMap.put("destinyBond",      result.battle.isDestinyBondTriggered());
            resp.put("battle", battleMap);
        }

        return ResponseEntity.ok(resp);
    }
}
