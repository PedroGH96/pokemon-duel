package com.pokemonduel.model.enums;

/**
 * Cor de um segmento da roleta.
 * Determina o resultado da batalha conforme a tabela de cores.
 *
 *  Hierarquia: BLUE > GOLD > PURPLE > WHITE > RED
 *  - BLUE   vence todos, mas não causa dano direto (cancela a batalha)
 *  - GOLD   vence PURPLE e WHITE (dano numérico alto)
 *  - PURPLE vence WHITE (aplica efeito de status)
 *  - WHITE  dano numérico básico
 *  - RED    perde de todos ("Miss")
 *
 *  Empates especiais: BLUE vs BLUE = nenhum efeito
 *                     WHITE vs GOLD / WHITE vs WHITE = Compare (dano maior vence)
 *                     GOLD  vs GOLD / PURPLE vs PURPLE = Compare
 */
public enum MoveColor {
    RED,
    WHITE,
    PURPLE,
    GOLD,
    BLUE
}
