package com.pokemonduel.controller;

import com.pokemonduel.model.Room;
import com.pokemonduel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoints de salas multiplayer.
 *
 *  GET  /salas               → lista salas públicas disponíveis
 *  GET  /salas/{id}          → detalhes de uma sala
 *  POST /salas               → cria sala (corpo: {nome, privada, jogadorId})
 *  POST /salas/{id}/entrar   → entra na sala (corpo: {jogadorId})
 */
@RestController
@RequestMapping("/salas")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired private RoomService roomService;

    @GetMapping
    public List<Room> listar() {
        return roomService.listPublicRooms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> get(@PathVariable String id) {
        return roomService.findRoom(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> criar(@RequestBody Map<String, Object> body) {
        String nome      = (String) body.getOrDefault("nome", "Sala");
        boolean privada  = Boolean.TRUE.equals(body.get("privada"));
        String jogadorId = (String) body.get("jogadorId");

        if (jogadorId == null || jogadorId.isBlank())
            return ResponseEntity.badRequest().build();

        Room room = roomService.createRoom(nome, privada, jogadorId);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{id}/entrar")
    public ResponseEntity<?> entrar(@PathVariable String id,
                                     @RequestBody Map<String, String> body) {
        String jogadorId = body.get("jogadorId");
        if (jogadorId == null || jogadorId.isBlank())
            return ResponseEntity.badRequest().body("jogadorId é obrigatório");

        Optional<Room> result = roomService.joinRoom(id, jogadorId);
        if (result.isEmpty())
            return ResponseEntity.badRequest().body("Sala cheia ou não encontrada");

        return ResponseEntity.ok(result.get());
    }
}
