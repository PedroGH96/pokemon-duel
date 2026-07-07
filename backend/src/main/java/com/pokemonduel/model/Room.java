package com.pokemonduel.model;

/**
 * Sala de espera para uma partida multiplayer.
 *
 * Estados:
 *   WAITING  → esperando o segundo jogador
 *   READY    → dois jogadores conectados, partida prestes a começar
 *   PLAYING  → partida em andamento
 *   CLOSED   → partida encerrada
 */
public class Room {

    public enum RoomStatus { WAITING, READY, PLAYING, CLOSED }

    private String id;
    private String name;
    private boolean privateRoom;
    private String player1Id;
    private String player2Id;
    private String matchId;       // preenchido quando a partida começa
    private RoomStatus status;

    public Room() {}

    public Room(String id, String name, boolean privateRoom, String player1Id) {
        this.id = id;
        this.name = name;
        this.privateRoom = privateRoom;
        this.player1Id = player1Id;
        this.status = RoomStatus.WAITING;
    }

    public boolean isFull() {
        return player1Id != null && player2Id != null;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Alias em português para compatibilidade com o cliente Godot. */
    public String getNome() { return name; }

    public boolean isPrivateRoom() { return privateRoom; }
    public void setPrivateRoom(boolean privateRoom) { this.privateRoom = privateRoom; }

    public String getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(String player1Id) { this.player1Id = player1Id; }

    public String getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(String player2Id) { this.player2Id = player2Id; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
}
