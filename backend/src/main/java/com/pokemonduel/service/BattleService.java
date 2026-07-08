package com.pokemonduel.service;

import com.pokemonduel.model.BattleResult;
import com.pokemonduel.model.BattleResult.Outcome;
import com.pokemonduel.model.Move;
import com.pokemonduel.model.PokemonFigure;
import com.pokemonduel.model.enums.MoveColor;
import com.pokemonduel.model.enums.StatusEffect;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por resolver batalhas entre duas figuras.
 *
 * Lógica baseada na tabela de cores do Pokémon Duel:
 *
 *   ┌─────────┬────────┬─────────┬────────┬──────────┬──────────┐
 *   │Você\Opp │  RED   │  WHITE  │ PURPLE │   GOLD   │   BLUE   │
 *   ├─────────┼────────┼─────────┼────────┼──────────┼──────────┤
 *   │  RED    │ Empate │   Opp   │  Opp   │   Opp    │   Opp    │
 *   │  WHITE  │  Você  │ Compare │  Opp   │ Compare  │   Opp    │
 *   │ PURPLE  │  Você  │  Você   │Compare │   Opp    │   Opp    │
 *   │  GOLD   │  Você  │ Compare │  Você  │ Compare  │   Opp    │
 *   │  BLUE   │  Você  │  Você   │  Você  │   Você   │  Empate  │
 *   └─────────┴────────┴─────────┴────────┴──────────┴──────────┘
 *
 * "Compare" = dano numérico maior vence (White ou Gold com damage).
 *
 * O método resolve() também:
 *  - Calcula o ângulo da roleta para animação no frontend
 *  - Aplica efeitos de status (PURPLE)
 *  - Verifica Destiny Bond
 */
@Service
public class BattleService {

    /**
     * Resolve a batalha entre atacante e defensor.
     * Gira as roletas de ambos e aplica a tabela de cores.
     *
     * @param attacker figura que iniciou a batalha (se moveu para o nó)
     * @param defender figura que estava no nó e foi desafiada
     * @return BattleResult com o resultado completo
     */
    public BattleResult resolve(PokemonFigure attacker, PokemonFigure defender) {
        // 1. Girar as roletas
        Move aMove = attacker.getPokemon().spin();
        Move dMove = defender.getPokemon().spin();

        // 2. Calcular ângulos para animação (centro do segmento na roleta)
        double aAngle = computeWheelAngle(attacker, aMove);
        double dAngle = computeWheelAngle(defender, dMove);

        // 3. Determinar o outcome pela tabela de cores
        Outcome outcome = resolveColors(aMove.getColor(), dMove.getColor(),
                                        aMove.getDamage(), dMove.getDamage());

        // 4. Montar resultado
        BattleResult result = BattleResult.of(aMove, dMove, outcome);
        result.setAttackerWheelAngle(aAngle);
        result.setDefenderWheelAngle(dAngle);

        // 5. Aplicar efeito de status se PURPLE venceu
        applyStatusEffect(result, aMove, dMove, outcome);

        // 6. Verificar Destiny Bond
        checkDestinyBond(result, attacker, defender, outcome);

        // 7. Atualizar estado das figuras (K.O., status)
        applyOutcome(result, attacker, defender);

        return result;
    }

    // ── Tabela de cores ───────────────────────────────────────────────────────

    /**
     * Resolve o resultado pela tabela de cores.
     * "atacante" = quem girou aColor.
     */
    public Outcome resolveColors(MoveColor aColor, MoveColor dColor,
                                  int aDamage, int dDamage) {
        // Blue vence tudo (exceto Blue vs Blue = empate)
        if (aColor == MoveColor.BLUE && dColor == MoveColor.BLUE) return Outcome.DRAW;
        if (aColor == MoveColor.BLUE) return Outcome.ATTACKER_WINS;
        if (dColor == MoveColor.BLUE) return Outcome.DEFENDER_WINS;

        // Red perde de tudo (exceto Red vs Red = empate)
        if (aColor == MoveColor.RED && dColor == MoveColor.RED) return Outcome.DRAW;
        if (aColor == MoveColor.RED) return Outcome.DEFENDER_WINS;
        if (dColor == MoveColor.RED) return Outcome.ATTACKER_WINS;

        // Gold vs Purple → Gold vence
        if (aColor == MoveColor.GOLD && dColor == MoveColor.PURPLE) return Outcome.ATTACKER_WINS;
        if (aColor == MoveColor.PURPLE && dColor == MoveColor.GOLD) return Outcome.DEFENDER_WINS;

        // Purple vs White → Purple vence
        if (aColor == MoveColor.PURPLE && dColor == MoveColor.WHITE) return Outcome.ATTACKER_WINS;
        if (aColor == MoveColor.WHITE && dColor == MoveColor.PURPLE) return Outcome.DEFENDER_WINS;

        // Gold vs White / White vs White / Gold vs Gold / Purple vs Purple → Compare
        return resolveCompare(aDamage, dDamage);
    }

