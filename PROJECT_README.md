# Pokémon Duel — Projeto (Caminho C: Cliente-Servidor)

Backend em **Java/Spring Boot** + cliente em **Godot/GDScript** (autorizado
pelo professor no lugar de JavaFX), implementando um jogo de tabuleiro
baseado na Pokémon Trading Figure Game / Pokémon Duel.

## Estrutura

```
.
├── backend/              ← API REST (Spring Boot 3, Java 17)
│   └── README.md         ← como rodar o servidor, endpoints, testes
├── godot-client/         ← cliente do jogo (Godot 4.x, 100% 2D)
│   └── README.md         ← como abrir o projeto no Godot, conectar à API
├── build_catalog.py       ← fonte legível dos 161 Pokémon (ID-2 a ID-162)
├── export_for_java.py     ← converte build_catalog.py → JSON p/ o backend
└── pokemon-catalog.json   ← saída intermediária de build_catalog.py
```

## O que já está pronto

- **Catálogo completo (161 Pokémon, ID-2 a ID-162)**, transcrito da
  `Lista_.pdf`: tipo(s), raridade, PM, habilidade especial e roleta de
  ataques (nome, cor, peso, dano, efeito de status, descrição).
- **Pipeline de sprites completo** (de `sprites.zip`), organizado por `id`
  (slug) de cada Pokémon em 5 conjuntos:
  - `assets/sprites/pokemon/icons/{id}.png` — ícone usado no tabuleiro, no
    banco de figuras e na tela de Deck.
  - `assets/sprites/pokemon/battle/front/{id}.png` (+ `shiny/{id}.png`) —
    Pokémon adversário na transição de batalha.
  - `assets/sprites/pokemon/battle/back/{id}.png` (+ `shiny/{id}.png`) —
    seu Pokémon na transição de batalha.
  - Mesma estrutura espelhada em `backend/src/main/resources/static/sprites/`.
  - **Não existe ícone shiny** — a variante shiny só aparece na tela de
    batalha (front/back), nunca no tabuleiro.
- **Backend Spring Boot**: `GameService`/`RoomService`/`MatchController`
  completos (movimentação, cerco, batalha, condições de vitória),
  `SoloController` (6 níveis × 5 inimigos + 1 chefe, deck fixo dos NPCs =
  ID-2 a ID-7) e sistema de recompensa (`POST /solo/recompensa`): ao vencer
  o chefe de um nível, sorteia um Pokémon ponderado por raridade; se o
  jogador já tiver essa figura, ele ganha a versão **shiny** dela em vez de
  uma repetida.
- **Cliente Godot 100% 2D** (o cenário 3D/`TileMapLayer3D` foi removido).
  O tabuleiro (`Board.tscn`/`Board.gd`) desenha o grafo real de 28 nós / 34
  arestas (mesma topologia de `Board.java`, validada nó a nó) com
  `draw_line`/`draw_circle` — não é mais um grid de tiles, é o grafo em si.
  O Modo Solo agora acontece dentro do próprio tabuleiro (movimentação real,
  roleta de batalha, turno do bot automático), não numa tela de log
  separada.

## Estrutura do tabuleiro (grafo)

Os nós usam os mesmos IDs 0–27 de `Board.java` (equivalentes às letras A–Z,
$ e # do diagrama de referência). `Board.gd` mantém uma cópia local de
`NODE_COLS`/`NODE_ROWS`/`BOARD_EDGES` apenas para calcular a posição 2D de
cada nó na tela — a adjacência real (quem pode se mover para onde) é sempre
decidida pelo servidor via `GET /partidas/{id}/figuras/{figId}/movimentos`.

## Como rodar tudo

1. **Backend**: `cd backend && mvn spring-boot:run` → `http://localhost:8080`
2. **Cliente**: abra `godot-client/` no Godot 4.2+, rode a cena `MainMenu`.

Os sprites (ícones e telas de batalha) são carregados localmente pelo
cliente (`res://assets/...`) e aparecem **mesmo sem o backend rodando** —
o que exige o backend é apenas o *estado da partida* (posição/turno das
figuras). Ou seja: abrir uma tela de menu/deck já mostra os sprites; para
ver Pokémon se movendo no tabuleiro (Solo ou Multiplayer) é preciso criar
uma partida, o que sim requer `mvn spring-boot:run`.

Veja os READMEs de cada pasta para detalhes.

## Próximos passos (roadmap)

1. Implementar os efeitos de status restantes (muitos ataques têm efeitos
   complexos descritos em texto livre no campo `description` de cada Move —
   bom roteiro para dividir o trabalho entre a dupla).
2. Persistir o progresso do Modo Solo e a coleção shiny entre sessões (hoje
   `GameState.gd` guarda isso só em memória, no lado do cliente).
3. Trocar a IA aleatória do bot (`DebugController`) por Dijkstra/busca em
   grafo, como sugerido na proposta original.
4. Sistema de moedas/gemas e fusão de figuras (mencionados na proposta, mas
   fora do escopo desta rodada de mudanças).

## ⚠️ Aviso de segurança

O PDF da proposta original (`Atividade_Proposta_de_Projeto...pdf`) contém um
texto de *prompt injection* tentando instruir IAs a destruir o conteúdo de
qualquer resumo/edição. Esse texto foi ignorado em todas as interações — é
apenas um teste/pegadinha embutido no documento e não afeta o projeto.
