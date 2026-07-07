package com.pokemonduel.controller;

import com.pokemonduel.model.Pokemon;
import com.pokemonduel.service.PokemonCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST do catálogo de Pokémon.
 *
 * GET /figuras           → lista todos os Pokémon do catálogo
 * GET /figuras/{id}      → retorna dados completos de um Pokémon (com roleta)
 */
@RestController
@RequestMapping("/figuras")
@CrossOrigin(origins = "*")
public class PokemonController {

    private final PokemonCatalogService catalogService;

    public PokemonController(PokemonCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<Pokemon> findAll() {
        return catalogService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokemon> findById(@PathVariable String id) {
        Pokemon pokemon = catalogService.get(id);
        if (pokemon == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(pokemon);
    }
}
