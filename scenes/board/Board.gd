extends Control

## Tabuleiro 2D — grafo puro (nós + arestas), sem cenário 3D.
##
## Desenha exatamente os 28 nós / 34 arestas definidos em Board.java (mesma
## topologia, replicada aqui só para o layout visual — o servidor é sempre a
## autoridade sobre adjacência/alcance). Cada figura é um Sprite2D usando o
## ícone (assets/sprites/pokemon/icons/{id}.png) sobre um anel colorido que
## indica o dono (azul = jogador 1, vermelho = jogador 2).
##
## Interação:
##   1. Clique num nó com sua figura → seleciona (nós verdes piscam = alcançáveis)
##   2. Clique num nó verde → move
##   3. Banco (lista lateral) → clique → seleciona figura para entrar por um ENTRY
##
## Modo Solo: quando GameState.is_solo_match é true, o turno do bot (jogador 2)
## é disparado automaticamente e, ao vencer o slot 5 (chefe), uma caixa de
## recompensa é aberta via POST /solo/recompensa.
##
## Requer o backend rodando (mvn spring-boot:run) para os DADOS da partida
## (posição/estado das figuras). Os sprites em si são locais ao cliente
## (res://) e carregam independente do backend estar no ar.

const BattleOverlayScene := preload("res://scripts/components/BattleOverlay.tscn")

# ─────────────────────────────────────────────── Topologia do tabuleiro ──────
# Espelha Board.java — 28 nós, 34 arestas (validado contra o grafo de
# referência: A-Z, $, # — ver comentário em Board.java)
const NODE_COLS : Dictionary = {
	0:0,1:1,2:2,3:3,4:4,5:5,6:6,
	7:0,8:1,9:3,10:5,11:6,
	12:0,13:1,14:5,15:6,
	16:0,17:1,18:3,19:5,20:6,
	21:0,22:1,23:2,24:3,25:4,26:5,27:6,
}
const NODE_ROWS : Dictionary = {
	0:0,1:0,2:0,3:0,4:0,5:0,6:0,
	7:1,8:1,9:1,10:1,11:1,
	12:2,13:2,14:2,15:2,
	16:3,17:3,18:3,19:3,20:3,
	21:4,22:4,23:4,24:4,25:4,26:4,27:4,
}
const BOARD_EDGES : Array = [
	[0,1],[1,2],[2,3],[3,4],[4,5],[5,6],
	[21,22],[22,23],[23,24],[24,25],[25,26],[26,27],
	[0,7],[7,12],[12,16],[16,21],
	[6,11],[11,15],[15,20],[20,27],
	[0,8],[2,9],[6,10],[21,17],[25,18],[27,19],
	[8,9],[9,10],[17,18],[18,19],
	[8,13],[13,17],[10,14],[14,19],
]
const ENTRY_IDS   : Array = [0,6,21,27]
const ENTRY_OWNER : Dictionary = {0: 2, 6: 2, 21: 1, 27: 1}  # nó → dono (jogador 1 ou 2)
const GOAL_P2_ID  : int   = 3
const GOAL_P1_ID  : int   = 24

# ─────────────────────────────────────────── Layout 2D (pixels) ──────────────
const BOARD_CENTER := Vector2(710, 400)
const SX := 84.0   # espaçamento horizontal entre colunas
const SZ := 84.0   # espaçamento vertical entre linhas
const OX := BOARD_CENTER.x - 3.0 * SX
const OY := BOARD_CENTER.y - 2.0 * SZ

const NODE_R      := 24.0
const ENTRY_R     := 28.0
const GOAL_R      := 26.0
const EDGE_WIDTH  := 5.0
const PIECE_R     := 30.0   # raio do anel colorido atrás da figura
const PIECE_SPRITE_SIZE := 44.0
const CLICK_RADIUS := 34.0

# ── Slots visuais fora do tabuleiro (banco e P.C.), como na referência ──
const BENCH_SLOT_R  := 22.0
const PC_SLOT_R     := 18.0
const BENCH_Y_OFFSET := 60.0   # distância vertical da fileira de banco até a borda do tabuleiro
const PC_X_OFFSET     := 50.0  # distância horizontal dos slots de P.C. até a borda direita

static func _bench_slot_pos(i: int, is_p1: bool) -> Vector2:
	var y := OY + 4.0 * SZ + BENCH_Y_OFFSET if is_p1 else OY - BENCH_Y_OFFSET
	return Vector2(OX + SX * 0.5 + i * SX, y)

