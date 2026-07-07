package com.pokemonduel.model;

import com.pokemonduel.model.enums.StatusEffect;

/**
 * Instância de um Pokémon durante uma partida.
 *
 * Diferente de Pokemon (o catálogo/dados), PokemonFigure rastreia
 * o estado dinâmico em jogo: posição no tabuleiro, status, etc.
 *
 * Estados possíveis:
 *   BENCH   → no banco, aguardando entrar
 *   ACTIVE  → no tabuleiro
 *   PC      → K.O., no Centro Pokémon
 *   WAITING → no P.C., aguardando retornar ao banco
 */
public class PokemonFigure {

    public enum FigureState { BENCH, ACTIVE, PC }

    private String figureId;       // UUID único desta instância na partida
    private Pokemon pokemon;       // dados do Pokémon (roleta, PM, etc.)
    private int owner;             // 1 ou 2
    private int nodeId;            // posição atual no tabuleiro (-1 se no banco/PC)
    private FigureState state;

    // Status temporário
    private StatusEffect activeStatus;
    private int statusTurnsLeft;   // turnos restantes do status

    // PM modificado (ex: Dragon Dance +1, Icy Wind -1)
    private int pmBonus;           // somado ao pokemon.getPm() neste turno

    // Variante shiny — puramente visual (troca o sprite de batalha para
    // battle/front|back/shiny/{id}.png no cliente). Não existe ícone shiny
    // para o tabuleiro; o efeito só aparece na transição de batalha.
    private boolean shiny;

    public PokemonFigure() {}

    public PokemonFigure(String figureId, Pokemon pokemon, int owner) {
        this(figureId, pokemon, owner, false);
    }

    public PokemonFigure(String figureId, Pokemon pokemon, int owner, boolean shiny) {
        this.figureId = figureId;
        this.pokemon = pokemon;
        this.owner = owner;
        this.nodeId = -1;
        this.state = FigureState.BENCH;
        this.activeStatus = StatusEffect.NONE;
        this.statusTurnsLeft = 0;
        this.pmBonus = 0;
        this.shiny = shiny;
    }

    // ── Status helpers ────────────────────────────────────────────────────────

    public void applyStatus(StatusEffect effect, int turns) {
        this.activeStatus = effect;
        this.statusTurnsLeft = turns;
    }

    public void clearStatus() {
        this.activeStatus = StatusEffect.NONE;
        this.statusTurnsLeft = 0;
    }

    /** Decrementa o contador de status ao final do turno. */
    public void tickStatus() {
        if (statusTurnsLeft > 0) {
            statusTurnsLeft--;
            if (statusTurnsLeft == 0) clearStatus();
        }
    }

    public boolean hasStatus() {
        return activeStatus != null && activeStatus != StatusEffect.NONE;
    }

    /** PM efetivo neste turno = base + bônus temporário. */
    public int getEffectivePm() {
        int effective = pokemon.getPm() + pmBonus;
        if (activeStatus == StatusEffect.REDUCED_PM) effective = Math.max(0, effective - 1);
        return Math.max(0, effective);
    }

    public boolean canMove() {
        return state == FigureState.ACTIVE
                && activeStatus != StatusEffect.PARALYSIS
                && activeStatus != StatusEffect.IMMOBILIZED
                && activeStatus != StatusEffect.SLEEP;
    }

    // ── K.O. / retorno ────────────────────────────────────────────────────────

    public void sendToPC() {
        this.state = FigureState.PC;
        this.nodeId = -1;
        clearStatus();
    }

    public void returnToBench() {
        this.state = FigureState.BENCH;
        this.nodeId = -1;
        this.pmBonus = 0;
    }

    public void enterBoard(int nodeId) {
        this.state = FigureState.ACTIVE;
        this.nodeId = nodeId;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getFigureId() { return figureId; }
    public void setFigureId(String figureId) { this.figureId = figureId; }

    public Pokemon getPokemon() { return pokemon; }
    public void setPokemon(Pokemon pokemon) { this.pokemon = pokemon; }

    public int getOwner() { return owner; }
    public void setOwner(int owner) { this.owner = owner; }

    public int getNodeId() { return nodeId; }
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }

    public FigureState getState() { return state; }
    public void setState(FigureState state) { this.state = state; }

    public StatusEffect getActiveStatus() { return activeStatus; }
    public void setActiveStatus(StatusEffect activeStatus) { this.activeStatus = activeStatus; }

    public int getStatusTurnsLeft() { return statusTurnsLeft; }
    public void setStatusTurnsLeft(int statusTurnsLeft) { this.statusTurnsLeft = statusTurnsLeft; }

    public int getPmBonus() { return pmBonus; }
    public void setPmBonus(int pmBonus) { this.pmBonus = pmBonus; }

    public boolean isShiny() { return shiny; }
    public void setShiny(boolean shiny) { this.shiny = shiny; }

    @Override
    public String toString() {
        return String.format("Figure{%s @ node=%d, state=%s, status=%s}",
                pokemon.getName(), nodeId, state, activeStatus);
    }
}
