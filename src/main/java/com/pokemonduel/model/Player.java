package com.pokemonduel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Perfil de um jogador.
 * Persistido em jogadores.json via Jackson.
 */
public class Player {

    private String id;               // UUID gerado no registro
    private String username;
    private List<String> unlockedPokemonIds; // IDs do catálogo que o jogador possui
    private List<String> deckIds;            // até 6 IDs para o deck atual
    private int wins;
    private int losses;
    private int rating;              // ELO para multiplayer

    public Player() {
        this.unlockedPokemonIds = new ArrayList<>();
        this.deckIds = new ArrayList<>();
        this.rating = 1000; // ELO inicial
    }

    public Player(String id, String username) {
        this();
        this.id = id;
        this.username = username;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getUnlockedPokemonIds() { return unlockedPokemonIds; }
    public void setUnlockedPokemonIds(List<String> unlockedPokemonIds) { this.unlockedPokemonIds = unlockedPokemonIds; }

    public List<String> getDeckIds() { return deckIds; }
    public void setDeckIds(List<String> deckIds) { this.deckIds = deckIds; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