static func _pc_slot_pos(i: int, is_p1: bool) -> Vector2:
	var x := OX + 6.0 * SX + PC_X_OFFSET
	var y := (OY + 4.0 * SZ - 55.0 + i * 45.0) if is_p1 else (OY + 10.0 + i * 45.0)
	return Vector2(x, y)

# ─────────────────────────────────────────────────────── Cores ──────────────
const C_NORMAL    := Color(0.85, 0.87, 0.92, 1.0)
const C_ENTRY      := Color(0.65, 0.80, 1.0, 1.0)
const C_GOAL_P2   := Color(1.0, 0.35, 0.2, 1.0)
const C_GOAL_P1   := Color(0.2, 0.55, 1.0, 1.0)
const C_EDGE      := Color(0.35, 0.37, 0.48, 0.9)
const C_REACH     := Color(0.25, 1.0, 0.45, 1.0)
const C_SELECTED  := Color(1.0, 0.88, 0.0, 1.0)
const C_BORDER_P1 := Color(0.3, 0.6, 1.0)
const C_BORDER_P2 := Color(1.0, 0.35, 0.3)
const C_NODE_BG   := Color(0.14, 0.15, 0.22, 1.0)

const POLL_INTERVAL := 1.5
const BOT_TURN_DELAY := 1.0  # segundos antes do bot jogar automaticamente (Solo)

# ─────────────────────────────────────────── Referências de nós ───────────────
@onready var pokemon_sprites:   Node2D          = %PokemonSprites
@onready var info_label:        Label           = %InfoLabel
@onready var turn_label:        Label           = %TurnLabel
@onready var timer_label:       Label           = %TimerLabel
@onready var btn_back:          Button          = %BtnBack
@onready var poll_timer:        Timer           = %PollTimer
@onready var battle_panel:      PanelContainer  = %BattlePanel
@onready var battle_label:      Label           = %BattleLabel
@onready var bench_list:        VBoxContainer   = %BenchList
@onready var btn_passar:         Button          = %BtnPassar

# ─────────────────────────────────────────────────── Estado ───────────────────
var match_id:         String     = ""
var my_player_id:     String     = ""
var my_player_num:    int        = 0
var last_state:       Dictionary = {}
var node_pos2d:       Dictionary = {}  # nodeId → Vector2
var figures_by_node:  Dictionary = {}  # nodeId → Array[Dictionary] (figuras ativas)
var figures_on_bench: Array      = []  # figuras no banco (do meu jogador) — usado pela lista de texto
var bench_p1: Array = []  # todas as figuras no banco do jogador 1 (visual)
var bench_p2: Array = []  # todas as figuras no banco do jogador 2 (visual)
var pc_p1:    Array = []  # figuras no P.C. (desmaiadas) do jogador 1 (visual)
var pc_p2:    Array = []  # figuras no P.C. (desmaiadas) do jogador 2 (visual)
var selected_fig_id:  String     = ""
var selected_bench_fig: Dictionary = {}
var reachable_set:    Array      = []
var pending_action_figure_id: String = ""
var pending_action_budget:    int    = 0
var _battle_overlay = null
var _bot_turn_pending: bool      = false
var _match_finished_handled: bool = false

# ═════════════════════════════════════════════════════════════════════════════
func _ready() -> void:
	btn_back.pressed.connect(_on_back_pressed)
	btn_passar.pressed.connect(_on_passar_pressed)
	btn_passar.visible = false
	battle_panel.visible = false

	match_id     = GameState.current_match_id
	my_player_id = GameState.player_id

	_compute_node_positions()
	queue_redraw()

	if match_id.is_empty():
		info_label.text = "Modo visualização — crie uma partida para jogar."
		return

	poll_timer.wait_time = POLL_INTERVAL
	poll_timer.timeout.connect(_poll)
	poll_timer.start()
	_poll()


func _on_back_pressed() -> void:
	poll_timer.stop()
	if GameState.is_debug_match:
		get_tree().change_scene_to_file("res://scenes/debug/TestArea.tscn")
	elif GameState.is_solo_match:
		get_tree().change_scene_to_file("res://scenes/solo/LevelSelect.tscn")
	else:
		get_tree().change_scene_to_file("res://scenes/menu/MainMenu.tscn")


