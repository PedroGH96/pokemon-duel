package com.pokemonduel.model.enums;

/**
 * Efeitos de status que um Move PURPLE pode aplicar ao oponente quando vence a batalha.
 *
 *  NONE          — sem efeito (a maioria dos ataques Branco/Ouro/Azul)
 *  PARALYSIS     — Pokémon paralisado (não pode atacar até se recuperar)
 *  SLEEP         — Pokémon dorme (pula turnos até despertar)
 *  CONFUSION     — Pokémon confuso
 *  POISON        — Pokémon envenenado (dano gradual leve)
 *  NOXIOUS       — Pokémon tóxico (dano gradual mais forte que Poison)
 *  BURN          — Pokémon queimado (dano gradual + penalidades)
 *  FROZEN        — Pokémon congelado (não pode agir até descongelar)
 *  IMMOBILIZED   — Pokémon recebe "Wait" (pula N turnos, ver statusTurns)
 *  REDUCED_PM    — Pokémon recebe marcador MP-N (Pontos de Movimento reduzidos)
 *  DESTINY_BOND  — efeito de "laço do destino" (reservado para mecânicas futuras)
 */
public enum StatusEffect {
    NONE,
    PARALYSIS,
    SLEEP,
    CONFUSION,
    POISON,
    NOXIOUS,
    BURN,
    FROZEN,
    IMMOBILIZED,
    REDUCED_PM,
    DESTINY_BOND
}
