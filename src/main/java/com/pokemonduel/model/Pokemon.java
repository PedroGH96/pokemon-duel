package com.pokemonduel.model;

import com.pokemonduel.model.enums.PokemonType;
import com.pokemonduel.model.enums.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modelo de uma figura de Pokémon.
 *
 * Campos:
 *  id          — chave interna em letras minúsculas, sem espaços (ex: "charmander")
 *                Usado em catalog.get(id), URLs REST e como nome do sprite.
 *  dexId       — número de exibição na UI (ex: 1 para Charmander, "ID-1")
 *  name        — nome de exibição (ex: "Charmander")
 *  type        — tipo elemental (FOGO, AGUA, GRAMA...)
 *  rarity      — raridade da figura (C, UC, R, EX, UX)
 *  pm          — Pontos de Movimento base por turno
 *  specialAbility — habilidade especial passiva (null = nenhuma)
 *  moves       — segmentos da roleta; a soma dos percentage deve ser 100
 *  spriteFile  — nome do arquivo de sprite 96x96 (ex: "charmander.png")
 */
public class Pokemon {

    private String id;
    private int dexId;
    private String name;
    private PokemonType type;
    private Rarity rarity;
    private int pm;
    private String specialAbility; // null ou "" = sem habilidade especial
    private List<Move> moves;
    private String spriteFile;

    public Pokemon() {
        this.moves = new ArrayList<>();
    }

    public Pokemon(String id, int dexId, String name, PokemonType type,
                   Rarity rarity, int pm, String specialAbility) {
        this.id = id;
        this.dexId = dexId;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.pm = pm;
        this.specialAbility = specialAbility;
        this.moves = new ArrayList<>();
        this.spriteFile = id + ".png";
    }

    // Construtor sem habilidade especial (atalho)
    public Pokemon(String id, int dexId, String name, PokemonType type,
                   Rarity rarity, int pm) {
        this(id, dexId, name, type, rarity, pm, null);
    }

    // ── Roleta ────────────────────────────────────────────────────────────────

    /**
     * Gira a roleta e retorna o Move sorteado.
     * Sorteia proporcionalmente ao percentage de cada segmento.
     */
    public Move spin() {
        int total = moves.stream().mapToInt(Move::getPercentage).sum();
        int roll  = new Random().nextInt(total);
        for (Move move : moves) {
            roll -= move.getPercentage();
            if (roll < 0) return move;
        }
        return moves.get(moves.size() - 1);
    }

    /** Valida que a soma dos percentuais é exatamente 100. */
    public boolean isWheelValid() {
        return moves.stream().mapToInt(Move::getPercentage).sum() == 100;
    }

    public boolean hasSpecialAbility() {
        return specialAbility != null && !specialAbility.isBlank();
    }

    public Pokemon addMove(Move move) {
        this.moves.add(move);
        return this;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getDexId() { return dexId; }
    public void setDexId(int dexId) { this.dexId = dexId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PokemonType getType() { return type; }
    public void setType(PokemonType type) { this.type = type; }

    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }

    public int getPm() { return pm; }
    public void setPm(int pm) { this.pm = pm; }

    public String getSpecialAbility() { return specialAbility; }
    public void setSpecialAbility(String specialAbility) { this.specialAbility = specialAbility; }

    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    public String getSpriteFile() { return spriteFile; }
    public void setSpriteFile(String spriteFile) { this.spriteFile = spriteFile; }

    @Override
    public String toString() {
        return String.format("Pokemon{ID-%d %s, %s, %s, PM=%d, moves=%d}",
                dexId, name, rarity, type, pm, moves.size());
    }
}