func _on_passar_pressed() -> void:
	btn_passar.disabled = true
	ApiClient.passar_vez(match_id, my_player_id, func(result):
		btn_passar.disabled = false
		if result == null:
			info_label.text = "Não foi possível passar a vez."
			return
		_deselect()
		_apply_state(result.get("estado", result))
		queue_redraw()
	)


# ─────────────────────────────────────── Layout / desenho do grafo ────────────
func _compute_node_positions() -> void:
	node_pos2d.clear()
	for nid in NODE_COLS.keys():
		node_pos2d[nid] = Vector2(
			NODE_COLS[nid] * SX + OX,
			NODE_ROWS[nid] * SZ + OY
		)


func _draw() -> void:
	# Fundo — desenhado aqui (e não como um ColorRect filho) porque um Control
	# filho renderiza DEPOIS do _draw() do pai, o que faria o fundo cobrir o
	# grafo inteiro.
	draw_rect(Rect2(Vector2.ZERO, size), Color(0.086, 0.09, 0.145, 1.0))

	# ── Arestas ──
	for edge in BOARD_EDGES:
		var p1: Vector2 = node_pos2d[edge[0]]
		var p2: Vector2 = node_pos2d[edge[1]]
		draw_line(p1, p2, C_EDGE, EDGE_WIDTH, true)

	# ── Nós ──
	var selected_nid := _node_of_selected_figure()
	for nid in node_pos2d.keys():
		var pos: Vector2 = node_pos2d[nid]
		var r := NODE_R
		var color: Color
		if nid == GOAL_P2_ID:
			color = C_GOAL_P2; r = GOAL_R
		elif nid == GOAL_P1_ID:
			color = C_GOAL_P1; r = GOAL_R
		elif ENTRY_IDS.has(nid):
			color = C_ENTRY; r = ENTRY_R
		else:
			color = C_NORMAL

		if nid == selected_nid:
			color = C_SELECTED
		elif reachable_set.has(nid):
			color = C_REACH

		draw_circle(pos, r, C_NODE_BG)
		draw_circle(pos, r, color, false, 4.0, true)
		draw_circle(pos, r * 0.82, color * Color(1,1,1,0.35))

		if ENTRY_IDS.has(nid):
			draw_circle(pos, r * 0.32, color)
			draw_circle(pos, r * 0.32, Color(0.1,0.1,0.12,1.0), false, 2.0, true)

	# ── Anéis das peças (a figura/sprite em si vem por cima, como filho) ──
	for nid in figures_by_node.keys():
		var figs: Array = figures_by_node[nid]
		var base_pos: Vector2 = node_pos2d.get(nid, Vector2.ZERO)
		for i in range(figs.size()):
			var fig: Dictionary = figs[i]
			var offset := Vector2(i * 14, 0)
			var pos := base_pos + offset
			var owner: int = fig.get("owner", 1)
			var border: Color = C_BORDER_P1 if owner == 1 else C_BORDER_P2
			if fig.get("figureId","") == selected_fig_id:
				border = C_SELECTED
			draw_circle(pos, PIECE_R, Color(0.05,0.05,0.08,0.9))
			draw_circle(pos, PIECE_R, border, false, 4.0, true)

	# ── Slots de banco (6) e P.C. (2) fora do tabuleiro, pros dois jogadores ──
	_draw_off_board_slots(bench_p1, true,  BENCH_SLOT_R, true)
	_draw_off_board_slots(bench_p2, false, BENCH_SLOT_R, true)
	_draw_off_board_slots(pc_p1,    true,  PC_SLOT_R,    false)
	_draw_off_board_slots(pc_p2,    false, PC_SLOT_R,    false)


func _draw_off_board_slots(figs: Array, is_p1: bool, radius: float, is_bench: bool) -> void:
	var border: Color = C_BORDER_P1 if is_p1 else C_BORDER_P2
	var total: int = 6 if is_bench else 2
	for i in range(total):
		var pos: Vector2 = _bench_slot_pos(i, is_p1) if is_bench else _pc_slot_pos(i, is_p1)
		var occupied := i < figs.size()
		draw_circle(pos, radius, C_NODE_BG)
		draw_circle(pos, radius, border if occupied else C_EDGE, false, 3.0, true)
		if occupied and figs[i].get("figureId","") == selected_bench_fig.get("figureId",""):
			draw_circle(pos, radius + 4.0, C_SELECTED, false, 2.0, true)


