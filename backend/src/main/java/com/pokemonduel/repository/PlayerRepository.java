package com.pokemonduel.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemonduel.model.Player;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Repositório de jogadores usando arquivo JSON como persistência.
 *
 * Arquivo: data/jogadores.json
 * Formato: lista de objetos Player serializados pelo Jackson.
 *
 * Em memória: HashMap<id, Player> para acesso O(1).
 * Ao modificar, salva o arquivo imediatamente (write-through).
 */
@Repository
public class PlayerRepository {

    private static final String FILE_PATH = "data/jogadores.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Player> store = new LinkedHashMap<>();

    public PlayerRepository() {
        loadFromFile();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public Player save(Player player) {
        store.put(player.getId(), player);
        persistToFile();
        return player;
    }

    public Optional<Player> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Player> findByUsername(String username) {
        return store.values().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<Player> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    // ── Persistência JSON ─────────────────────────────────────────────────────

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            return;
        }
        try {
            List<Player> players = mapper.readValue(file, new TypeReference<>() {});
            players.forEach(p -> store.put(p.getId(), p));
            System.out.println("[PlayerRepository] " + store.size() + " jogadores carregados.");
        } catch (IOException e) {
            System.err.println("[PlayerRepository] Erro ao carregar jogadores.json: " + e.getMessage());
        }
    }

    private void persistToFile() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(file, new ArrayList<>(store.values()));
        } catch (IOException e) {
            System.err.println("[PlayerRepository] Erro ao salvar jogadores.json: " + e.getMessage());
        }
    }
}
