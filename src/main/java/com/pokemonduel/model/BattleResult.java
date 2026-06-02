package com.pokemonduel.model;

import com.pokemonduel.model.enums.MoveColor;
import com.pokemonduel.model.enums.StatusEffect;

/**
 * Resultado de uma batalha entre duas figuras.
 *
 * Retornado pelo BattleService e enviado ao cliente via API.
 * O frontend usa este objeto para:
 *   1. Animar a roleta com os ângulos corretos
 *   2. Exibir o resultado (quem ganhou / K.O.)
 *   3. Aplicar efeitos de status
 */
public class BattleResult {

    public enum Outcome {
        ATTACKER_WINS,  // figura atacante vence → defensor vai ao P.C.
        DEFENDER_WINS,  // figura defensora vence → atacante vai ao P.C.
        DRAW,           // Blue vs Blue ou Red vs Red → nenhum efeito
        COMPARE_ATTACKER_WINS, // Compare e atacante tem dano maior
        COMPARE_DEFENDER_WINS, // Compare e defensor tem dano maior
        COMPARE_TIE     // Compare com dano igual → ambos permanecem
    }

    private Move attackerMove;
    private Move defenderMove;
    private Outcome outcome;

    // Para animação da roleta no frontend (ângulo onde parar)
    private double attackerWheelAngle;
    private double defenderWheelAngle;

    // Status aplicado (se houver)
    private StatusEffect statusApplied;
    private int statusTarget; // 1 ou 2 (jogador afetado)
    private int statusTurns;

    // Destiny Bond: se o perdedor tinha este status
    private boolean destinyBondTriggered;

    public BattleResult() {}

    // ── Factory ───────────────────────────────────────────────────────────────

    public static BattleResult of(Move attackerMove, Move defenderMove, Outcome outcome) {
        BattleResult r = new BattleResult();
        r.attackerMove = attackerMove;
        r.defenderMove = defenderMove;
        r.outcome = outcome;
        r.statusApplied = StatusEffect.NONE;
        return r;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean attackerGoesToPC() {
        return outcome == Outcome.DEFENDER_WINS || outcome == Outcome.COMPARE_DEFENDER_WINS;
    }

    public boolean defenderGoesToPC() {
        return outcome == Outcome.ATTACKER_WINS || outcome == Outcome.COMPARE_ATTACKER_WINS;
    }

    public boolean isCompare() {
        return outcome == Outcome.COMPARE_ATTACKER_WINS
                || outcome == Outcome.COMPARE_DEFENDER_WINS
                || outcome == Outcome.COMPARE_TIE;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Move getAttackerMove() { return attackerMove; }
    public void setAttackerMove(Move attackerMove) { this.attackerMove = attackerMove; }

    public Move getDefenderMove() { return defenderMove; }
    public void setDefenderMove(Move defenderMove) { this.defenderMove = defenderMove; }

    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }

    public double getAttackerWheelAngle() { return attackerWheelAngle; }
    public void setAttackerWheelAngle(double attackerWheelAngle) { this.attackerWheelAngle = attackerWheelAngle; }

    public double getDefenderWheelAngle() { return defenderWheelAngle; }
    public void setDefenderWheelAngle(double defenderWheelAngle) { this.defenderWheelAngle = defenderWheelAngle; }

    public StatusEffect getStatusApplied() { return statusApplied; }
    public void setStatusApplied(StatusEffect statusApplied) { this.statusApplied = statusApplied; }

    public int getStatusTarget() { return statusTarget; }
    public void setStatusTarget(int statusTarget) { this.statusTarget = statusTarget; }

    public int getStatusTurns() { return statusTurns; }
    public void setStatusTurns(int statusTurns) { this.statusTurns = statusTurns; }

    public boolean isDestinyBondTriggered() { return destinyBondTriggered; }
    public void setDestinyBondTriggered(boolean destinyBondTriggered) {
        this.destinyBondTriggered = destinyBondTriggered;
    }
}
