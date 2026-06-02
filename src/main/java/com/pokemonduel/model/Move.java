package com.pokemonduel.model;

import com.pokemonduel.model.enums.MoveColor;
import com.pokemonduel.model.enums.StatusEffect;

/**
 * Representa um segmento da roleta de um Pokémon.
 *
 * A roleta é composta por N Move's cujos percentuais somam 100.
 * O servidor sorteia qual segmento foi girado usando pesos proporcionais.
 *
 * Campos:
 *  - name        : nome do ataque (ex: "Flamethrower")
 *  - color       : cor do segmento — define o resultado da batalha
 *  - percentage  : tamanho do segmento em % (ex: 20 = 20% da roleta)
 *  - damage      : dano numérico (usado em comparações WHITE vs WHITE/GOLD)
 *  - statusEffect: efeito aplicado se for ROXO (ou BLUE com efeito especial)
 *  - statusTurns : quantos turnos o status dura
 *  - description : texto descritivo exibido na UI
 */
public class Move {

    private String name;
    private MoveColor color;
    private int percentage;  // soma de todos os moves do Pokémon deve ser 100
    private int damage;      // 0 se não causar dano numérico direto
    private StatusEffect statusEffect;
    private int statusTurns;
    private String description;

    public Move() {}

    public Move(String name, MoveColor color, int percentage, int damage,
                StatusEffect statusEffect, int statusTurns, String description) {
        this.name = name;
        this.color = color;
        this.percentage = percentage;
        this.damage = damage;
        this.statusEffect = statusEffect;
        this.statusTurns = statusTurns;
        this.description = description;
    }

    // ── Factory helpers ──────────────────────────────────────────────────────

    /** Cria um segmento de dano (Branco ou Ouro). */
    public static Move damage(String name, MoveColor color, int pct, int dmg, String desc) {
        return new Move(name, color, pct, dmg, StatusEffect.NONE, 0, desc);
    }

    /** Cria um segmento Miss (Vermelho). */
    public static Move miss(int pct) {
        return new Move("Miss", MoveColor.RED, pct, 0, StatusEffect.NONE, 0,
                "Erra o ataque — perde para qualquer movimento oposto.");
    }

    /** Cria um segmento Azul (cancela batalha). */
    public static Move dodge(String name, int pct, String desc) {
        return new Move(name, MoveColor.BLUE, pct, 0, StatusEffect.NONE, 0, desc);
    }

    /** Cria um segmento de status (Roxo). */
    public static Move status(String name, int pct, StatusEffect effect, int turns, String desc) {
        return new Move(name, MoveColor.PURPLE, pct, 0, effect, turns, desc);
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MoveColor getColor() { return color; }
    public void setColor(MoveColor color) { this.color = color; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public StatusEffect getStatusEffect() { return statusEffect; }
    public void setStatusEffect(StatusEffect statusEffect) { this.statusEffect = statusEffect; }

    public int getStatusTurns() { return statusTurns; }
    public void setStatusTurns(int statusTurns) { this.statusTurns = statusTurns; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("Move{%s, %s, %d%%, dmg=%d}", name, color, percentage, damage);
    }
}
