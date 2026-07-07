package com.pokemonduel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do servidor Pokémon Duel.
 *
 * Para rodar:  mvn spring-boot:run
 * Porta padrão: 8080
 *
 * Endpoints disponíveis em http://localhost:8080/
 */
@SpringBootApplication
public class PokemonDuelApplication {
    public static void main(String[] args) {
        SpringApplication.run(PokemonDuelApplication.class, args);
    }
}
