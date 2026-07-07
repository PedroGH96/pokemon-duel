# Pokémon Duel — Backend (Spring Boot)

## Estrutura de Arquivos

```
backend/
├── pom.xml
├── build_catalog.py                          ← gera o catálogo (fonte legível)
└── src/
    ├── main/java/com/pokemonduel/
    │   ├── PokemonDuelApplication.java       ← ponto de entrada
    │   ├── model/
    │   │   ├── enums/
    │   │   │   ├── MoveColor.java            ← RED, WHITE, PURPLE, GOLD, BLUE
    │   │   │   ├── Rarity.java               ← C, UC, R, EX, UX
    │   │   │   ├── PokemonType.java          ← FOGO, AGUA, GRAMA...
    │   │   │   ├── StatusEffect.java         ← PARALYSIS, SLEEP, POISON, BURN, FROZEN...
    │   │   │   └── NodeType.java             ← NORMAL, GOAL, BANK, PC, ENTRY
    │   │   ├── Move.java                     ← segmento de roleta
    │   │   ├── Pokemon.java                  ← dados do Pokémon (type/type2) + spin()
    │   │   ├── PokemonFigure.java            ← instância em partida
    │   │   ├── BoardNode.java                ← nó do tabuleiro
    │   │   ├── Board.java                    ← grafo + BFS/Dijkstra
    │   │   ├── BattleResult.java             ← resultado de batalha
    │   │   └── Player.java                   ← perfil do jogador
    │   ├── service/
    │   │   ├── PokemonCatalogService.java    ← carrega 161 Pokémon de data/pokemon-catalog.json
    │   │   ├── BattleService.java            ← tabela de cores + spin()
    │   │   └── PlayerService.java            ← registro, deck, ELO
    │   ├── repository/
    │   │   └── PlayerRepository.java         ← persistência JSON
    │   ├── resources/
    │   │   ├── data/pokemon-catalog.json     ← catálogo completo (ID-2 a ID-162)
    │   │   └── static/sprites/*.png          ← 161 sprites, servidos em /sprites/{id}.png
    │   └── controller/
    │       ├── PlayerController.java         ← /jogadores
    │       └── PokemonController.java        ← /figuras
    └── test/java/com/pokemonduel/
        └── service/
            ├── BattleServiceTest.java                  ← tabela de cores
            └── PokemonCatalogServiceAdditionalTest.java ← catálogo + starters

godot-client/                                  ← cliente do jogo (Godot/GDScript)
└── README.md                                  ← ver instruções de uso
```

## Como rodar

**Pré-requisitos:** Java 17+, Maven 3.8+

```bash
cd backend
mvn spring-boot:run
```

Servidor sobe em `http://localhost:8080` — **precisa ficar rodando** enquanto
você usa o cliente Godot: é o design de Cliente-Servidor pedido pelo
professor (Caminho C), então o tabuleiro, as batalhas e o catálogo vivem no
servidor, não no cliente. Não tem como eliminar essa etapa, mas dá pra
deixá-la mais rápida/indolor:

- **Windows**: dois arquivos `.bat` prontos nesta pasta —
  - `iniciar-backend.bat` — duplo-clique, roda `mvn spring-boot:run` (igual
    ao comando acima, só sem digitar). Use este no dia a dia.
  - `compilar-jar.bat` + `iniciar-backend-rapido.bat` — rode o primeiro uma
    vez (gera um `.jar` executável); depois disso, `iniciar-backend-rapido.bat`
    sobe o servidor bem mais rápido (pula a etapa de o Maven recompilar/
    resolver dependências a cada vez).
- **IntelliJ IDEA / VS Code / Eclipse**: se você abrir a pasta `backend/`
  como projeto Maven na IDE, dá pra simplesmente clicar no ▶️ **Run** em cima
  de `PokemonDuelApplication.java` — sem terminal nenhum.

Em qualquer um dos casos, espere aparecer `Started PokemonDuelApplication`
no log antes de abrir o Godot — e mantenha a janela/processo aberto durante
a sessão de jogo.

## Catálogo de Pokémon

O catálogo completo (161 figuras, ID-2 Bulbasaur a ID-162 Genesect) é gerado
a partir da `Lista_.pdf` fornecida pela disciplina, usando `build_catalog.py`
(na raiz do projeto, fora do `backend/`). Cada figura tem:

- `type` / `type2` — tipo(s) elemental(is) (várias figuras são duo-tipo, ex.
  Bulbasaur = Grama/Veneno)
- `rarity` — C, UC, R, EX, UX
- `pm` — Pontos de Movimento
- `specialAbility` — texto da habilidade (ou `null`)
- `moves` — segmentos da roleta: nome, cor, peso ("percentage"), dano,
  efeito de status (quando aplicável) e descrição completa do efeito

