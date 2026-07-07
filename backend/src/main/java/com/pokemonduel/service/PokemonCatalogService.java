package com.pokemonduel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemonduel.model.Move;
import com.pokemonduel.model.Pokemon;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Catálogo central de todos os Pokémon do jogo.
 *
 * Os dados (161 figuras, ID-2 a ID-162) são carregados a partir do recurso
 * "/data/pokemon-catalog.json" em tempo de inicialização, usando Jackson.
 * Esse arquivo é gerado a partir da Lista_.pdf (ver build_catalog.py na raiz
 * do projeto) e contém, para cada Pokémon: tipo(s), raridade, PM, habilidade
 * especial e os segmentos completos da roleta de batalha.
 *
 * Como adicionar/corrigir um Pokémon:
 *  1. Edite build_catalog.py (fonte da verdade legível) e rode-o
 *     (gera pokemon-catalog.json na raiz do projeto)
 *  2. Rode export_for_java.py para regenerar
 *     src/main/resources/data/pokemon-catalog.json
 *  3. Reinicie o servidor — se alguma roleta estiver vazia, ele falha no
 *     startup com o nome do Pokémon problemático.
 *
 * Convenção de ID:
 *  - Letras minúsculas, sem espaços, sem acentos
 *  - Formas especiais: "kyurem-white", "kyurem-black", "keldeo-resolute"
 *  - dexId = número de exibição na UI (ID-2, ID-3... ID-162)
 */
@Service
public class PokemonCatalogService {

    private static final String CATALOG_RESOURCE = "/data/pokemon-catalog.json";

    /**
     * Figuras recebidas por todo jogador no início do jogo (deck/coleção
     * inicial padrão), conforme definido no projeto.
     */
    public static final List<String> STARTER_IDS = List.of(
        "bulbasaur",  // Grama/Veneno
        "charmander", // Fogo
        "squirtle",   // Água
        "pikachu",    // Elétrico
        "machop",     // Lutador
        "eevee"       // Normal
    );

    private final Map<String, Pokemon> catalog = new LinkedHashMap<>();

    public PokemonCatalogService() {
        loadAll();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // loadAll — lê o catálogo completo do JSON em resources/data
    // ═══════════════════════════════════════════════════════════════════════════
    private void loadAll() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = getClass().getResourceAsStream(CATALOG_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(
                    "Recurso não encontrado no classpath: " + CATALOG_RESOURCE);
            }
            List<Pokemon> all = mapper.readValue(in, mapper.getTypeFactory()
                    .constructCollectionType(List.class, Pokemon.class));
            for (Pokemon p : all) {
                // spriteFile não está no JSON — é derivado do id em tempo de carga
                if (p.getSpriteFile() == null || p.getSpriteFile().isBlank()) {
                    p.setSpriteFile(p.getId() + ".png");
                }
                // name também pode vir null — capitaliza o id como fallback
                if (p.getName() == null || p.getName().isBlank()) {
                    String id = p.getId();
                    p.setName(id.substring(0, 1).toUpperCase() + id.substring(1));
                }
                catalog.put(p.getId(), p);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao carregar " + CATALOG_RESOURCE, e);
        }

        validateAll();
        validateStarters();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Validação — garante que toda roleta tem peso positivo ao subir o servidor
    // ═══════════════════════════════════════════════════════════════════════════
    private void validateAll() {
        for (Pokemon p : catalog.values()) {
            if (!p.isWheelValid()) {
                throw new IllegalStateException(
                    "Roleta inválida para " + p.getName()
                    + " (ID-" + p.getDexId() + ")"
                    + ": soma = " + p.getWheelTotal() + " (esperado > 0)");
            }
        }
        System.out.println("[PokemonCatalog] " + catalog.size()
            + " Pokémon carregados. Todas as roletas válidas.");
    }

    private void validateStarters() {
        for (String id : STARTER_IDS) {
            if (!catalog.containsKey(id)) {
                throw new IllegalStateException(
                    "STARTER_IDS contém um id desconhecido: " + id);
            }
        }
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
