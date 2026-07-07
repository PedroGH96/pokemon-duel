package com.pokemonduel.service;

import com.pokemonduel.model.Pokemon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PokemonCatalogServiceAdditionalTest {

    @Test
    void getAndStartersAndExists() {
        PokemonCatalogService svc = new PokemonCatalogService();

        assertTrue(svc.exists("pikachu"));
        Pokemon p = svc.get("pikachu");
        assertNotNull(p);
        assertEquals("Pikachu", p.getName());

        // Catálogo completo: ID-2 (Bulbasaur) a ID-162 (Genesect) = 161 figuras
        assertEquals(161, svc.getAll().size());
        assertEquals(PokemonCatalogService.STARTER_IDS.size(), svc.getStarters().size());

        // Todos os starters devem ser figuras Comuns (Rarity.C)
        for (Pokemon starter : svc.getStarters()) {
            assertEquals(com.pokemonduel.model.enums.Rarity.C, starter.getRarity(),
                "Starter " + starter.getName() + " deveria ser Rarity.C");
        }
    }

    @Test
    void everyPokemonHasValidWheelAndSprite() {
        PokemonCatalogService svc = new PokemonCatalogService();
        for (Pokemon p : svc.getAll()) {
            assertTrue(p.isWheelValid(), p.getName() + " tem roleta inválida");
            assertNotNull(p.getSpriteFile());
            assertTrue(p.getSpriteFile().endsWith(".png"));
        }
    }

    @Test
    void unknownPokemon() {
        PokemonCatalogService svc = new PokemonCatalogService();
        assertNull(svc.get("no-such-id"));
        assertFalse(svc.exists("no-such-id"));
    }
}
