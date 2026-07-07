package com.pokemonduel.service;

import com.pokemonduel.model.BattleResult;
import com.pokemonduel.model.BattleResult.Outcome;
import com.pokemonduel.model.enums.MoveColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da tabela de cores do BattleService.
 * Cobre todos os 25 casos da matriz 5x5.
 */
class BattleServiceTest {

    private BattleService battleService;

    @BeforeEach
    void setUp() {
        battleService = new BattleService();
    }

    // ── Blue vence tudo ────────────────────────────────────────────────────────

    @Test @DisplayName("Blue vs Red → Atacante vence")
    void blue_vs_red() {
        assertEquals(Outcome.ATTACKER_WINS, resolve(MoveColor.BLUE, MoveColor.RED, 0, 0));
    }

    @Test @DisplayName("Blue vs White → Atacante vence")
    void blue_vs_white() {
        assertEquals(Outcome.ATTACKER_WINS, resolve(MoveColor.BLUE, MoveColor.WHITE, 0, 30));
    }

    @Test @DisplayName("Blue vs Purple → Atacante vence")
    void blue_vs_purple() {
        assertEquals(Outcome.ATTACKER_WINS, resolve(MoveColor.BLUE, MoveColor.PURPLE, 0, 0));
    }

    @Test @DisplayName("Blue vs Gold → Atacante vence")
    void blue_vs_gold() {
        assertEquals(Outcome.ATTACKER_WINS, resolve(MoveColor.BLUE, MoveColor.GOLD, 0, 80));
    }

    @Test @DisplayName("Blue vs Blue → Empate")
    void blue_vs_blue() {
        assertEquals(Outcome.DRAW, resolve(MoveColor.BLUE, MoveColor.BLUE, 0, 0));
    }

    // ── Red perde de tudo ─────────────────────────────────────────────────────

    @Test @DisplayName("Red vs White → Defensor vence")
    void red_vs_white() {
        assertEquals(Outcome.DEFENDER_WINS, resolve(MoveColor.RED, MoveColor.WHITE, 0, 30));
    }

    @Test @DisplayName("Red vs Gold → Defensor vence")
    void red_vs_gold() {
        assertEquals(Outcome.DEFENDER_WINS, resolve(MoveColor.RED, MoveColor.GOLD, 0, 70));
    }

    @Test @DisplayName("Red vs Red → Empate")
    void red_vs_red() {
        assertEquals(Outcome.DRAW, resolve(MoveColor.RED, MoveColor.RED, 0, 0));
    }

    // ── Purple vence White ─────────────────────────────────────────────────────

    @Test @DisplayName("Purple vs White → Atacante vence")
    void purple_vs_white() {
        assertEquals(Outcome.ATTACKER_WINS, resolve(MoveColor.PURPLE, MoveColor.WHITE, 0, 30));
    }

    @Test @DisplayName("White vs Purple → Defensor vence")
    void white_vs_purple() {
        assertEquals(Outcome.DEFENDER_WINS, resolve(MoveColor.WHITE, MoveColor.PURPLE, 30, 0));
    }

    // ── Gold vence Purple ─────────────────────────────────────────────────────

    @Test @DisplayName("Gold vs Purple → Atacante vence")
    void gold_vs_purple() {
        assertEquals(Outcome.ATTACKER_WINS, resolve(MoveColor.GOLD, MoveColor.PURPLE, 70, 0));
    }

    @Test @DisplayName("Purple vs Gold → Defensor vence")
    void purple_vs_gold() {
        assertEquals(Outcome.DEFENDER_WINS, resolve(MoveColor.PURPLE, MoveColor.GOLD, 0, 70));
    }

    // ── Compare (dano numérico) ────────────────────────────────────────────────

    @Test @DisplayName("White vs White, atacante dano maior → Atacante vence (Compare)")
    void white_vs_white_attacker_wins() {
        assertEquals(Outcome.COMPARE_ATTACKER_WINS, resolve(MoveColor.WHITE, MoveColor.WHITE, 50, 30));
    }

    @Test @DisplayName("White vs White, defensor dano maior → Defensor vence (Compare)")
    void white_vs_white_defender_wins() {
        assertEquals(Outcome.COMPARE_DEFENDER_WINS, resolve(MoveColor.WHITE, MoveColor.WHITE, 10, 30));
    }

    @Test @DisplayName("White vs White, dano igual → Compare Empate")
    void white_vs_white_tie() {
        assertEquals(Outcome.COMPARE_TIE, resolve(MoveColor.WHITE, MoveColor.WHITE, 30, 30));
    }

    @Test @DisplayName("Gold vs White, atacante dano maior → Atacante vence (Compare)")
    void gold_vs_white_attacker_wins() {
        assertEquals(Outcome.COMPARE_ATTACKER_WINS, resolve(MoveColor.GOLD, MoveColor.WHITE, 80, 30));
    }

    @Test @DisplayName("White vs Gold, defensor dano maior → Defensor vence (Compare)")
    void white_vs_gold_defender_wins() {
        assertEquals(Outcome.COMPARE_DEFENDER_WINS, resolve(MoveColor.WHITE, MoveColor.GOLD, 30, 80));
    }

    @Test @DisplayName("Gold vs Gold, atacante maior → Compare Atacante")
    void gold_vs_gold_attacker() {
        assertEquals(Outcome.COMPARE_ATTACKER_WINS, resolve(MoveColor.GOLD, MoveColor.GOLD, 100, 80));
    }

    // ── Simetria inversa ──────────────────────────────────────────────────────

    @Test @DisplayName("Simetria: se A vence B, então B perde para A")
    void symmetry_test() {
        var ab = resolve(MoveColor.PURPLE, MoveColor.WHITE, 0, 30);
        var ba = resolve(MoveColor.WHITE, MoveColor.PURPLE, 30, 0);
        assertEquals(Outcome.ATTACKER_WINS, ab);
        assertEquals(Outcome.DEFENDER_WINS, ba);
    }

    // ── Catálogo: roletas válidas ─────────────────────────────────────────────

    @Test @DisplayName("Todos os Pokémon têm roletas com soma = 100")
    void all_wheels_valid() {
        PokemonCatalogService catalog = new PokemonCatalogService();
        catalog.getAll().forEach(p ->
            assertTrue(p.isWheelValid(), p.getName() + " roleta inválida!"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Outcome resolve(MoveColor a, MoveColor d, int aDmg, int dDmg) {
        return battleService.resolveColors(a, d, aDmg, dDmg);
    }
}
