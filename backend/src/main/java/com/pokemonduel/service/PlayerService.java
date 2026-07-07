package com.pokemonduel.service;

import com.pokemonduel.model.Player;
import com.pokemonduel.model.Pokemon;
import com.pokemonduel.model.enums.Rarity;
import com.pokemonduel.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lógica de negócio relacionada a jogadores.
 */
@Service
public class PlayerService {

    private final PlayerRepository repository;
    private final PokemonCatalogService catalog;

    public PlayerService(PlayerRepository repository, PokemonCatalogService catalog) {
        this.repository = repository;
        this.catalog    = catalog;
    }

    /**
     * Registra um novo jogador e concede as 6 figuras iniciais.
     * Lança IllegalArgumentException se o username já existir.
     */
    public Player register(String username) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username não pode ser vazio.");
        if (repository.existsByUsername(username))
            throw new IllegalArgumentException("Username '" + username + "' já está em uso.");

        Player player = new Player(UUID.randomUUID().toString(), username);

        // Conceder as 6 figuras iniciais comuns
        player.setUnlockedPokemonIds(new ArrayList<>(PokemonCatalogService.STARTER_IDS));
        player.setDeckIds(new ArrayList<>(PokemonCatalogService.STARTER_IDS));

        return repository.save(player);
    }

    public Player findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado: " + id));
    }

    public java.util.Optional<Player> findByIdOptional(String id) {
        return repository.findById(id);
    }

    public List<Player> findAll() {
        return repository.findAll();
    }

    /**
     * Atualiza o deck do jogador (máx. 6 figuras, todas desbloqueadas por ele).
     */
    public Player updateDeck(String playerId, List<String> pokemonIds) {
        if (pokemonIds.size() > 6)
            throw new IllegalArgumentException("O deck pode ter no máximo 6 figuras.");

        Player player = findById(playerId);

        for (String pid : pokemonIds) {
            if (!catalog.exists(pid))
                throw new IllegalArgumentException("Pokémon desconhecido: " + pid);
            if (!player.getUnlockedPokemonIds().contains(pid))
                throw new IllegalArgumentException("Jogador não possui: " + pid);
        }

        player.setDeckIds(new ArrayList<>(pokemonIds));
        return repository.save(player);
    }

    /**
     * Adiciona uma figura à coleção do jogador (ex: drop de caixa).
     */
    public Player unlockPokemon(String playerId, String pokemonId) {
        Player player = findById(playerId);
        if (!player.getUnlockedPokemonIds().contains(pokemonId)) {
            player.getUnlockedPokemonIds().add(pokemonId);
            repository.save(player);
        }
        return player;
    }

    /**
     * Atualiza o rating ELO do jogador após uma partida.
     * Implementação simplificada do Elo.
     */
    public void updateRating(String playerId, boolean won, int opponentRating) {
        Player player = findById(playerId);
        double expected = 1.0 / (1.0 + Math.pow(10, (opponentRating - player.getRating()) / 400.0));
        double actual   = won ? 1.0 : 0.0;
        int    k        = 32; // K-factor
        int    newRating = (int) Math.round(player.getRating() + k * (actual - expected));

        player.setRating(Math.max(100, newRating)); // mínimo de 100
        if (won) player.setWins(player.getWins() + 1);
        else     player.setLosses(player.getLosses() + 1);

        repository.save(player);
    }

    // ── Recompensas do Modo Solo (caixas) ───────────────────────────────────────

    /** Peso de drop por raridade (soma = 100), conforme Rarity. */
    private static final Map<Rarity, Integer> DROP_WEIGHTS = new EnumMap<>(Rarity.class);
    static {
        DROP_WEIGHTS.put(Rarity.C,  60);
        DROP_WEIGHTS.put(Rarity.UC, 25);
        DROP_WEIGHTS.put(Rarity.R,  10);
        DROP_WEIGHTS.put(Rarity.EX,  4);
        DROP_WEIGHTS.put(Rarity.UX,  1);
    }
    private final Random random = new Random();

    /** Resultado de uma abertura de caixa de recompensa. */
    public static class RewardResult {
        public String pokemonId;
        public String pokemonName;
        public boolean shiny;   // true = o jogador já possuía essa figura, virou shiny
        public boolean isNew;   // true = primeira vez que o jogador recebe essa figura
    }

    /**
     * Sorteia uma figura aleatória ponderada por raridade (Rarity.DROP_WEIGHTS)
     * dentre todo o catálogo.
     */
    private Pokemon rollByRarity() {
        int roll = random.nextInt(100);
        int acc = 0;
        Rarity chosen = Rarity.C;
        for (Map.Entry<Rarity, Integer> e : DROP_WEIGHTS.entrySet()) {
            acc += e.getValue();
            if (roll < acc) { chosen = e.getKey(); break; }
        }
        final Rarity finalChosen = chosen;

        List<Pokemon> pool = catalog.getAll().stream()
                .filter(p -> p.getRarity() == finalChosen)
                .collect(Collectors.toList());
        if (pool.isEmpty()) pool = catalog.getAll(); // fallback de segurança

        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * Abre uma caixa de recompensa para o jogador (ex: ao concluir um nível
     * do Modo Solo). Regra de shiny: se o jogador já possui a figura sorteada
     * (na forma normal), a mesma sorteio concede a versão shiny dela em vez
     * de uma figura nova. Se já tiver a shiny também, não há efeito adicional
     * (retorna isNew=false, shiny=true) — a caixa "não desperdiça" o sorteio,
     * apenas não há nada a mais para conceder por ora.
     *
     * Quando playerId não corresponde a um jogador registrado (ex: sessão solo
     * anônima), o sorteio ainda é feito e retornado — o cliente Godot decide
     * como rastrear a posse localmente (GameState.unlocked_pokemon_ids).
     */
    public RewardResult openRewardBox(String playerId) {
        Pokemon prize = rollByRarity();

        RewardResult result = new RewardResult();
        result.pokemonId = prize.getId();
        result.pokemonName = prize.getName();

        findByIdOptional(playerId).ifPresentOrElse(player -> {
            boolean owned = player.getUnlockedPokemonIds().contains(prize.getId());
            boolean ownedShiny = player.getShinyPokemonIds().contains(prize.getId());

            if (!owned) {
                player.getUnlockedPokemonIds().add(prize.getId());
                result.isNew = true;
                result.shiny = false;
            } else if (!ownedShiny) {
                player.getShinyPokemonIds().add(prize.getId());
                result.isNew = false;
                result.shiny = true;
            } else {
                result.isNew = false;
                result.shiny = true; // já era shiny
            }
            repository.save(player);
        }, () -> {
            // Jogador anônimo/solo sem registro — apenas informa o sorteio.
            result.isNew = true;
            result.shiny = false;
        });

        return result;
    }

    /** Verifica se o jogador (se registrado) possui a versão shiny desta figura. */
    public boolean hasShiny(String playerId, String pokemonId) {
        return findByIdOptional(playerId)
                .map(p -> p.getShinyPokemonIds().contains(pokemonId))
                .orElse(false);
    }
}
