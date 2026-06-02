package com.pokemonduel.model;

import com.pokemonduel.model.enums.PokemonType;
import com.pokemonduel.model.enums.Rarity;
import com.pokemonduel.model.enums.StatusEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modelo de uma figura de Pokémon.
 *
 * Cada Pokémon tem uma roleta definida por sua lista de Move's.
 * A roleta é "girada" pelo método spin(), que sorteia um Move
 * com probabilidade proporcional ao seu percentage.
 *
 * pm (Pontos de Movimento): quantas casas o Pokémon pode mover por turno.
 *
 * Na partida, um PokemonFigure (instância em jogo) referencia um Pokemon
 * e rastreia posição, status e estado atual.
 */
public class Pokemon {

    private String id;            // ex: "charizard", "pikachu"
    private String name;          // ex: "Charizard"
    private PokemonType type;
    private Rarity rarity;
    private int pm;               // Pontos de Movimento base
    private List<Move> moves;     // segmentos da roleta (soma = 100%)
    private String spriteFile;    // ex: "charizard.png" (96x96)

    // ──────────────────────────────────────────────────────────────────────────

    public Pokemon() {
        this.moves = new ArrayList<>();
    }

    public Pokemon(String id, String name, PokemonType type, Rarity rarity, int pm) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.pm = pm;
        this.moves = new ArrayList<>();
        this.spriteFile = id + ".png";
    }

    // ── Roleta ────────────────────────────────────────────────────────────────

    /**
     * Gira a roleta e retorna o Move sorteado.
     *
     * Algoritmo: soma todos os percentuais (deveria ser 100),
     * gera um número aleatório [0, total), percorre os segmentos
     * subtraindo seus pesos até encontrar o vencedor.
     *
     * Este método é chamado pelo BattleService no backend;
     * o frontend apenas anima o resultado recebido via API.
     */
    public Move spin() {
        int total = moves.stream().mapToInt(Move::getPercentage).sum();
        int roll  = new Random().nextInt(total);

        for (Move move : moves) {
            roll -= move.getPercentage();
            if (roll < 0) return move;
        }
        return moves.get(moves.size() - 1); // fallback (nunca deve acontecer)
    }

    /**
     * Valida que a soma dos percentuais é exatamente 100.
     * Chame ao registrar um Pokémon no catálogo.
     */
    public boolean isWheelValid() {
        return moves.stream().mapToInt(Move::getPercentage).sum() == 100;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Pokemon addMove(Move move) {
        this.moves.add(move);
        return this; // fluent API
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PokemonType getType() { return type; }
    public void setType(PokemonType type) { this.type = type; }

    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }

    public int getPm() { return pm; }
    public void setPm(int pm) { this.pm = pm; }

    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    public String getSpriteFile() { return spriteFile; }
    public void setSpriteFile(String spriteFile) { this.spriteFile = spriteFile; }

    @Override
    public String toString() {
        return String.format("Pokemon{%s, %s, %s, PM=%d, moves=%d}",
                id, name, rarity, pm, moves.size());
    }
}
