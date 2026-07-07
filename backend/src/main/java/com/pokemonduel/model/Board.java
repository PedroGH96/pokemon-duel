package com.pokemonduel.model;

import com.pokemonduel.model.enums.NodeType;

import java.util.*;

/**
 * Tabuleiro representado como grafo de adjacência.
 *
 * Estrutura interna: Map<Integer, List<Integer>>
 *   chave = ID do nó
 *   valor = lista de IDs dos nós adjacentes
 *
 * Topologia extraída e validada pixel-a-pixel a partir do grafo de
 * referência oficial (28 nós, rotulados A–Z + $ + # na imagem original;
 * ver conversa do projeto). NÃO é uma grade retangular regular — linhas
 * têm contagens de nós diferentes (7/5/4/5/7), com diagonais formando um
 * losango interno que liga as entradas (Pokébola) ao centro do tabuleiro,
 * exatamente como no jogo original:
 *
 *   [E:0]──[1]──[2]──[GOAL-J2:3]──[4]──[5]──[E:6]          linha 0 (7 nós)
 *      ╲              ╱  ╲              ╱
 *      [7]          [8]  [9]          [10]──[11]          linha 1 (5 nós: 7,8,9,10,11)
 *       │            │                  │     │
 *      [12]        [13]                [14] [15]          linha 2 (4 nós: 12,13,14,15)
 *       │            │                  │     │
 *      [16]        [17]──[18]──[19]   [20]                linha 3 (5 nós: 16,17,18,19,20)
 *      ╱              ╲              ╱  ╲              ╲
 *   [E:21]─[22]─[23]─[GOAL-J1:24]─[25]─[26]─[E:27]         linha 4 (7 nós)
 *
 * (diagrama simplificado — ver addNode()/addEdge() abaixo para a lista
 * exata de arestas, validada por detecção de linhas na imagem de
 * referência: 28 nós, 34 arestas, confirmado por soma de graus)
 *
 *  E = ENTRY (Pokébola, 4 cantos: ids 0, 6, 21, 27)
 *  GOAL = bandeira (ids 3 e 24, centro da 1ª e última linha)
 *
 *  Banco e P.C. NÃO se conectam ao grid (figuras "teleportam" do banco
 *  para um ENTRY livre, e do P.C. de volta ao banco — ver GameState).
 */
public class Board {

    // ── IDs especiais — constantes para referência fácil ────────────────────
    public static final int GOAL_P2 = 3;    // bandeira topo (objetivo do jogador 1 atacar)
    public static final int GOAL_P1 = 24;   // bandeira base (objetivo do jogador 2 atacar)

    public static final int ENTRY_P2_LEFT  = 0;
    public static final int ENTRY_P2_RIGHT = 6;
    public static final int ENTRY_P1_LEFT  = 21;
    public static final int ENTRY_P1_RIGHT = 27;

    public static final int TOTAL_NODES = 28; // 0–27 no grid principal

    // NOTA: Banco e P.C. NÃO são nós do grafo — são representados pelo
    // FigureState (BENCH/ACTIVE/PC) em PokemonFigure, com nodeId=-1.
    // O cliente Godot posiciona visualmente as figuras BENCH/PC nos
    // 6 slots de banco e 2 slots de P.C. de cada jogador (ver Board.gd),
    // sem precisar que o backend atribua IDs de nó para essas posições.

    private final Map<Integer, BoardNode>        nodes;      // id → nó
    private final Map<Integer, List<Integer>>    adjacency;  // id → vizinhos

    public Board() {
        nodes     = new HashMap<>();
        adjacency = new HashMap<>();
        buildBoard();
    }

    // ── Construção do grafo ───────────────────────────────────────────────────
    //
    // Mapeamento letra → id (ordem de leitura, linha a linha):
    //   linha 0 (7): A=0  B=1  C=2  D=3*  E=4  F=5  G=6
    //   linha 1 (5): H=7  I=8  J=9  K=10  L=11
    //   linha 2 (4): M=12 N=13 O=14 P=15
    //   linha 3 (5): Q=16 R=17 S=18 T=19  U=20
    //   linha 4 (7): V=21 W=22 X=23 Y=24* Z=25 $=26 #=27
    //   (* = GOAL)
    //
    // col/row abaixo são coordenadas visuais (0–6 colunas, 0–4 linhas)
    // derivadas das posições reais dos nós na imagem de referência —
    // usadas pelo cliente Godot para layout 2D/3D.

