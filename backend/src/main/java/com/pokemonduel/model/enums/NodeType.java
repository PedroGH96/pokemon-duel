package com.pokemonduel.model.enums;

/**
 * Tipo de cada nó no tabuleiro.
 *
 *  NORMAL   — casa comum, qualquer figura pode ocupar
 *  GOAL     — casa objetivo (uma por jogador); vencer chegando aqui
 *  BANK     — fora do tabuleiro; figuras aguardam antes de entrar
 *  PC       — Centro Pokémon; K.O. temporário
 *  ENTRY    — nós de canto (Pokébola); entrada do banco para o tabuleiro
 */
public enum NodeType {
    NORMAL,
    GOAL,
    BANK,
    PC,
    ENTRY
}
