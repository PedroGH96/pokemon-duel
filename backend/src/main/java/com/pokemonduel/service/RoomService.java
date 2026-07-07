package com.pokemonduel.service;

import com.pokemonduel.model.*;
import com.pokemonduel.model.PokemonFigure.FigureState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gerencia salas de espera e partidas em andamento.
 * Tudo em memória (sem persistência em arquivo para partidas).
 */
@Service
public class RoomService {

    @Autowired private PokemonCatalogService catalog;
    @Autowired private PlayerService playerService;

    // ── Storage em memória ────────────────────────────────────────────────────
    private final Map<String, Room>      rooms   = new ConcurrentHashMap<>();
    private final Map<String, GameState> matches = new ConcurrentHashMap<>();

    // ── Salas ─────────────────────────────────────────────────────────────────

    public Room createRoom(String name, boolean privateRoom, String playerId) {
        String id = UUID.randomUUID().toString();
        Room room = new Room(id, name, privateRoom, playerId);
        rooms.put(id, room);
        return room;
    }

    public List<Room> listPublicRooms() {
        return rooms.values().stream()
                .filter(r -> !r.isPrivateRoom() && r.getStatus() == Room.RoomStatus.WAITING)
                .collect(Collectors.toList());
    }

    public Optional<Room> findRoom(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    /**
     * Entra em uma sala existente.
     * Se a sala ficar cheia, cria a partida automaticamente.
     *
     * @return a sala atualizada (com matchId preenchido se a partida começou)
     */
    public Optional<Room> joinRoom(String roomId, String playerId) {
        Room room = rooms.get(roomId);
        if (room == null) return Optional.empty();
        if (room.isFull()) return Optional.empty();
        if (room.getPlayer1Id().equals(playerId)) return Optional.empty(); // mesmo jogador

        room.setPlayer2Id(playerId);
        room.setStatus(Room.RoomStatus.READY);

        // Cria a partida
        GameState gs = startMatch(room);
        room.setMatchId(gs.getMatchId());
        room.setStatus(Room.RoomStatus.PLAYING);

        return Optional.of(room);
    }

    // ── Partidas ──────────────────────────────────────────────────────────────

    public Optional<GameState> findMatch(String matchId) {
        return Optional.ofNullable(matches.get(matchId));
    }

    public List<GameState> allMatches() {
        return new ArrayList<>(matches.values());
    }

    /** Injeta uma partida de debug criada externamente (DebugController). */
    public void injectMatchForDebug(String matchId, GameState gs) {
        matches.put(matchId, gs);
    }

    /**
     * Cria o GameState inicial para a sala.
     * Usa o deck salvo de cada jogador (ou os starters se o deck estiver vazio).
     */
    private GameState startMatch(Room room) {
        List<PokemonFigure> figs1 = buildFigures(room.getPlayer1Id(), 1);
        List<PokemonFigure> figs2 = buildFigures(room.getPlayer2Id(), 2);

        String matchId = UUID.randomUUID().toString();
        GameState gs = GameState.create(matchId, room.getId(),
                room.getPlayer1Id(), room.getPlayer2Id(), figs1, figs2);
        matches.put(matchId, gs);
        return gs;
    }

    /**
     * Constrói as figuras de um jogador a partir do deck salvo.
     * Se o deck estiver vazio, usa os 6 starters do catálogo.
     */
    private List<PokemonFigure> buildFigures(String playerId, int playerNum) {
        Player player = playerService.findByIdOptional(playerId).orElse(null);

        List<String> ids = (player != null && !player.getDeckIds().isEmpty())
                ? player.getDeckIds()
                : PokemonCatalogService.STARTER_IDS;

        List<PokemonFigure> figures = new ArrayList<>();
        for (String pokemonId : ids) {
            Pokemon poke = catalog.get(pokemonId);
            if (poke == null) continue;
            String figureId = UUID.randomUUID().toString();
            figures.add(new PokemonFigure(figureId, poke, playerNum));
        }
        return figures;
    }

    // ── Estado da partida ─────────────────────────────────────────────────────

    /** Snapshot simplificado do estado para o Godot (sem objetos internos pesados). */
    public Map<String, Object> matchSnapshot(GameState gs) {
        List<Map<String, Object>> figureList = new ArrayList<>();
        for (PokemonFigure fig : gs.allFigures()) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("figureId",   fig.getFigureId());
            f.put("pokemonId",  fig.getPokemon().getId());
            f.put("pokemonName",fig.getPokemon().getName());
            f.put("spriteFile", fig.getPokemon().getSpriteFile());
            f.put("shiny",      fig.isShiny());
            f.put("owner",      fig.getOwner());
            f.put("nodeId",     fig.getNodeId());
            f.put("state",      fig.getState().name());
            f.put("status",     fig.getActiveStatus() != null ? fig.getActiveStatus().name() : "NONE");
            f.put("statusTurnsLeft", fig.getStatusTurnsLeft());
            f.put("pm",         fig.getEffectivePm());
            figureList.add(f);
        }

        // Grafo do tabuleiro (nós)
        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (BoardNode node : gs.getBoard().getAllNodes().values()) {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id",    node.getId());
            n.put("type",  node.getType().name());
            n.put("col",   node.getCol());
            n.put("row",   node.getRow());
            n.put("owner", node.getOwner());
            nodeList.add(n);
        }

        // Adjacências (para o Godot desenhar as arestas)
        Map<String, List<Integer>> adj = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Integer>> e : gs.getBoard().getAdjacency().entrySet()) {
            adj.put(String.valueOf(e.getKey()), e.getValue());
        }

        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("matchId",         gs.getMatchId());
        snap.put("status",          gs.getStatus().name());
        snap.put("currentTurn",     gs.getCurrentTurn());
        snap.put("turnNumber",      gs.getTurnNumber());
        snap.put("player1Id",       gs.getPlayer1Id());
        snap.put("player2Id",       gs.getPlayer2Id());
        snap.put("secondsRemaining", gs.secondsRemaining());
        snap.put("winnerId",        gs.getWinnerId());
        snap.put("winReason",       gs.getWinReason());
        snap.put("pendingActionFigureId", gs.getPendingActionFigureId());
        snap.put("pendingActionBudget",   gs.getPendingActionBudget());
        snap.put("figures",         figureList);
        snap.put("nodes",           nodeList);
        snap.put("adjacency",       adj);
        return snap;
    }
}