    private void buildBoard() {
        // linha 0 — ids 0–6  (A,B,C,D,E,F,G)
        addNode(0, NodeType.ENTRY,  0, 0, 2);  // A
        addNode(1, NodeType.NORMAL, 1, 0, 0);  // B
        addNode(2, NodeType.NORMAL, 2, 0, 0);  // C
        addNode(3, NodeType.GOAL,   3, 0, 2);  // D — bandeira = objetivo do J1
        addNode(4, NodeType.NORMAL, 4, 0, 0);  // E
        addNode(5, NodeType.NORMAL, 5, 0, 0);  // F
        addNode(6, NodeType.ENTRY,  6, 0, 2);  // G

        // linha 1 — ids 7–11  (H,I,J,K,L)
        addNode(7,  NodeType.NORMAL, 0, 1, 0); // H
        addNode(8,  NodeType.NORMAL, 1, 1, 0); // I
        addNode(9,  NodeType.NORMAL, 3, 1, 0); // J
        addNode(10, NodeType.NORMAL, 5, 1, 0); // K
        addNode(11, NodeType.NORMAL, 6, 1, 0); // L

        // linha 2 — ids 12–15  (M,N,O,P)
        addNode(12, NodeType.NORMAL, 0, 2, 0); // M
        addNode(13, NodeType.NORMAL, 1, 2, 0); // N
        addNode(14, NodeType.NORMAL, 5, 2, 0); // O
        addNode(15, NodeType.NORMAL, 6, 2, 0); // P

        // linha 3 — ids 16–20  (Q,R,S,T,U)
        addNode(16, NodeType.NORMAL, 0, 3, 0); // Q
        addNode(17, NodeType.NORMAL, 1, 3, 0); // R
        addNode(18, NodeType.NORMAL, 3, 3, 0); // S
        addNode(19, NodeType.NORMAL, 5, 3, 0); // T
        addNode(20, NodeType.NORMAL, 6, 3, 0); // U

        // linha 4 — ids 21–27  (V,W,X,Y,Z,$,#)
        addNode(21, NodeType.ENTRY,  0, 4, 1); // V
        addNode(22, NodeType.NORMAL, 1, 4, 0); // W
        addNode(23, NodeType.NORMAL, 2, 4, 0); // X
        addNode(24, NodeType.GOAL,   3, 4, 1); // Y — bandeira = objetivo do J2
        addNode(25, NodeType.NORMAL, 4, 4, 0); // Z
        addNode(26, NodeType.NORMAL, 5, 4, 0); // $
        addNode(27, NodeType.ENTRY,  6, 4, 1); // #

        // ── Arestas — lista exata validada contra a imagem de referência ────

        // Bordas externas (perímetro do quadrado)
        addEdge(0,1);  addEdge(1,2);  addEdge(2,3);  addEdge(3,4);  addEdge(4,5);  addEdge(5,6);   // topo  A-B-C-D-E-F-G
        addEdge(21,22);addEdge(22,23);addEdge(23,24);addEdge(24,25);addEdge(25,26);addEdge(26,27); // base  V-W-X-Y-Z-$-#
        addEdge(0,7);  addEdge(7,12); addEdge(12,16);addEdge(16,21);                               // esq.  A-H-M-Q-V
        addEdge(6,11); addEdge(11,15);addEdge(15,20);addEdge(20,27);                                // dir.  G-L-P-U-#

        // Diagonais de entrada (ligam ENTRY/cantos ao losango interno)
        addEdge(0,8);   // A-I
        addEdge(2,9);   // C-J
        addEdge(6,10);  // G-K
        addEdge(21,17); // V-R
        addEdge(25,18); // Z-S
        addEdge(27,19); // #-T

        // Losango interno — linha superior (I-J-K) e inferior (R-S-T)
        addEdge(8,9);   // I-J
        addEdge(9,10);  // J-K
        addEdge(17,18); // R-S
        addEdge(18,19); // S-T

        // Losango interno — verticais (I-N-R e K-O-T)
        addEdge(8,13);  // I-N
        addEdge(13,17); // N-R
        addEdge(10,14); // K-O
        addEdge(14,19); // O-T
    }

    private void addNode(int id, NodeType type, int col, int row, int owner) {
        nodes.put(id, new BoardNode(id, type, col, row, owner));
        adjacency.put(id, new ArrayList<>());
    }

    /** Aresta bidirecional (grafo não-dirigido). */
    private void addEdge(int a, int b) {
        if (!adjacency.get(a).contains(b)) adjacency.get(a).add(b);
        if (!adjacency.get(b).contains(a)) adjacency.get(b).add(a);
    }

    // ── Pathfinding ───────────────────────────────────────────────────────────

