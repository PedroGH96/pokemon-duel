package com.pokemonduel.service;

import com.pokemonduel.model.Player;
import com.pokemonduel.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
}
