package com.pokemonduel.service;

import com.pokemonduel.model.*;
import com.pokemonduel.model.PokemonFigure.FigureState;
import com.pokemonduel.model.enums.NodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
/**
 * Serviço central do motor de jogo.
 *
 * Responsável por:
 *  - Validar e aplicar movimentos (MP move + entrada do banco)
 *  - Detectar e resolver batalhas ao entrar num nó inimigo
 *  - Detectar cerco e aplicar K.O. automático
 *  - Verificar condições de vitória após cada ação
 *  - Gerenciar retorno do P.C. ao banco
 */
@Service
public class GameService {

    @Autowired private BattleService battleService;

    // ── Mover figura ──────────────────────────────────────────────────────────

    /**
     * Resultado de uma tentativa de movimento.
     * Pode conter uma batalha se o destino estava ocupado por inimigo.
     */
    public static class MoveResult {
        public boolean success;
        public String error;
        public BattleResult battle;      // null se não houve batalha
        public String attackerFigureId;  // preenchido quando battle != null
        public String defenderFigureId;  // preenchido quando battle != null
        public boolean surroundKO;       // true se houve K.O. por cerco
        public boolean matchOver;
        public GameState gameState;

        public static MoveResult ok(GameState gs) {
            MoveResult r = new MoveResult();
            r.success = true;
            r.gameState = gs;
            return r;
        }

        public static MoveResult withBattle(GameState gs, BattleResult b,
                                             String attackerFigureId, String defenderFigureId) {
            MoveResult r = new MoveResult();
            r.success = true;
            r.battle = b;
            r.attackerFigureId = attackerFigureId;
            r.defenderFigureId = defenderFigureId;
            r.gameState = gs;
            return r;
        }

        public static MoveResult err(String msg) {
            MoveResult r = new MoveResult();
            r.success = false;
            r.error = msg;
            return r;
        }
    }

    /**
     * Move uma figura do banco para o tabuleiro (entrada).
     *
     * @param gs       estado atual da partida
     * @param figureId ID da figura a entrar
     * @param playerId ID do jogador que está agindo
     */
    /** Overload usado pelo bot/IA — deixa o servidor escolher a entrada livre. */
    public MoveResult enterBoard(GameState gs, String figureId, String playerId) {
        return enterBoard(gs, figureId, playerId, -1);
    }

    /**
     * @param requestedNodeId nó ENTRY que o jogador clicou (deve ser dono
     *                        dele e estar livre); -1 = deixa o servidor
     *                        escolher automaticamente (usado pelo bot).
     */
    public MoveResult enterBoard(GameState gs, String figureId, String playerId, int requestedNodeId) {
        if (gs.getStatus() != GameState.Status.ACTIVE)
            return MoveResult.err("Partida não está ativa");

        int player = playerNumber(gs, playerId);
        if (player != gs.getCurrentTurn())
            return MoveResult.err("Não é o seu turno");

        if (gs.getPendingActionFigureId() != null)
            return MoveResult.err("Você ainda tem PM sobrando de outra figura neste turno — mova-a ou passe a vez");

        Optional<PokemonFigure> optFig = gs.findFigure(figureId);
        if (optFig.isEmpty()) return MoveResult.err("Figura não encontrada");

        PokemonFigure fig = optFig.get();
        if (fig.getOwner() != player) return MoveResult.err("Figura não é sua");
        if (fig.getState() != FigureState.BENCH) return MoveResult.err("Figura não está no banco");

        int entryNode;
        if (requestedNodeId < 0) {
            entryNode = gs.entryNodeOf(player); // bot: escolha automática
            if (entryNode == -1) return MoveResult.err("Nenhum ponto de entrada disponível");
        } else {
            BoardNode node = gs.getBoard().getNode(requestedNodeId);
            if (node == null || node.getType() != NodeType.ENTRY)
                return MoveResult.err("Nó escolhido não é uma entrada válida");
            if (node.getOwner() != player)
                return MoveResult.err("Essa entrada não é sua");
            if (gs.figureAtNode(requestedNodeId).isPresent())
                return MoveResult.err("Essa entrada já está ocupada");
            entryNode = requestedNodeId;
        }

        fig.enterBoard(entryNode);

        // Verifica cerco imediato ao entrar
        MoveResult result = checkSurroundAndVictory(gs, fig, player);
        if (!result.success) return result;
        if (result.matchOver) return result;

        // A entrada consome 1 ponto de movimento. Se a figura tinha PM > 1,
        // sobra PM pra continuar se movendo NESTE MESMO turno (o jogador pode
        // clicar num nó verde alcançável com o PM restante, ou chamar
        // /passar pra encerrar o turno sem usar o resto).
        int pmRestante = fig.getEffectivePm() - 1;
        if (pmRestante > 0) {
            gs.setPendingAction(fig.getFigureId(), pmRestante);
        } else {
            gs.nextTurn();
        }
        return MoveResult.ok(gs);
    }

