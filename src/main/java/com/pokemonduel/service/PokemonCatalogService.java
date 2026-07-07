package com.pokemonduel.service;

import com.pokemonduel.model.Move;
import com.pokemonduel.model.Pokemon;
import com.pokemonduel.model.enums.MoveColor;
import com.pokemonduel.model.enums.PokemonType;
import com.pokemonduel.model.enums.Rarity;
import com.pokemonduel.model.enums.StatusEffect;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Catálogo central de todos os Pokémon do jogo.
 *
 * Como adicionar um Pokémon novo (após finalizar o documento):
 *  1. Crie um método privado buildNomeDoPokemon()
 *  2. Chame add(buildNomeDoPokemon()) dentro de loadAll()
 *  3. Rode o servidor — se a roleta não somar 100% ele avisa no startup
 *
 * Convenção de ID:
 *  - Letras minúsculas, sem espaços, sem acentos
 *  - Formas especiais: "kyurem-white", "keldeo-resolute"
 *  - dexId = número de exibição na UI (ID-1, ID-2...)
 *
 * Starters (figuras iniciais de todo jogador):
 *  Serão definidos em STARTER_IDS após o documento ser finalizado.
 *  Por ora a lista está vazia — adicione os IDs quando souber quais são.
 */
@Service
public class PokemonCatalogService {

    // TODO: preencher com os IDs das figuras iniciais após finalizar o documento
    public static final List<String> STARTER_IDS = List.of(
        // ex: "charmander", "squirtle", ...
    );

    private final Map<String, Pokemon> catalog = new LinkedHashMap<>();

    public PokemonCatalogService() {
        loadAll();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // loadAll — registra todos os Pokémon na ordem do documento
    // ═══════════════════════════════════════════════════════════════════════════
    private void loadAll() {

        // ── Geração 1 ─────────────────────────────────────────────────────────
        add(buildCharmander());      // ID-1

        validateAll();
    }

    private void add(Pokemon p) {
        catalog.put(p.getId(), p);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Definições dos Pokémon
    // Preencher conforme o documento for sendo finalizado.
    // ═══════════════════════════════════════════════════════════════════════════

    // ── Geração 1 ─────────────────────────────────────────────────────────────

    private Pokemon buildCharmander() {
        // Fonte: documento ID-1
        // Rarity: UC | Type: FOGO | PM: 3 | Special Ability: none
        return new Pokemon("charmander", 1, "Charmander", PokemonType.FOGO, Rarity.UC, 3)
            .addMove(Move.damage("Flame Tail",  MoveColor.WHITE,  24, 40,
                "Cauda de fogo — oponente não pode se mover no próximo turno."))
            .addMove(Move.status("Smokescreen", 28, StatusEffect.NONE, 0,
                "Fumaça — efeito a definir."))
            .addMove(Move.damage("Scratch",     MoveColor.WHITE,  32, 10,
                "Arranhão rápido, 10 de dano."))
            .addMove(Move.miss(16));
        // soma: 24+28+32+16 = 100 ✓
    }

    // TODO: private Pokemon buildCharmeleon() { ... }
    // TODO: private Pokemon buildCharizard()  { ... }
    // ... (continuar conforme o documento)

    // ═══════════════════════════════════════════════════════════════════════════
    // Validação — garante que toda roleta soma 100% ao subir o servidor
    // ═══════════════════════════════════════════════════════════════════════════
    private void validateAll() {
        for (Pokemon p : catalog.values()) {
            if (!p.isWheelValid()) {
                int sum = p.getMoves().stream().mapToInt(Move::getPercentage).sum();
                throw new IllegalStateException(
                    "Roleta inválida para " + p.getName()
                    + " (ID-" + p.getDexId() + ")"
                    + ": soma = " + sum + " (esperado 100)");
            }
        }
        System.out.println("[PokemonCatalog] " + catalog.size()
            + " Pokémon carregados. Todas as roletas válidas.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // API pública
    // ═══════════════════════════════════════════════════════════════════════════

    public Pokemon get(String id)          { return catalog.get(id); }
    public List<Pokemon> getAll()          { return new ArrayList<>(catalog.values()); }
    public boolean exists(String id)       { return catalog.containsKey(id); }

    public List<Pokemon> getStarters() {
        return STARTER_IDS.stream()
                .map(catalog::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
