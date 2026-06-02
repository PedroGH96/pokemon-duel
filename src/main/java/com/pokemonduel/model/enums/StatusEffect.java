package com.pokemonduel.model.enums;

/**
 * Efeitos de status aplicados por movimentos ROXO.
 * Cada efeito tem uma duração em turnos.
 */
public enum StatusEffect {
    NONE,           // Sem efeito
    PARALYSIS,      // Não pode mover nem atacar no próximo turno
    SLEEP,          // Pula N turnos dormindo
    CONFUSION,      // Movimento aleatório no tabuleiro
    IMMOBILIZED,    // Não pode mover, mas pode batalhar
    REDUCED_PM,     // PM reduzido em 1 por N turnos
    DESTINY_BOND    // Se for K.O., oponente também vai ao P.C.
}