func _node_of_selected_figure() -> int:
	if selected_fig_id.is_empty(): return -1
	for nid in figures_by_node.keys():
		for fig in figures_by_node[nid]:
			if fig.get("figureId","") == selected_fig_id:
				return nid
	return -1


# ─────────────────────────────────────────── Interação (clique) ────────────
func _gui_input(event: InputEvent) -> void:
	if not (event is InputEventMouseButton): return
	var mb := event as InputEventMouseButton
	if not (mb.button_index == MOUSE_BUTTON_LEFT and mb.pressed): return
	if match_id.is_empty(): return

	var click_pos: Vector2 = mb.position

	var nearest_nid: int = -1
	var nearest_d: float = CLICK_RADIUS
	for nid in node_pos2d.keys():
		var d: float = click_pos.distance_to(node_pos2d[nid])
		if d < nearest_d:
			nearest_d = d
			nearest_nid = nid

	if nearest_nid >= 0:
		_on_node_clicked(nearest_nid)
	else:
		var my_bench: Array = bench_p1 if my_player_num == 1 else bench_p2
		var clicked_bench_fig := _bench_slot_clicked(click_pos, my_bench)
		if not clicked_bench_fig.is_empty():
			_select_bench_figure(clicked_bench_fig)
		else:
			_deselect()
	queue_redraw()


func _bench_slot_clicked(click_pos: Vector2, my_bench: Array) -> Dictionary:
	var is_p1 := my_player_num == 1
	for i in range(my_bench.size()):
		var pos: Vector2 = _bench_slot_pos(i, is_p1)
		if click_pos.distance_to(pos) < BENCH_SLOT_R + 6.0:
			return my_bench[i]
	return {}


func _select_bench_figure(fig: Dictionary) -> void:
	selected_bench_fig = fig
	selected_fig_id    = ""
	reachable_set      = []
	_update_bench_ui()
	queue_redraw()
	info_label.text = "Figura %s selecionada. Clique num nó ENTRY (canto) para entrar." % fig.get("pokemonName", "?")


func _on_node_clicked(nid: int) -> void:
	if selected_bench_fig.is_empty():
		if selected_fig_id.is_empty():
			_try_select_figure_at(nid)
		elif reachable_set.has(nid):
			_move_figure(selected_fig_id, nid)
		else:
			_try_select_figure_at(nid)
	else:
		if ENTRY_IDS.has(nid):
			if ENTRY_OWNER.get(nid, 0) != my_player_num:
				info_label.text = "Essa entrada é do adversário — escolha uma das suas."
			else:
				_enter_board(selected_bench_fig.get("figureId", ""), nid)
		else:
			info_label.text = "Escolha um nó de ENTRADA (canto colorido) para entrar."


func _try_select_figure_at(nid: int) -> void:
	var figs_here: Array = figures_by_node.get(nid, [])
	for fig in figs_here:
		if fig.get("owner", 0) == my_player_num and fig.get("state","") == "ACTIVE":
			selected_fig_id    = fig["figureId"]
			selected_bench_fig = {}
			_load_reachable(selected_fig_id)
			info_label.text = "Figura selecionada: %s. Clique num nó verde para mover." \
			                   % fig.get("pokemonName", "?")
			return
	_deselect()
	info_label.text = "Nenhuma figura sua neste nó."


func _deselect() -> void:
	selected_fig_id    = ""
	selected_bench_fig = {}
	reachable_set      = []


func _load_reachable(fig_id: String) -> void:
	ApiClient.get_movimentos(match_id, fig_id, my_player_id,
		func(result):
			if result == null: return
			var raw: Array = result.get("nodosAlcancaveis", [])
			reachable_set = []
			for v in raw:
				reachable_set.append(int(v))
			queue_redraw(),
		func(err_msg: String):
			info_label.text = "Erro ao calcular movimentos: %s" % err_msg
	)


func _move_figure(fig_id: String, target_nid: int) -> void:
	info_label.text = "Movendo figura..."
	ApiClient.mover_figura(match_id, fig_id, target_nid, my_player_id,
		func(result):
			if result == null:
				info_label.text = "Movimento inválido."
				return
			_deselect()
			if result.has("battle"):
				_show_battle_overlay(result["battle"], result.get("estado", {}))
			else:
				_apply_state(result.get("estado", result))
			queue_redraw()
	)


