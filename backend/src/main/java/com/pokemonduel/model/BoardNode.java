package com.pokemonduel.model;

import com.pokemonduel.model.enums.NodeType;

/**
 * Um nó do tabuleiro.
 *
 * O tabuleiro é um grafo representado como lista de adjacência (ver Board.java).
 * Cada nó tem um ID inteiro único, um tipo e coordenadas (col, row) para
 * renderização no cliente (Godot).
 *
 * Layout (replica o grafo oficial do Pokémon Duel, 28 nós, ids 0–27,
 * linhas com 7/5/4/5/7 nós e diagonais formando um losango interno).
 * Ver Board.java para o diagrama completo.
 *
 *  Nós especiais: GOAL_P2=3 (topo), GOAL_P1=24 (base)
 *  ENTRY_P2: 0 e 6 (topo) | ENTRY_P1: 21 e 27 (base)
 *  Banco (6 figuras) e P.C. (2 slots) ficam fora do grafo — são
 *  representados pelo FigureState (BENCH/PC) em PokemonFigure, e
 *  posicionados visualmente pelo cliente Godot ao redor do tabuleiro.
 */
public class BoardNode {

    private int id;
    private NodeType type;
    private int col;    // coluna visual (0–6)
    private int row;    // linha visual  (0–5)
    private int owner;  // 1 ou 2 para GOAL/ENTRY; 0 para neutro

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
