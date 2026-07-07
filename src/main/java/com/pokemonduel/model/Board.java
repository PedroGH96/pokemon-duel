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
 * Layout fixo (como no Pokémon Duel original):
 *
 *               [40:GOAL-J2]
 *    [0:E]─[1]─[2]─[3]─[4]─[5:E]      row 0
 *     │    │   │×│  │   │    │
 *    [6]─[7]─[8]─[9]─[10]─[11]        row 1
 *     │    │         │    │    │
 *   [12]─[13]─[14]─[15]─[16]─[17]     row 2
 *     │    │   │×│  │   │    │
 *   [18:E]─[19]─[20]─[21]─[22]─[23:E] row 3
 *               [41:GOAL-J1]
 *
 *  × = arestas diagonais (cruzamentos no centro)
 *  E = ENTRY (Pokébola, canto)
 *
 * Para simplificar: nós 40 e 41 são os GOALS, conectados
 * ao centro da row 0 (nó 2) e row 3 (nó 20) respectivamente.
 */
public class Board {

    // ID dos nós especiais — constantes para referência fácil
    public static final int GOAL_P1   = 41;
    public static final int GOAL_P2   = 40;
    public static final int TOTAL_NODES = 42; // 0–41

    private final Map<Integer, BoardNode>        nodes;      // id → nó
    private final Map<Integer, List<Integer>>    adjacency;  // id → vizinhos

    public Board() {
        nodes     = new HashMap<>();
        adjacency = new HashMap<>();
        buildBoard();
    }

    // ── Construção do grafo ───────────────────────────────────────────────────

    private void buildBoard() {
        // ── Nós da grade principal (0–23) ────────────────────────────────────
        // row 0 (topo)
        addNode(0,  NodeType.ENTRY,  0, 0, 2);
        addNode(1,  NodeType.NORMAL, 1, 0, 0);
        addNode(2,  NodeType.NORMAL, 2, 0, 0);
        addNode(3,  NodeType.NORMAL, 3, 0, 0);
        addNode(4,  NodeType.NORMAL, 4, 0, 0);
        addNode(5,  NodeType.ENTRY,  5, 0, 2);
        // row 1
        addNode(6,  NodeType.NORMAL, 0, 1, 0);
        addNode(7,  NodeType.NORMAL, 1, 1, 0);
        addNode(8,  NodeType.NORMAL, 2, 1, 0);
        addNode(9,  NodeType.NORMAL, 3, 1, 0);
        addNode(10, NodeType.NORMAL, 4, 1, 0);
        addNode(11, NodeType.NORMAL, 5, 1, 0);
        // row 2
        addNode(12, NodeType.NORMAL, 0, 2, 0);
        addNode(13, NodeType.NORMAL, 1, 2, 0);
        addNode(14, NodeType.NORMAL, 2, 2, 0);
        addNode(15, NodeType.NORMAL, 3, 2, 0);
        addNode(16, NodeType.NORMAL, 4, 2, 0);
        addNode(17, NodeType.NORMAL, 5, 2, 0);
        // row 3 (base)
        addNode(18, NodeType.ENTRY,  0, 3, 1);
        addNode(19, NodeType.NORMAL, 1, 3, 0);
        addNode(20, NodeType.NORMAL, 2, 3, 0);
        addNode(21, NodeType.NORMAL, 3, 3, 0);
        addNode(22, NodeType.NORMAL, 4, 3, 0);
        addNode(23, NodeType.ENTRY,  5, 3, 1);
        // GOALS
        addNode(GOAL_P2, NodeType.GOAL, 2, -1, 2); // acima do tabuleiro
        addNode(GOAL_P1, NodeType.GOAL, 2,  4, 1); // abaixo do tabuleiro

        // ── Arestas horizontais ───────────────────────────────────────────────
        // row 0
        addEdge(0,1); addEdge(1,2); addEdge(2,3); addEdge(3,4); addEdge(4,5);
        // row 1
        addEdge(6,7); addEdge(7,8); addEdge(8,9); addEdge(9,10); addEdge(10,11);
        // row 2
        addEdge(12,13); addEdge(13,14); addEdge(14,15); addEdge(15,16); addEdge(16,17);
        // row 3
        addEdge(18,19); addEdge(19,20); addEdge(20,21); addEdge(21,22); addEdge(22,23);

        // ── Arestas verticais ─────────────────────────────────────────────────
        addEdge(0,6);  addEdge(1,7);  addEdge(2,8);  addEdge(3,9);  addEdge(4,10); addEdge(5,11);
        addEdge(6,12); addEdge(7,13); addEdge(8,14); addEdge(9,15); addEdge(10,16);addEdge(11,17);
        addEdge(12,18);addEdge(13,19);addEdge(14,20);addEdge(15,21);addEdge(16,22);addEdge(17,23);

        // ── Arestas diagonais (cruzamentos centrais) ──────────────────────────
        // cruzamento superior (entre row 0 e row 1, col 2-3)
        addEdge(2,9);  addEdge(3,8);
        // cruzamento inferior (entre row 2 e row 3, col 2-3)
        addEdge(14,21); addEdge(15,20);

        // ── Conexões com os GOALS ─────────────────────────────────────────────
        // Goal J2 conecta ao centro da row 0 (nó 2 e 3)
        addEdge(GOAL_P2, 2);
        addEdge(GOAL_P2, 3);
        // Goal J1 conecta ao centro da row 3 (nó 20 e 21)
        addEdge(GOAL_P1, 20);
        addEdge(GOAL_P1, 21);
    }

    private void addNode(int id, NodeType type, int col, int row, int owner) {
        nodes.put(id, new BoardNode(id, type, col, row, owner));
        adjacency.put(id, new ArrayList<>());
    }

    /** Aresta bidirecional (grafo não-dirigido). */
    private void addEdge(int a, int b) {
        adjacency.get(a).add(b);
        adjacency.get(b).add(a);
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
     * Retorna os nós alcançáveis a partir de {@code from} com exatamente
     * {@code pm} passos, não ocupados pelo próprio jogador.
     *
     * @param from          nó de origem
     * @param pm            pontos de movimento disponíveis
     * @param friendlyNodes nós já ocupados por aliados (bloqueados)
     */
    public Set<Integer> reachableNodes(int from, int pm, Set<Integer> friendlyNodes) {
        Set<Integer> reachable = new HashSet<>();
        bfsLimited(from, pm, friendlyNodes, reachable);
        reachable.remove(from);
        return reachable;
    }

    private void bfsLimited(int start, int pm, Set<Integer> blocked, Set<Integer> result) {
        Queue<int[]> queue = new LinkedList<>(); // {nodeId, stepsLeft}
        Set<Integer> visited = new HashSet<>();

        queue.add(new int[]{start, pm});
        visited.add(start);

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int node = curr[0], steps = curr[1];

            result.add(node);
            if (steps == 0) continue;

            for (int neighbor : adjacency.getOrDefault(node, List.of())) {
                if (!visited.contains(neighbor) && !blocked.contains(neighbor)) {
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
