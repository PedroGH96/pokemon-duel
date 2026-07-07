package com.pokemonduel.model;

import com.pokemonduel.model.PokemonFigure.FigureState;
import com.pokemonduel.model.enums.NodeType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estado completo de uma partida em andamento.
 *
 * Ciclo de vida:
 *   WAITING → ambos os jogadores conectados, mas a partida ainda não começou
 *   ACTIVE  → partida em andamento
 *   FINISHED → alguém venceu
 *
 * Regras implementadas:
 *   - Movimentação com PM (Pontos de Movimento)
 *   - Batalha ao entrar num nó ocupado pelo inimigo
 *   - Cerco automático (K.O. sem batalha)
 *   - Retorno do P.C. ao banco quando o adversário envia alguém ao P.C.
 *   - Condições de vitória: atingir GOAL, esgotar tempo (5 min), stalemate
 */
public class GameState {

    public enum Status { WAITING, ACTIVE, FINISHED }

    private String matchId;
    private String roomId;

    private String player1Id;
    private String player2Id;
    private int currentTurn;          // 1 ou 2
    private int turnNumber;           // contador global de turnos
    private Status status;

    // Ação pendente: quando uma figura entra no tabuleiro (ou se move) e ainda
    // sobra PM, o turno NÃO passa — o jogador pode continuar movendo essa
    // mesma figura (com o PM restante) ou chamar /passar pra encerrar o turno.
    private String pendingActionFigureId; // null = nenhuma ação pendente
    private int pendingActionBudget;      // PM restante disponível pra essa figura

    private List<PokemonFigure> figures1; // figuras do jogador 1
    private List<PokemonFigure> figures2; // figuras do jogador 2

    private Board board;

    private Instant startedAt;
    private Instant lastActionAt;
    private static final long MATCH_DURATION_SECONDS = 300; // 5 minutos

    private int winnerId; // 0 = nenhum, 1 ou 2
    private String winReason;

    // Fila de retorno do P.C.: figuras que voltam ao banco quando
    // o adversário enviar qualquer figura ao P.C.
    private Queue<String> pcQueueP1 = new LinkedList<>();
    private Queue<String> pcQueueP2 = new LinkedList<>();

    // ── Construção ────────────────────────────────────────────────────────────

    public GameState() {}

    public static GameState create(String matchId, String roomId,
                                   String player1Id, String player2Id,
                                   List<PokemonFigure> figures1,
                                   List<PokemonFigure> figures2) {
        GameState gs = new GameState();
        gs.matchId    = matchId;
        gs.roomId     = roomId;
        gs.player1Id  = player1Id;
        gs.player2Id  = player2Id;
        gs.figures1   = new ArrayList<>(figures1);
        gs.figures2   = new ArrayList<>(figures2);
        gs.board      = new Board();
        gs.currentTurn = 1;
        gs.turnNumber  = 1;
        gs.status      = Status.ACTIVE;
        gs.startedAt   = Instant.now();
        gs.lastActionAt = Instant.now();
        gs.winnerId    = 0;
        return gs;
    }

    // ── Helpers de figuras ────────────────────────────────────────────────────

    public List<PokemonFigure> figuresOf(int player) {
        return player == 1 ? figures1 : figures2;
    }

    public List<PokemonFigure> enemyFiguresOf(int player) {
        return player == 1 ? figures2 : figures1;
    }

    public Optional<PokemonFigure> findFigure(String figureId) {
        return allFigures().stream()
                .filter(f -> f.getFigureId().equals(figureId))
                .findFirst();
    }

    public List<PokemonFigure> allFigures() {
        List<PokemonFigure> all = new ArrayList<>(figures1);
        all.addAll(figures2);
        return all;
    }

    /** Figura no nó dado, ou empty. */
    public Optional<PokemonFigure> figureAtNode(int nodeId) {
        return allFigures().stream()
                .filter(f -> f.getState() == FigureState.ACTIVE && f.getNodeId() == nodeId)
                .findFirst();
    }