    /**
     * BFS — encontra o caminho mais curto de {@code from} até {@code to}.
     *
     * Como todas as arestas têm peso 1, BFS é equivalente ao Dijkstra
     * e mais eficiente (O(V+E) vs O((V+E)logV)).
     *
     * Use Dijkstra se futuramente adicionar pesos (ex: penalizar cerco).
     *
     * @return lista de IDs de nós do caminho (inclui origem e destino),
     *         ou lista vazia se não houver caminho.
     */
    public List<Integer> bfsPath(int from, int to) {
        if (from == to) return List.of(from);

        Map<Integer, Integer> parent = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(from);
        parent.put(from, -1);

        while (!queue.isEmpty()) {
            int curr = queue.poll();
            if (curr == to) return reconstructPath(parent, from, to);

            for (int neighbor : adjacency.getOrDefault(curr, List.of())) {
                if (!parent.containsKey(neighbor)) {
                    parent.put(neighbor, curr);
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList(); // sem caminho
    }

    /**
     * Dijkstra genérico com suporte a pesos.
     * Útil se futuramente arestas forem bloqueadas ou penalizadas.
     *
     * @param blockedNodes nós que não podem ser atravessados (ex: cercados)
     * @return mapa de distâncias mínimas de {@code from} para todos os nós
     */
    public Map<Integer, Integer> dijkstra(int from, Set<Integer> blockedNodes) {
        Map<Integer, Integer> dist = new HashMap<>();
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        for (int id : nodes.keySet()) dist.put(id, Integer.MAX_VALUE);
        dist.put(from, 0);
        pq.offer(new int[]{from, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int node = curr[0], d = curr[1];

            if (d > dist.get(node)) continue;

            for (int neighbor : adjacency.getOrDefault(node, List.of())) {
                if (blockedNodes.contains(neighbor)) continue;
                int newDist = dist.get(node) + 1;
                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    pq.offer(new int[]{neighbor, newDist});
                }
            }
        }
        return dist;
    }

    private List<Integer> reconstructPath(Map<Integer, Integer> parent, int from, int to) {
        LinkedList<Integer> path = new LinkedList<>();
        int curr = to;
        while (curr != -1) {
            path.addFirst(curr);
            curr = parent.get(curr);
        }
        return path;
    }

    // ── Regras do tabuleiro ───────────────────────────────────────────────────

    /**
     * Verifica se uma figura está cercada (todos os nós adjacentes ocupados
     * por figuras inimigas). O cerco causa K.O. automático sem batalha.
     *
     * @param nodeId      posição da figura a verificar
     * @param enemyNodes  conjunto de nós ocupados por figuras inimigas
     */
    public boolean isSurrounded(int nodeId, Set<Integer> enemyNodes) {
        List<Integer> neighbors = adjacency.getOrDefault(nodeId, List.of());
        return !neighbors.isEmpty() && enemyNodes.containsAll(neighbors);
    }

    /**
     * Retorna os nós alcançáveis a partir de {@code from} com até
     * {@code pm} passos.
     *
     * Regras de bloqueio:
     *  - nós com FIGURA ALIADA: totalmente bloqueados — não dá pra parar
     *    neles nem atravessá-los.
     *  - nós com FIGURA INIMIGA: dá pra PARAR neles (aciona a batalha), mas
     *    o caminho não pode continuar além — funcionam como "parede" só
     *    depois de alcançados, nunca como passagem livre.
     *
     * @param from          nó de origem
     * @param pm            pontos de movimento disponíveis
     * @param friendlyNodes nós ocupados por aliados (bloqueio total)
     * @param enemyNodes    nós ocupados por inimigos (pode parar, não atravessa)
     */
    public Set<Integer> reachableNodes(int from, int pm, Set<Integer> friendlyNodes, Set<Integer> enemyNodes) {
        Set<Integer> reachable = new HashSet<>();
        bfsLimited(from, pm, friendlyNodes, enemyNodes, reachable);
        reachable.remove(from);
        return reachable;
    }

    /** Sobrecarga de compatibilidade — sem distinguir inimigos (trata todo mundo como bloqueio total). */
    public Set<Integer> reachableNodes(int from, int pm, Set<Integer> friendlyNodes) {
        return reachableNodes(from, pm, friendlyNodes, Collections.emptySet());
    }

    private void bfsLimited(int start, int pm, Set<Integer> friendlyBlocked,
                             Set<Integer> enemyNodes, Set<Integer> result) {
        Queue<int[]> queue = new LinkedList<>(); // {nodeId, stepsLeft}
        Set<Integer> visited = new HashSet<>();

        queue.add(new int[]{start, pm});
        visited.add(start);

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int node = curr[0], steps = curr[1];

            result.add(node);
            // Nó com inimigo: pode ser alcançado (pra batalhar), mas o
            // caminho NÃO continua a partir dele — trata como fim de linha.
            if (steps == 0 || enemyNodes.contains(node)) continue;

            for (int neighbor : adjacency.getOrDefault(node, List.of())) {
                if (!visited.contains(neighbor) && !friendlyBlocked.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(new int[]{neighbor, steps - 1});
                }
            }
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public BoardNode getNode(int id) { return nodes.get(id); }
    public List<Integer> getNeighbors(int id) {
        return adjacency.getOrDefault(id, Collections.emptyList());
    }
    public Map<Integer, BoardNode> getAllNodes() { return Collections.unmodifiableMap(nodes); }
    public Map<Integer, List<Integer>> getAdjacency() { return Collections.unmodifiableMap(adjacency); }
}
