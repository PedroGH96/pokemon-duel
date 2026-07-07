# 🎮 Pokémon Duel

Clone do **Pokémon Duel / Pokémon TFG** — tabuleiro em grafo, figuras que se
movem, batalham girando uma roleta de ataques e podem cercar o inimigo para
derrotá-lo sem precisar batalhar.

Trabalho da disciplina **Linguagem de Programação 2** (UFRN, prof. João
Anísio Marinho da Nóbrega). Trilha escolhida: **C — Cliente-Servidor**,
com autorização do professor para usar **Godot/GDScript** no lugar de
JavaFX no cliente.

---

## 📖 Sobre o projeto

- **Backend**: Java 17 + Spring Boot 3.2 — motor de jogo (regras, tabuleiro,
  batalhas, partidas), catálogo de **161 Pokémon**.
- **Cliente**: Godot 4.x, GDScript, **100% 2D** — menu, montagem de deck,
  lobby multiplayer, modo solo e tabuleiro jogável.
- Comunicação via **API REST HTTP/JSON** entre cliente e servidor — o
  cliente nunca decide sozinho o que é uma jogada válida; toda regra
  (movimento, alcance, batalha, vitória) é validada e resolvida no backend.

---

## 📂 Estrutura do repositório

```
.
├── project.godot              ← projeto Godot (abra a raiz do repo no editor)
├── build_catalog.py           ← fonte legível dos 161 Pokémon
├── export_for_java.py         ← gera o catálogo consumido pelo backend
├── pokemon-catalog.json       ← catálogo exportado
├── assets/
│   └── sprites/
│       ├── pokemon/{icons, battle/front(+shiny), battle/back(+shiny)}
│       └── npc/
├── scenes/
│   ├── menu/MainMenu.*
│   ├── deck_builder/DeckBuilder.*
│   ├── lobby/Lobby.*
│   ├── board/Board.*              ← tabuleiro 2D (multiplayer, solo e teste)
│   ├── solo/{LevelSelect,SoloBattle}.*
│   └── debug/TestArea.*           ← área de teste (tabuleiro + roleta isolada)
├── scripts/
│   ├── autoload/{ApiClient,GameState}.gd
│   └── components/{BattleOverlay,BattleWheel}.gd
└── backend/
    ├── pom.xml
    ├── iniciar-backend.bat / compilar-jar.bat / iniciar-backend-rapido.bat
    └── src/main/java/com/pokemonduel/
        ├── model/       (Board, Pokemon, PokemonFigure, Player, Move, GameState, Room, BattleResult, enums/...)
        ├── service/     (GameService, RoomService, PlayerService, PokemonCatalogService, BattleService)
        ├── controller/  (MatchController, RoomController, PlayerController, SoloController, DebugController, PokemonController)
        └── repository/  (PlayerRepository)
```

---

## ✨ Funcionalidades implementadas

**Backend**
- Tabuleiro em grafo (28 nós / 34 arestas), com pontos de **Entrada** (2 por
  jogador), **Goal** e nós comuns.
- Movimento por **PM (pontos de movimento)**: a entrada consome 1 PM e o
  restante pode ser usado na mesma jogada (o jogador escolhe continuar
  movendo ou passar a vez sem gastar o resto).
- Regra de bloqueio de caminho: peça **aliada** bloqueia totalmente a
  passagem; peça **inimiga** pode ser alcançada (para lutar), mas o caminho
  não continua depois dela.
- Cerco (surround): derrota o inimigo sem precisar de batalha.
- Batalha com roleta de cores (Red/White/Purple/Gold/Blue), seguindo a
  tabela oficial de prioridade/comparação, incluindo empate (nenhuma figura
  se move da posição de origem/destino) e Destiny Bond.
- Sistema de figuras: banco, ativa em campo, P.C. (fora de combate).
- Modo Solo: 6 níveis (5 inimigos + 1 chefe cada), bot com IA simples,
  caixa de recompensa com sorteio ponderado por raridade e sistema de shiny.
- Endpoints de depuração (`/debug/...`) para testar tabuleiro e roleta sem
  precisar de dois jogadores.

**Cliente (Godot)**
- Menu → Montar Deck (até 6 figuras, com detalhes de tipo/raridade/PM/roleta
  de cada uma) → Multiplayer (Lobby) ou Modo Solo ou Área de Teste.