    /** Nós ocupados pelas figuras ativas do jogador. */
    public Set<Integer> activeNodesOf(int player) {
        return figuresOf(player).stream()
                .filter(f -> f.getState() == FigureState.ACTIVE)
                .map(PokemonFigure::getNodeId)
                .collect(Collectors.toSet());
    }

    /** Verifica se o jogo excedeu o tempo limite. */
    public boolean isTimeUp() {
        if (startedAt == null) return false;
        return Instant.now().getEpochSecond() - startedAt.getEpochSecond() > MATCH_DURATION_SECONDS;
    }

    /** Segundos restantes (0 se acabou). */
    public long secondsRemaining() {
        if (startedAt == null) return MATCH_DURATION_SECONDS;
        long elapsed = Instant.now().getEpochSecond() - startedAt.getEpochSecond();
        return Math.max(0, MATCH_DURATION_SECONDS - elapsed);
    }

    /** Verifica se o jogador atual não tem nenhum movimento válido. */
    public boolean isStalemate() {
        Set<Integer> friendly = activeNodesOf(currentTurn);
        for (PokemonFigure fig : figuresOf(currentTurn)) {
            if (fig.getState() != FigureState.ACTIVE) continue;
            if (!fig.canMove()) continue;
            Set<Integer> reachable = board.reachableNodes(fig.getNodeId(),
                    fig.getEffectivePm(), friendly);
            if (!reachable.isEmpty()) return false;
        }
        // Também verifica se há figuras no banco (pode entrar no tabuleiro)
        boolean hasBench = figuresOf(currentTurn).stream()
                .anyMatch(f -> f.getState() == FigureState.BENCH);
        return !hasBench;
    }

    /** Checa todas as condições de vitória e atualiza winnerId se necessário. */
    public boolean checkVictory() {
        // 1. Alguém chegou ao GOAL adversário?
        for (PokemonFigure fig : figures1) {
            if (fig.getState() == FigureState.ACTIVE
                    && board.getNode(fig.getNodeId()) != null
                    && board.getNode(fig.getNodeId()).getType() == NodeType.GOAL
                    && board.getNode(fig.getNodeId()).getOwner() == 2) {
                setWinner(1, "Objetivo alcançado");
                return true;
            }
        }
        for (PokemonFigure fig : figures2) {
            if (fig.getState() == FigureState.ACTIVE
                    && board.getNode(fig.getNodeId()) != null
                    && board.getNode(fig.getNodeId()).getType() == NodeType.GOAL
                    && board.getNode(fig.getNodeId()).getOwner() == 1) {
                setWinner(2, "Objetivo alcançado");
                return true;
            }
        }
        // 2. Tempo esgotado → jogador com mais figuras ativas vence
        if (isTimeUp()) {
            long active1 = figures1.stream().filter(f -> f.getState() == FigureState.ACTIVE).count();
            long active2 = figures2.stream().filter(f -> f.getState() == FigureState.ACTIVE).count();
            if (active1 > active2) setWinner(1, "Tempo esgotado");
            else if (active2 > active1) setWinner(2, "Tempo esgotado");
            else setWinner(0, "Empate por tempo");
            return true;
        }
        // 3. Stalemate: jogador atual não tem movimentos → adversário vence
        if (isStalemate()) {
            int winner = currentTurn == 1 ? 2 : 1;
            setWinner(winner, "Vitória por espera");
            return true;
        }
        return false;
    }

    private void setWinner(int player, String reason) {
        this.winnerId  = player;
        this.winReason = reason;
        this.status    = Status.FINISHED;
    }