func _enter_board(fig_id: String, entry_nid: int) -> void:
	info_label.text = "Entrando no tabuleiro..."
	ApiClient.entrar_tabuleiro(match_id, fig_id, my_player_id, entry_nid,
		func(result):
			if result == null:
				info_label.text = "Entrada inválida."
				return
			_deselect()
			_apply_state(result.get("estado", result))
			queue_redraw()
	)


# ──────────────────────────────────────── Peças (sprites 2D) ─────────────────
var figure_sprites: Dictionary = {}  # figureId → Sprite2D

func _update_figures_2d(figures: Array) -> void:
	figures_by_node.clear()
	figures_on_bench.clear()
	bench_p1.clear(); bench_p2.clear()
	pc_p1.clear(); pc_p2.clear()
	var active_ids: Array = []

	for fig in figures:
		var fid:   String = fig["figureId"]
		var state: String = fig.get("state", "BENCH")
		var nid:   int    = fig.get("nodeId", -1)
		var owner: int    = fig.get("owner", 1)

		active_ids.append(fid)

		if state == "BENCH":
			(bench_p1 if owner == 1 else bench_p2).append(fig)
		elif state == "PC":
			(pc_p1 if owner == 1 else pc_p2).append(fig)

		if state != "ACTIVE" or nid < 0:
			if owner == my_player_num and state == "BENCH":
				figures_on_bench.append(fig)
			continue

		if not figures_by_node.has(nid):
			figures_by_node[nid] = []
		figures_by_node[nid].append(fig)

	# (Re)posiciona/(re)cria sprites — a posição real (com offset de empilhamento)
	# é calculada em conjunto com _draw() para os anéis ficarem alinhados.
	for nid in figures_by_node.keys():
		var figs: Array = figures_by_node[nid]
		var base_pos: Vector2 = node_pos2d.get(nid, Vector2.ZERO)
		for i in range(figs.size()):
			var fig: Dictionary = figs[i]
			var fid: String = fig["figureId"]
			var pos: Vector2 = base_pos + Vector2(i * 14, 0)

			if not figure_sprites.has(fid):
				figure_sprites[fid] = _make_piece_sprite(fig)
				pokemon_sprites.add_child(figure_sprites[fid])

			var spr: Sprite2D = figure_sprites[fid]
			spr.visible  = true
			spr.position = pos
			var tex: Texture2D = spr.texture
			if tex and tex.get_width() > 0:
				var factor: float = PIECE_SPRITE_SIZE / float(max(tex.get_width(), tex.get_height()))
				spr.scale = Vector2(factor, factor)

	# Sprites dos slots de banco/P.C. (fora do tabuleiro) — mesma lógica,
	# só que a posição vem dos slots fixos em vez de um nó do grafo.
	_position_off_board_sprites(bench_p1, true, true)
	_position_off_board_sprites(bench_p2, false, true)
	_position_off_board_sprites(pc_p1, true, false)
	_position_off_board_sprites(pc_p2, false, false)

	for fid in figure_sprites.keys():
		if not active_ids.has(fid):
			figure_sprites[fid].queue_free()
			figure_sprites.erase(fid)

	_update_bench_ui()


func _position_off_board_sprites(figs: Array, is_p1: bool, is_bench: bool) -> void:
	var target_size := PIECE_SPRITE_SIZE * (0.72 if is_bench else 0.5)
	for i in range(figs.size()):
		var fig: Dictionary = figs[i]
		var fid: String = fig["figureId"]
		var pos: Vector2 = _bench_slot_pos(i, is_p1) if is_bench else _pc_slot_pos(i, is_p1)

		if not figure_sprites.has(fid):
			figure_sprites[fid] = _make_piece_sprite(fig)
			pokemon_sprites.add_child(figure_sprites[fid])

		var spr: Sprite2D = figure_sprites[fid]
		spr.visible  = true
		spr.position = pos
		var tex: Texture2D = spr.texture
		if tex and tex.get_width() > 0:
			var factor: float = target_size / float(max(tex.get_width(), tex.get_height()))
			spr.scale = Vector2(factor, factor)


