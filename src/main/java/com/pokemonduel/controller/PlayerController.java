package com.pokemonduel.controller;

import com.pokemonduel.model.Player;
import com.pokemonduel.model.Pokemon;
import com.pokemonduel.service.PlayerService;
import com.pokemonduel.service.PokemonCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints REST para gerenciamento de jogadores.
 *
 * POST /jogadores/registrar         → registra e retorna Player com figuras iniciais
 * GET  /jogadores                   → lista todos os jogadores
 * GET  /jogadores/{id}              → retorna perfil do jogador
 * GET  /jogadores/{id}/figuras      → retorna as figuras desbloqueadas (com dados completos)
 * PUT  /jogadores/{id}/deck         → atualiza o deck do jogador
 */
@RestController
@RequestMapping("/jogadores")
@CrossOrigin(origins = "*") // permite requisições do JavaFX em localhost
public class PlayerController {

    private final PlayerService       playerService;
    private final PokemonCatalogService catalogService;

    public PlayerController(PlayerService playerService,
                            PokemonCatalogService catalogService) {
        this.playerService   = playerService;
        this.catalogService  = catalogService;
    }

    // POST /jogadores/registrar
    @PostMapping("/registrar")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            Player player = playerService.register(username);
            return ResponseEntity.status(HttpStatus.CREATED).body(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // GET /jogadores
    @GetMapping
    public List<Player> findAll() {
        return playerService.findAll();
    }

    // GET /jogadores/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(playerService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /jogadores/{id}/figuras — retorna dados completos dos Pokémon do jogador
    @GetMapping("/{id}/figuras")
    public ResponseEntity<?> getFiguras(@PathVariable String id) {
        try {
            Player player = playerService.findById(id);
            List<Pokemon> figuras = player.getUnlockedPokemonIds().stream()
                    .map(catalogService::get)
                    .filter(p -> p != null)
                    .toList();
            return ResponseEntity.ok(figuras);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /jogadores/{id}/deck
    // body: { "pokemonIds": ["pikachu", "charizard", ...] }
    @PutMapping("/{id}/deck")
    public ResponseEntity<?> updateDeck(@PathVariable String id,
                                        @RequestBody Map<String, List<String>> body) {
        try {
            List<String> ids = body.get("pokemonIds");
            Player updated = playerService.updateDeck(id, ids);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
