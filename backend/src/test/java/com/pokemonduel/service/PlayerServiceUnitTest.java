package com.pokemonduel.service;

import com.pokemonduel.model.Player;
import com.pokemonduel.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceUnitTest {

    PlayerRepository repository;
    PokemonCatalogService catalog;
    PlayerService service;

    @BeforeEach
    void setup() {
        // Use real implementations to avoid inline-mock incompatibilities with Java 25
        this.repository = new PlayerRepository();
        this.catalog = new PokemonCatalogService();
        this.service = new PlayerService(repository, catalog);
    }

    @Test
    void registerSuccess() {
        Player p = service.register("john");

        assertNotNull(p.getId());
        assertEquals("john", p.getUsername());
        assertEquals(6, p.getUnlockedPokemonIds().size());
    }

    @Test
    void registerDuplicateUsername() {
        // persist a player with username to cause duplicate
        Player existing = new Player(java.util.UUID.randomUUID().toString(), "john");
        repository.save(existing);

        assertThrows(IllegalArgumentException.class, () -> service.register("john"));
    }

    @Test
    void updateDeckValid() {
        Player player = new Player("id", "john");
        player.setUnlockedPokemonIds(new ArrayList<>(PokemonCatalogService.STARTER_IDS));
        player.setDeckIds(new ArrayList<>(PokemonCatalogService.STARTER_IDS));
        repository.save(player);

        List<String> newDeck = List.of("pikachu", "eevee");
        Player updated = service.updateDeck("id", newDeck);

        assertEquals(newDeck, updated.getDeckIds());
    }

    @Test
    void updateDeckTooMany() {
        List<String> seven = Arrays.asList("a","b","c","d","e","f","g");
        assertThrows(IllegalArgumentException.class, () -> service.updateDeck("id", seven));
    }

    @Test
    void unlockPokemonAddsIfMissing() {
        Player player = new Player("id", "john");
        player.setUnlockedPokemonIds(new ArrayList<>(List.of("pikachu")));
        repository.save(player);

        Player res = service.unlockPokemon("id", "eevee");
        assertTrue(res.getUnlockedPokemonIds().contains("eevee"));
    }

    @Test
    void updateRatingWinIncrementsWins() {
        Player player = new Player("id", "john");
        player.setRating(1000);
        player.setWins(0);
        player.setLosses(0);
        repository.save(player);

        service.updateRating("id", true, 1100);

        assertEquals(1, player.getWins());
        assertTrue(player.getRating() >= 100);
    }
}