**Importante:** no jogo original a maioria das roletas soma 96 "cliques",
mas algumas figuras (ex. Gyarados, Articuno, Darkrai) têm somas diferentes
no documento-fonte — isso foi preservado como está. `spin()` sorteia de
forma proporcional ao peso real de cada segmento, então qualquer soma
positiva funciona corretamente.

### Atualizando o catálogo
1. Edite `build_catalog.py` (formato legível, um Pokémon por bloco `add(...)`)
2. Rode `python3 build_catalog.py` → gera `pokemon-catalog.json`
3. Rode `python3 export_for_java.py` → regenera
   `src/main/resources/data/pokemon-catalog.json`
4. `mvn spring-boot:run` — se alguma roleta estiver vazia, o servidor falha
   no startup apontando o Pokémon problemático.

## Endpoints disponíveis

| Método | Endpoint                        | Descrição                             |
|--------|---------------------------------|---------------------------------------|
| POST   | /jogadores/registrar            | Registra jogador + 6 figuras iniciais |
| GET    | /jogadores                      | Lista todos os jogadores              |
| GET    | /jogadores/{id}                 | Perfil do jogador                     |
| GET    | /jogadores/{id}/figuras         | Figuras desbloqueadas (com roleta)    |
| PUT    | /jogadores/{id}/deck            | Atualiza deck (máx 6 figuras)         |
| GET    | /figuras                        | Catálogo completo de Pokémon (161)    |
| GET    | /figuras/{id}                   | Dados de um Pokémon específico        |
| GET    | /sprites/{id}.png                | Sprite estático do Pokémon            |
| GET    | /salas                          | Lista salas públicas disponíveis      |
| POST   | /salas                          | Cria sala (nome, privada, jogadorId)  |
| POST   | /salas/{id}/entrar              | Entra na sala; inicia a partida quando enche |
| GET    | /partidas/{id}/estado           | Snapshot completo da partida (polling)|
| POST   | /partidas/{id}/entrar           | Move figura do banco para o tabuleiro |
| POST   | /partidas/{id}/mover            | Move figura ativa; resolve batalha automaticamente |
| GET    | /partidas/{id}/movimentos       | Nós alcançáveis por uma figura        |
| GET    | /partidas/{id}/resultado        | Resultado final (após FINISHED)       |

### Endpoints de depuração (`/debug`) — apenas para desenvolvimento

| Método | Endpoint                        | Descrição                             |
|--------|---------------------------------|---------------------------------------|
| POST   | /debug/partida                  | Cria partida Player vs. Bot sem precisar do Lobby |
| POST   | /debug/partida/{id}/bot         | Executa um turno do bot (IA aleatória)|
| POST   | /debug/batalha                  | Simula N batalhas entre dois Pokémon  |
| GET    | /debug/roleta/{id}?vezes=N      | Simula N giros da roleta de um Pokémon|

Esses endpoints alimentam a **Cena de Teste** do cliente Godot (botão
"🧪 Cena de Teste" no Menu Principal) — veja `godot-client/README.md`.
Não exigem nenhum jogador previamente registrado; criam IDs fixos
(`debug-player-1` / `debug-bot-2`) para facilitar testes repetidos.

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

### Exemplo: testar a roleta de um Pokémon
```bash
curl "http://localhost:8080/debug/roleta/mewtwo?vezes=50"
```

### Exemplo: testar uma batalha isolada
```bash
curl -X POST http://localhost:8080/debug/batalha \
     -H "Content-Type: application/json" \
     -d '{"pokemon1": "pikachu", "pokemon2": "charizard", "vezes": 5}'
```

## Como rodar os testes

```bash
mvn test
```

Os testes cobrem toda a tabela de cores 5×5 (BattleService) e validam que
os 161 Pokémon do catálogo têm roletas válidas, sprites configurados e que
os 6 starters existem e são todos Rarity.C.

## Figuras iniciais (Comuns)
Definidas em `PokemonCatalogService.STARTER_IDS`. Todo jogador recebe ao se
registrar:
- **Ivysaur** (C) — Grama/Veneno, PM 2
- **Charmeleon** (C) — Fogo, PM 2
- **Wartortle** (C) — Água, PM 2
- **Machop** (C) — Lutador, PM 2
- **Mareep** (C) — Elétrico, PM 2
- **Marill** (C) — Água/Fada, PM 2

## Cliente do jogo (Godot)

A interface gráfica foi migrada de JavaFX para **Godot/GDScript** (autorizado
pelo professor). Veja `../godot-client/README.md` para instruções — o cliente
consome esta API via HTTP/JSON (`GET /figuras` já está integrado).

## Próxima fase (Fase 2/3)
- `GameState.java` — estado completo de uma partida
- `GameService.java` — movimentação, cerco, condições de vitória
- `RoomService.java` — salas públicas/privadas
- Endpoints `/salas` e `/partidas` (já previstos no `ApiClient.gd` do cliente Godot)