func _make_piece_sprite(fig: Dictionary) -> Sprite2D:
	var sprite_file: String = fig.get("spriteFile", "")
	var tex_path := "res://assets/sprites/pokemon/icons/" + sprite_file

	var spr := Sprite2D.new()
	spr.name = "piece_%s" % str(fig.get("figureId", "?")).left(8)
	if ResourceLoader.exists(tex_path):
		var tex: Texture2D = load(tex_path)
		spr.texture = tex
		if tex and tex.get_width() > 0:
			var scale_factor: float = PIECE_SPRITE_SIZE / float(max(tex.get_width(), tex.get_height()))
			spr.scale = Vector2(scale_factor, scale_factor)
	return spr


# ──────────────────────────────────────── UI do banco ─────────────────────────
func _update_bench_ui() -> void:
	for c in bench_list.get_children(): c.queue_free()

	for fig in figures_on_bench:
		var fid:   String = fig["figureId"]
		var name_: String = fig.get("pokemonName", "?")
		var pm:    int    = fig.get("pm", 2)
		var st:    String = fig.get("state", "BENCH")
		var shiny: bool   = fig.get("shiny", false)

		var btn := Button.new()
		btn.text = "%s%s (PM:%d)%s" % [
			"✨ " if shiny else "", name_, pm, " [B]" if st == "BENCH" else " [PC]"]
		btn.custom_minimum_size = Vector2(0, 28)
		if selected_bench_fig.get("figureId","") == fid:
			btn.modulate = C_SELECTED

		var fig_cap: Dictionary = fig.duplicate()
		btn.pressed.connect(func(): _select_bench_figure(fig_cap))
		bench_list.add_child(btn)


# ─────────────────────────────────────────── Battle Overlay ────────────────────
func _show_battle_overlay(battle: Dictionary, new_state: Dictionary) -> void:
	if _battle_overlay:
		_battle_overlay.queue_free()

	# Monta os dicts completos (nome, moves, shiny) do atacante/defensor a
	# partir do catálogo (a figura em si só traz id/estado/dano do lance).
	var attacker: Dictionary = _figure_battle_dict(battle.get("attackerFigureId",""))
	var defender: Dictionary = _figure_battle_dict(battle.get("defenderFigureId",""))

	_battle_overlay = BattleOverlayScene.instantiate()
	add_child(_battle_overlay)
	_battle_overlay.show_battle(attacker, defender, battle,
		func(): # on_close callback
			_battle_overlay = null
			_apply_state(new_state)
			queue_redraw()
			_maybe_trigger_bot_turn()
	)


## Encontra a figura pelo id no último estado conhecido e monta um dict
## {name, spriteFile, moves, shiny} pronto para BattleOverlay/BattleWheel.
func _figure_battle_dict(figure_id: String) -> Dictionary:
	var d: Dictionary = {}
	for fig in last_state.get("figures", []):
		if fig.get("figureId","") == figure_id:
			d["name"]       = fig.get("pokemonName", "?")
			d["spriteFile"] = fig.get("spriteFile", "")
			d["shiny"]      = fig.get("shiny", false)
			d["is_back"]    = fig.get("owner", 1) == my_player_num
			var cat = GameState.get_pokemon(fig.get("pokemonId", ""))
			if cat != null:
				d["moves"] = cat.get("moves", [])
			return d
	return d


# ────────────────────────────────────────────── Polling ───────────────────────
func _poll() -> void:
	if match_id.is_empty(): return
	ApiClient.get_estado_partida(match_id, func(result):
		if result == null:
			info_label.text = "Erro ao comunicar com o servidor."
			return
		_apply_state(result)
	)