    /**
     * Move uma figura ativa para um nó de destino.
     *
     * @param gs       estado atual
     * @param figureId figura a mover
     * @param destNode nó de destino
     * @param playerId ID do jogador
     */
    public MoveResult moveFigure(GameState gs, String figureId, int destNode, String playerId) {
        if (gs.getStatus() != GameState.Status.ACTIVE)
            return MoveResult.err("Partida não está ativa");

        int player = playerNumber(gs, playerId);
        if (player != gs.getCurrentTurn())
            return MoveResult.err("Não é o seu turno");

        if (gs.getPendingActionFigureId() != null && !gs.getPendingActionFigureId().equals(figureId))
            return MoveResult.err("Você ainda tem PM sobrando de outra figura neste turno — mova-a ou passe a vez");

        Optional<PokemonFigure> optFig = gs.findFigure(figureId);
        if (optFig.isEmpty()) return MoveResult.err("Figura não encontrada");

        PokemonFigure fig = optFig.get();
        if (fig.getOwner() != player) return MoveResult.err("Figura não é sua");
        if (fig.getState() != FigureState.ACTIVE) return MoveResult.err("Figura não está em campo");
        if (!fig.canMove()) return MoveResult.err("Figura não pode se mover agora (status ativo)");

        // Valida alcance — se essa figura tem uma ação pendente neste turno
        // (acabou de entrar e sobrou PM), usa o PM restante; senão, PM completo.
        int budget = fig.getEffectivePm();
        if (figureId.equals(gs.getPendingActionFigureId())) {
            budget = gs.getPendingActionBudget();
        }

        Set<Integer> friendly = gs.activeNodesOf(player);
        friendly.remove(fig.getNodeId()); // remove a própria figura para não bloquear
        int opponent = player == 1 ? 2 : 1;
        Set<Integer> enemyNodes = gs.activeNodesOf(opponent);
        Set<Integer> reachable = gs.getBoard().reachableNodes(fig.getNodeId(),
                budget, friendly, enemyNodes);

        if (!reachable.contains(destNode))
            return MoveResult.err("Destino fora do alcance (PM restante = " + budget + ")");

        // Verifica se o destino está ocupado por aliado
        Optional<PokemonFigure> occupant = gs.figureAtNode(destNode);
        if (occupant.isPresent() && occupant.get().getOwner() == player)
            return MoveResult.err("Destino ocupado por figura aliada");

        // Move a figura (posição provisória — pode ser revertida se a
        // batalha terminar em empate, ver abaixo)
        int origemAtacante = fig.getNodeId();
        fig.setNodeId(destNode);

        // ── Batalha ao entrar em nó inimigo ──────────────────────────────────
        if (occupant.isPresent() && occupant.get().getOwner() != player) {
            PokemonFigure defender = occupant.get();
            BattleResult battle = battleService.resolve(fig, defender);

            // Libera P.C. se alguém foi ao P.C.
            if (battle.defenderGoesToPC()) {
                gs.onFigureSentToPC(defender.getOwner());
                addToPcQueue(gs, defender);
                // Atacante vence e ocupa o nó do defensor — fig já está em destNode.
            } else if (battle.attackerGoesToPC()) {
                gs.onFigureSentToPC(fig.getOwner());
                addToPcQueue(gs, fig);
                // battleService.resolve() já chamou fig.sendToPC() (nodeId=-1,
                // state=PC) — nada a reverter aqui.
            } else {
                // Empate (DRAW ou COMPARE_TIE): ninguém foi derrotado, então
                // ninguém se move — o atacante volta pra posição de origem.
                // Sem isso, as duas figuras ficariam empilhadas no mesmo nó.
                fig.setNodeId(origemAtacante);
            }

            MoveResult result = MoveResult.withBattle(gs, battle, fig.getFigureId(), defender.getFigureId());
            if (gs.checkVictory()) {
                result.matchOver = true;
                return result;
            }
            gs.nextTurn();
            return result;
        }

        // ── Cerco e vitória ───────────────────────────────────────────────────
        MoveResult result = checkSurroundAndVictory(gs, fig, player);
        if (!result.success) return result;

        gs.nextTurn();
        return MoveResult.ok(gs);
    }

    /**
     * Encerra o turno sem mover mais. Só é válido quando existe uma ação
     * pendente (a figura acabou de entrar/mover e sobrou PM que o jogador
     * optou por não usar).
     */
    public MoveResult passarVez(GameState gs, String playerId) {
        if (gs.getStatus() != GameState.Status.ACTIVE)
            return MoveResult.err("Partida não está ativa");

        int player = playerNumber(gs, playerId);
        if (player != gs.getCurrentTurn())
            return MoveResult.err("Não é o seu turno");

        if (gs.getPendingActionFigureId() == null)
            return MoveResult.err("Nada para passar — jogue uma figura primeiro");

        gs.nextTurn();
        return MoveResult.ok(gs);
    }

    // ── Helpers internos ──────────────────────────────────────────────────────

    private MoveResult checkSurroundAndVictory(GameState gs, PokemonFigure movedFig, int player) {
        int enemy = player == 1 ? 2 : 1;

        // Verifica se a figura movida cercou algum inimigo
        // (usa os nós amigos atuais — inclui a figura recém-movida)
        Set<Integer> allFriendly = gs.activeNodesOf(player);
        for (PokemonFigure enemyFig : gs.figuresOf(enemy)) {
            if (enemyFig.getState() != FigureState.ACTIVE) continue;
            if (gs.getBoard().isSurrounded(enemyFig.getNodeId(), allFriendly)) {
                battleService.applySurroundKO(enemyFig);
                gs.onFigureSentToPC(enemyFig.getOwner());
                addToPcQueue(gs, enemyFig);
            }
        }

        // Verifica vitória
        if (gs.checkVictory()) {
            MoveResult r = MoveResult.ok(gs);
            r.matchOver = true;
            return r;
        }
        return MoveResult.ok(gs);
    }

    private void addToPcQueue(GameState gs, PokemonFigure fig) {
        if (fig.getOwner() == 1) {
            gs.getPcQueueP1().offer(fig.getFigureId());
        } else {
            gs.getPcQueueP2().offer(fig.getFigureId());
        }
    }

    private int playerNumber(GameState gs, String playerId) {
        if (playerId.equals(gs.getPlayer1Id())) return 1;
        if (playerId.equals(gs.getPlayer2Id())) return 2;
        return -1;
    }
}