    /** Avança para o próximo turno e executa tick de status. */
    public void nextTurn() {
        // Tick de status das figuras do jogador atual
        for (PokemonFigure fig : figuresOf(currentTurn)) {
            fig.tickStatus();
        }
        currentTurn = currentTurn == 1 ? 2 : 1;
        turnNumber++;
        lastActionAt = Instant.now();
        pendingActionFigureId = null;
        pendingActionBudget = 0;
    }

    /** Marca uma ação pendente: a figura ainda tem PM sobrando neste turno. */
    public void setPendingAction(String figureId, int budgetRemaining) {
        this.pendingActionFigureId = figureId;
        this.pendingActionBudget = budgetRemaining;
    }

    /** Limpa a ação pendente sem avançar o turno (uso interno). */
    public void clearPendingAction() {
        this.pendingActionFigureId = null;
        this.pendingActionBudget = 0;
    }

    public String getPendingActionFigureId() { return pendingActionFigureId; }
    public int getPendingActionBudget() { return pendingActionBudget; }

    /**
     * Chamado quando uma figura é enviada ao P.C.
     * Libera a primeira figura da fila do P.C. do adversário de volta ao banco.
     */
    public void onFigureSentToPC(int ownerOfKnockedOut) {
        // O adversário do K.O. pode liberar um Pokémon do P.C.
        int adversary = ownerOfKnockedOut == 1 ? 2 : 1;
        Queue<String> pcQueue = adversary == 1 ? pcQueueP1 : pcQueueP2;
        if (!pcQueue.isEmpty()) {
            String figId = pcQueue.poll();
            findFigure(figId).ifPresent(PokemonFigure::returnToBench);
        }
        // Adiciona a figura derrubada à fila do P.C.
        // (já foi enviada ao PC por sendToPC(), só registra na fila)
    }

    // ── Entrada no tabuleiro ──────────────────────────────────────────────────

    /** Ponto de entrada do jogador (nó ENTRY cujo owner == player). */
    public int entryNodeOf(int player) {
        // Ordena por ID para garantir comportamento determinístico:
        // Jogador 1 → nós 21 e 27 (owner=1, linha 4/base)
        // Jogador 2 → nós 0 e 6   (owner=2, linha 0/topo)
        return board.getAllNodes().entrySet().stream()
                .filter(e -> e.getValue().getType() == NodeType.ENTRY
                          && e.getValue().getOwner() == player)
                .sorted(Map.Entry.comparingByKey())
                .filter(e -> figureAtNode(e.getKey()).isEmpty())
                .map(Map.Entry::getKey)
                .findFirst()
                // Se todos ocupados, retorna o primeiro ENTRY do jogador
                .orElseGet(() -> board.getAllNodes().entrySet().stream()
                        .filter(e -> e.getValue().getType() == NodeType.ENTRY
                                  && e.getValue().getOwner() == player)
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(-1));
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(String player1Id) { this.player1Id = player1Id; }

    public String getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(String player2Id) { this.player2Id = player2Id; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<PokemonFigure> getFigures1() { return figures1; }
    public void setFigures1(List<PokemonFigure> figures1) { this.figures1 = figures1; }

    public List<PokemonFigure> getFigures2() { return figures2; }
    public void setFigures2(List<PokemonFigure> figures2) { this.figures2 = figures2; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getLastActionAt() { return lastActionAt; }
    public void setLastActionAt(Instant lastActionAt) { this.lastActionAt = lastActionAt; }

    public int getWinnerId() { return winnerId; }
    public void setWinnerId(int winnerId) { this.winnerId = winnerId; }

    public String getWinReason() { return winReason; }
    public void setWinReason(String winReason) { this.winReason = winReason; }

    public Queue<String> getPcQueueP1() { return pcQueueP1; }
    public void setPcQueueP1(Queue<String> pcQueueP1) { this.pcQueueP1 = pcQueueP1; }

    public Queue<String> getPcQueueP2() { return pcQueueP2; }
    public void setPcQueueP2(Queue<String> pcQueueP2) { this.pcQueueP2 = pcQueueP2; }
}