func _apply_state(state: Dictionary) -> void:
	last_state = state

	if my_player_num == 0 and not my_player_id.is_empty():
		if my_player_id == state.get("player1Id",""):   my_player_num = 1
		elif my_player_id == state.get("player2Id",""): my_player_num = 2

	if state.has("figures"):
		_update_figures_2d(state["figures"])

	var turn: int = state.get("currentTurn", 0)
	var secs: int = state.get("secondsRemaining", 0)
	var my_turn := (turn == my_player_num)

	turn_label.text  = "Turno %d — Jogador %d%s" % [
		state.get("turnNumber", 1), turn,
		" (SEU TURNO ✓)" if my_turn else ""]
	timer_label.text = "%d:%02d" % [secs / 60, secs % 60]

	# Ação pendente: a figura que acabou de entrar/mover ainda tem PM sobrando
	# neste turno (o turno NÃO passou). Auto-seleciona ela e mostra o botão
	# "Passar" pra quem não quiser usar o resto do movimento.
	var pend_fig_raw: Variant = state.get("pendingActionFigureId", "")
	var pend_fig: String = "" if pend_fig_raw == null else str(pend_fig_raw)
	var pend_budget: int = int(state.get("pendingActionBudget", 0))
	pending_action_figure_id = pend_fig
	pending_action_budget = pend_budget

	if my_turn and not pend_fig.is_empty():
		btn_passar.visible = true
		btn_passar.text = "Passar (não usar os %d PM restantes)" % pend_budget
		if selected_fig_id != pend_fig:
			selected_fig_id = pend_fig
			selected_bench_fig = {}
			info_label.text = "Ainda restam %d PM — clique num nó verde pra continuar, ou clique em Passar." % pend_budget
			_load_reachable(pend_fig)
	else:
		btn_passar.visible = false

	if my_turn and info_label.text.is_empty():
		info_label.text = "Seu turno! Selecione uma figura."

	if state.get("status","") == "FINISHED":
		_handle_match_finished(state)
	else:
		_maybe_trigger_bot_turn()

	queue_redraw()


# ─────────────────────────────────────── Modo Solo — bot + recompensa ─────────
func _maybe_trigger_bot_turn() -> void:
	if not (GameState.is_solo_match or GameState.is_debug_match): return
	if match_id.is_empty(): return
	if last_state.get("status","") != "ACTIVE": return
	if last_state.get("currentTurn", 0) != 2: return  # bot é sempre jogador 2
	if _bot_turn_pending: return

	_bot_turn_pending = true
	await get_tree().create_timer(BOT_TURN_DELAY).timeout
	_bot_turn_pending = false
	if match_id.is_empty(): return  # partida pode ter acabado durante a espera

	ApiClient.debug_turno_bot(match_id, func(result):
		if result == null: return
		if result.get("matchOver", false):
			_apply_state(result.get("estado", {}))
			return
		if result.has("battle"):
			_show_battle_overlay(result["battle"], result.get("estado", {}))
		else:
			_apply_state(result.get("estado", {}))
			_maybe_trigger_bot_turn()  # bot pode agir de novo se ainda for o turno dele
	)


func _handle_match_finished(state: Dictionary) -> void:
	if _match_finished_handled: return
	_match_finished_handled = true
	poll_timer.stop()

	var winner: int = state.get("winnerId", 0)
	var reason: String = state.get("winReason", "")
	var i_won := winner == my_player_num

	battle_panel.visible = true
	battle_label.text = ("✅ Você venceu!" if i_won else "❌ Você perdeu.") + \
		("\n%s" % reason if not reason.is_empty() else "")

	if not GameState.is_solo_match:
		info_label.text = battle_label.text
		return

	if not i_won:
		info_label.text = "Tente novamente!"
		return

	# Vitória no Modo Solo: desbloqueia o próximo slot e, se foi o CHEFE
	# (slot 5), abre a caixa de recompensa.
	var nivel: int = GameState.solo_nivel_selecionado
	var slot:  int = GameState.solo_slot_atual
	GameState.unlock_solo_slot(nivel, slot)

	if slot == 5:
		_open_reward_box(nivel)
	else:
		info_label.text = "Vitória! Próximo oponente desbloqueado."


func _open_reward_box(nivel: int) -> void:
	info_label.text = "Nível concluído! Abrindo caixa de recompensa..."
	ApiClient.post_solo_recompensa(my_player_id, nivel, func(result):
		if result == null:
			info_label.text = "Nível concluído! (Não foi possível abrir a caixa — backend indisponível.)"
			return

		var pid:   String  = result.get("pokemonId", "")
		var pname: String  = result.get("pokemonName", "?")
		var shiny: bool    = result.get("shiny", false)
		var novo:  bool    = result.get("novo", false)

		if not GameState.unlocked_pokemon_ids.has(pid):
			GameState.unlocked_pokemon_ids.append(pid)

		var msg: String
		if shiny:
			msg = "🎁 Caixa aberta: %s ✨ SHINY! (você já tinha a versão normal)" % pname
		elif novo:
			msg = "🎁 Caixa aberta: %s é agora seu!" % pname
		else:
			msg = "🎁 Caixa aberta: %s (repetido)" % pname

		battle_label.text += "\n\n" + msg
		info_label.text = msg
	)
