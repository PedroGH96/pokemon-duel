# Pokémon Duel — Backend (Spring Boot)

## Estrutura de Arquivos

```
backend/
├── pom.xml
└── src/
    ├── main/java/com/pokemonduel/
    │   ├── PokemonDuelApplication.java       ← ponto de entrada
    │   ├── model/
    │   │   ├── enums/
    │   │   │   ├── MoveColor.java            ← RED, WHITE, PURPLE, GOLD, BLUE
    │   │   │   ├── Rarity.java               ← C, UC, R, EX, UX
    │   │   │   ├── PokemonType.java          ← FOGO, AGUA, GRAMA...
    │   │   │   ├── StatusEffect.java         ← PARALYSIS, SLEEP, CONFUSION...
    │   │   │   └── NodeType.java             ← NORMAL, GOAL, BANK, PC, ENTRY
    │   │   ├── Move.java                     ← segmento de roleta
    │   │   ├── Pokemon.java                  ← dados do Pokémon + spin()
    │   │   ├── PokemonFigure.java            ← instância em partida
    │   │   ├── BoardNode.java                ← nó do tabuleiro
    │   │   ├── Board.java                    ← grafo + BFS/Dijkstra
    │   │   ├── BattleResult.java             ← resultado de batalha
    │   │   └── Player.java                   ← perfil do jogador
    │   ├── service/
    │   │   ├── PokemonCatalogService.java    ← 12 Pokémon definidos
    │   │   ├── BattleService.java            ← tabela de cores + spin()
    │   │   └── PlayerService.java            ← registro, deck, ELO
    │   ├── repository/
    │   │   └── PlayerRepository.java         ← persistência JSON
    │   └── controller/
    │       ├── PlayerController.java         ← /jogadores
    │       └── PokemonController.java        ← /figuras
    └── test/java/com/pokemonduel/
        └── service/BattleServiceTest.java    ← 19 testes da tabela de cores
```

## Como rodar

**Pré-requisitos:** Java 17+, Maven 3.8+

```bash
cd backend
mvn spring-boot:run
```

Servidor sobe em `http://localhost:8080`

## Endpoints disponíveis (Fase 1)

| Método | Endpoint                        | Descrição                             |
|--------|---------------------------------|---------------------------------------|
| POST   | /jogadores/registrar            | Registra jogador + 6 figuras iniciais |
| GET    | /jogadores                      | Lista todos os jogadores              |
| GET    | /jogadores/{id}                 | Perfil do jogador                     |
| GET    | /jogadores/{id}/figuras         | Figuras desbloqueadas (com roleta)    |
| PUT    | /jogadores/{id}/deck            | Atualiza deck (máx 6 figuras)        |
| GET    | /figuras                        | Catálogo completo de Pokémon          |
| GET    | /figuras/{id}                   | Dados de um Pokémon específico        |

### Exemplo: registrar jogador
```bash
curl -X POST http://localhost:8080/jogadores/registrar \
     -H "Content-Type: application/json" \
     -d '{"username": "AshKetchum"}'
```

### Exemplo: ver figuras do jogador
```bash
curl http://localhost:8080/jogadores/{id}/figuras
```

## Como rodar os testes

```bash
mvn test
```

Os 19 testes cobrem toda a tabela de cores 5×5 (BattleService)
e validam que todos os 12 Pokémon têm roletas com soma = 100%.

## Figuras iniciais (Comuns)
Todo jogador recebe ao se registrar:
- **Pikachu** (C) — PM 3, Elétrico
- **Eevee** (C) — PM 2, Normal
- **Machamp** (C) — PM 2, Lutador
- **Clefairy** (C) — PM 2, Normal
- **Charizard** (R) — PM 2, Fogo *(exceção: R para tornar o starter atraente)*
- **Blastoise** (R) — PM 2, Água

## Próxima fase (Fase 2)
- `GameState.java` — estado completo de uma partida
- `GameService.java` — movimentação, cerco, condições de vitória
- `RoomService.java` — salas públicas/privadas
- Endpoints `/salas` e `/partidas`