- Tabuleiro 2D fiel ao grafo do backend, com nós de entrada, goal e alcance
  de movimento (nós verdes) desenhados dinamicamente.
- Slots visuais de **banco (6)** e **P.C. (2)** fora do tabuleiro para os
  dois jogadores, além da lista em texto.
- Overlay de batalha animado (roleta girando, sprites de ataque/defesa,
  resultado).
- Sprites completos: ícone no tabuleiro/banco/deck e sprites de batalha
  (frente/costas, normal/shiny).
- Área de Teste: cria partidas com decks customizados e roleta isolada
  (não depende de partida ativa).

---

## ⚠️ Limitações conhecidas / próximos passos

- **Habilidades especiais** (ex.: "Semente Curativa") hoje são só texto
  descritivo — não têm lógica aplicada nas batalhas.
- **Persistência**: progresso do Modo Solo e coleção shiny ficam só em
  memória no cliente (fecha o jogo, perde).
- **IA do bot** é aleatória — a proposta original sugeria busca em grafo
  (Dijkstra) para o Modo Solo.
- Sistema de moedas/gemas e fusão de figuras: fora do escopo até agora.
- Tela de resultado dedicada de fim de partida: hoje é um painel sobre o
  próprio tabuleiro.

---

## 🚀 Como rodar

### Pré-requisitos
- **JDK 17+** e **Maven** (ou use os scripts `.bat` inclusos, que baixam/
  usam o Maven Wrapper)
- **Godot Engine 4.x** ([godotengine.org](https://godotengine.org/download))

### 1. Subir o backend
=======
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

>>>>>>> a83bfd93e1d72d70030200b3f0e02516fe30e7fe
```bash
cd backend
mvn spring-boot:run

No Windows, alternativa com duplo-clique:
- `iniciar-backend.bat` — roda `mvn spring-boot:run` direto.
- `compilar-jar.bat` — gera um `.jar` executável (rodar uma vez).
- `iniciar-backend-rapido.bat` — depois do jar compilado, sobe bem mais
  rápido (`java -jar target\pokemon-duel-backend-1.0.0.jar`).

O backend sobe em `http://localhost:8080`. Pra confirmar que subiu certo,
acesse `http://localhost:8080/figuras` — deve retornar o JSON com os 161
Pokémon do catálogo.

### 2. Abrir o cliente no Godot
1. Abra a Godot Engine 4.x.
2. **Importar** → selecione a **raiz deste repositório** (onde está o
   `project.godot`).
3. Play (▶) ou F5 — o backend precisa estar rodando primeiro, senão as
   telas de Deck/Multiplayer/Solo não conseguem carregar o catálogo.

---

## 🎮 Como jogar

1. **Menu Principal** → **Montar Deck**: escolha até 6 figuras.
2. Escolha um modo:
   - **Multiplayer**: cria/entra numa sala (Lobby) com outro jogador.
   - **Modo Solo**: enfrenta 6 níveis (5 inimigos + chefe cada) controlados
     por bot.
   - **🧪 Área de Teste**: cria uma partida com decks customizáveis (por
     ID, separados por vírgula) e/ou testa a roleta isoladamente.
3. No tabuleiro:
   - Clique numa figura do banco (lista da esquerda ou nos slots visuais
     fora do tabuleiro) → clique num nó de **Entrada** (canto colorido —
     precisa ser um dos seus).
   - Se sobrar PM depois de entrar, você pode clicar num nó verde pra
     continuar se movendo, ou no botão **Passar** pra encerrar o turno.
   - Clique numa figura já em campo → clique num nó verde (alcance)
     pra mover; se o nó tiver um inimigo, a batalha começa automaticamente
     (roleta gira para os dois lados e o resultado é aplicado).
   - Cerque o inimigo (bloqueie todas as saídas dele com suas figuras) para
     derrotá-lo sem precisar de batalha.

---

## 🛠️ Stack técnica

| Camada    | Tecnologia                          |
|-----------|--------------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Maven      |
| Cliente   | Godot 4.x, GDScript                  |
| Dados     | JSON (catálogo), em memória (partidas) |
| Comunicação | REST HTTP/JSON                     |

---

## 📜 Créditos

Projeto acadêmico desenvolvido para a disciplina de Linguagem de Programação 2
— UFRN.
=======

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
>>>>>>> a83bfd93e1d72d70030200b3f0e02516fe30e7fe