    private Outcome resolveCompare(int aDamage, int dDamage) {
        if (aDamage > dDamage) return Outcome.COMPARE_ATTACKER_WINS;
        if (dDamage > aDamage) return Outcome.COMPARE_DEFENDER_WINS;
        return Outcome.COMPARE_TIE;
    }

    // ── Ângulo da roleta (para animação no JavaFX) ────────────────────────────

    /**
     * Calcula o ângulo (em graus) que o ponteiro da roleta deve parar
     * para mostrar o segmento sorteado.
     *
     * A roleta começa em 0° (topo) e vai no sentido horário.
     * Retorna o ângulo do CENTRO do segmento sorteado.
     *
     * IMPORTANTE: usa o total real dos pesos (não assume 100),
     * pois algumas roletas somam 96, 88, etc. conforme o PDF original.
     */
    private double computeWheelAngle(PokemonFigure figure, Move drawnMove) {
        int total = figure.getPokemon().getMoves().stream()
                .mapToInt(Move::getPercentage).sum();
        if (total <= 0) return 720.0;

        double angle = 0;
        for (Move m : figure.getPokemon().getMoves()) {
            double segmentDegrees = (m.getPercentage() / (double) total) * 360.0;
            if (m == drawnMove) {
                // O cliente (BattleWheel.gd) mantém o ponteiro FIXO no topo e gira
                // o CONTEÚDO da roleta por "currentRotation" graus (sentido horário).
                // Isso significa que o segmento que termina sob o ponteiro é o de
                // posição (360 - currentRotation) — não currentRotation direto.
                // Por isso o ângulo enviado precisa ser o COMPLEMENTAR da posição
                // do segmento sorteado, senão o ponteiro pousa no segmento oposto.
                return 720.0 - angle - segmentDegrees / 2.0;
            }
            angle += segmentDegrees;
        }
        return 720.0;
    }

    // ── Status effects ────────────────────────────────────────────────────────

    private void applyStatusEffect(BattleResult result,
                                   Move aMove, Move dMove, Outcome outcome) {
        // Status de PURPLE vencedor é aplicado ao perdedor
        if (outcome == Outcome.ATTACKER_WINS
                && aMove.getColor() == MoveColor.PURPLE
                && aMove.getStatusEffect() != StatusEffect.NONE) {
            result.setStatusApplied(aMove.getStatusEffect());
            result.setStatusTarget(2); // aplica no defensor (jogador 2 neste contexto)
            result.setStatusTurns(aMove.getStatusTurns());
        } else if (outcome == Outcome.DEFENDER_WINS
                && dMove.getColor() == MoveColor.PURPLE
                && dMove.getStatusEffect() != StatusEffect.NONE) {
            result.setStatusApplied(dMove.getStatusEffect());
            result.setStatusTarget(1); // aplica no atacante
            result.setStatusTurns(dMove.getStatusTurns());
        }
    }

    // ── Destiny Bond ──────────────────────────────────────────────────────────

    private void checkDestinyBond(BattleResult result,
                                  PokemonFigure attacker, PokemonFigure defender,
                                  Outcome outcome) {
        if (result.attackerGoesToPC()
                && attacker.getActiveStatus() == StatusEffect.DESTINY_BOND) {
            result.setDestinyBondTriggered(true);
        }
        if (result.defenderGoesToPC()
                && defender.getActiveStatus() == StatusEffect.DESTINY_BOND) {
            result.setDestinyBondTriggered(true);
        }
    }

    // ── Aplicar outcome nas figuras ───────────────────────────────────────────

    private void applyOutcome(BattleResult result,
                              PokemonFigure attacker, PokemonFigure defender) {
        // K.O. direto
        if (result.defenderGoesToPC()) {
            defender.sendToPC();
            if (result.isDestinyBondTriggered()) attacker.sendToPC(); // Destiny Bond
        } else if (result.attackerGoesToPC()) {
            attacker.sendToPC();
            if (result.isDestinyBondTriggered()) defender.sendToPC();
        }

        // Aplicar status ao alvo
        if (result.getStatusApplied() != null
                && result.getStatusApplied() != StatusEffect.NONE) {
            PokemonFigure target = (result.getStatusTarget() == 2) ? defender : attacker;
            target.applyStatus(result.getStatusApplied(), result.getStatusTurns());
        }
    }

    // ── Cerco (Surround) ──────────────────────────────────────────────────────

    /**
     * Aplica K.O. por cerco sem batalha.
     * Chamado pelo GameService quando detecta cerco.
     */
    public void applySurroundKO(PokemonFigure surrounded) {
        surrounded.sendToPC();
    }
}
