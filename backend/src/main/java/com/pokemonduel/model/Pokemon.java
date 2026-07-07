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
 *  dexId       — número de exibição na UI (ex: 5 para Charmander, "ID-5")
 *  name        — nome de exibição (ex: "Charmander")
 *  type        — tipo elemental primário (FOGO, AGUA, GRAMA...)
 *  type2       — tipo elemental secundário (null = Pokémon mono-tipo)
 *  rarity      — raridade da figura (C, UC, R, EX, UX)
 *  pm          — Pontos de Movimento base por turno
 *  specialAbility — habilidade especial passiva (null = nenhuma)
 *  moves       — segmentos da roleta. A soma dos "percentage" varia por Pokémon
 *                (no jogo original a maioria soma 96, mas alguns diferem) —
 *                o sorteio em spin() é proporcional ao total real, então
 *                não é necessário forçar uma soma fixa.
 *  spriteFile  — nome do arquivo de sprite (ex: "charmander.png")
 */
public class Pokemon {

    private String id;
    private int dexId;
    private String name;
    private PokemonType type;
    private PokemonType type2; // null se o Pokémon for mono-tipo
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

    /**
     * Valida que a roleta tem pelo menos um segmento com peso positivo.
     * O total não precisa ser 100 (ou 96) — spin() funciona com qualquer total,
     * pois sorteia proporcionalmente ao peso de cada segmento.
     */
    public boolean isWheelValid() {
        return !moves.isEmpty() && moves.stream().mapToInt(Move::getPercentage).sum() > 0;
    }

    /** Soma dos pesos de todos os segmentos da roleta (não necessariamente 100). */
    public int getWheelTotal() {
        return moves.stream().mapToInt(Move::getPercentage).sum();
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

    public PokemonType getType2() { return type2; }
    public void setType2(PokemonType type2) { this.type2 = type2; }

    public boolean isDualType() { return type2 != null; }

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
