package com.pokemonduel.model;

import com.pokemonduel.model.enums.NodeType;

/**
 * Um nó do tabuleiro.
 *
 * O tabuleiro é um grafo representado como lista de adjacência.
 * Cada nó tem um ID inteiro único, um tipo e coordenadas (col, row)
 * para renderização no JavaFX.
 *
 * Layout da grade (6 colunas × 4 linhas de nós + especiais):
 *
 *      [GOAL J2]  ← nó 40 (centro topo)
 *   [ENTRY 0][1][2][3][4][ENTRY 5]   ← row 0
 *   [6][7][8][9][10][11]             ← row 1
 *   [12][13][14][15][16][17]         ← row 2
 *   [ENTRY 18][19][20][21][22][ENTRY 23] ← row 3
 *      [GOAL J1]  ← nó 41 (centro base)
 *
 *  Nós especiais: 40 = Goal J2, 41 = Goal J1
 *  Banco e PC ficam fora do grafo principal (gerenciados pelo GameState).
 */
public class BoardNode {

    private int id;
    private NodeType type;
    private int col;    // coluna visual (0–5)
    private int row;    // linha visual  (0–3)
    private int owner;  // 1 ou 2 para GOAL/ENTRY/PC; 0 para neutro

    public BoardNode() {}

    public BoardNode(int id, NodeType type, int col, int row, int owner) {
        this.id = id;
        this.type = type;
        this.col = col;
        this.row = row;
        this.owner = owner;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public NodeType getType() { return type; }
    public void setType(NodeType type) { this.type = type; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getOwner() { return owner; }
    public void setOwner(int owner) { this.owner = owner; }

    @Override
    public String toString() {
        return String.format("Node{id=%d, %s, (%d,%d), owner=%d}", id, type, col, row, owner);
    }
}
