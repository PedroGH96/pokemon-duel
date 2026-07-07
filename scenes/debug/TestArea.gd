extends Control

## Área de Teste — Tabuleiro & Roleta
##
## Aba "Tabuleiro": cria uma partida de debug (Jogador vs. Bot, via
## POST /debug/partida) e abre o MESMO Board.tscn usado no Multiplayer e no
## Modo Solo — ou seja, testa o tabuleiro de verdade (grafo 2D, movimentação,
## batalhas, cerco, vitória), sem precisar passar por Lobby/Sala nem por
## Nível/Oponente do Solo. GameState.is_debug_match=true garante que o botão
## "Voltar" do tabuleiro volte para cá, e que o bot jogue sozinho.
##
## Aba "Roleta": gira a roleta de qualquer um dos 161 Pokémon do catálogo.
## O giro em si é 100% local (usa BattleWheel + os pesos de "moves" do
## catálogo já carregado) — não precisa do backend rodando. Um botão
## opcional consulta GET /debug/roleta/{id}?vezes=N no backend para
## comparar a frequência estatística real.

const BattleWheelScript := preload("res://scripts/components/BattleWheel.gd")

@onready var btn_voltar:        Button        = %BtnVoltar
@onready var deck_p1_edit:      LineEdit      = %DeckP1Edit
@onready var deck_p2_edit:      LineEdit      = %DeckP2Edit
@onready var btn_criar_partida: Button        = %BtnCriarPartida
@onready var board_status:      Label         = %BoardStatus

@onready var pokemon_select:    OptionButton  = %PokemonSelect
@onready var btn_girar:         Button        = %BtnGirar
@onready var result_label:      Label         = %ResultLabel
@onready var vezes_edit:        LineEdit      = %VezesEdit
@onready var btn_estatisticas:  Button        = %BtnEstatisticas
@onready var moves_list:        VBoxContainer = %MovesList
@onready var wheel_slot:        CenterContainer = %WheelSlot

var wheel: Control = null
var current_pokemon: Dictionary = {}


func _ready() -> void:
	btn_voltar.pressed.connect(func(): get_tree().change_scene_to_file("res://scenes/menu/MainMenu.tscn"))

	btn_criar_partida.pressed.connect(_on_criar_partida)

	wheel = BattleWheelScript.new()
	wheel.radius = 120.0
	wheel_slot.add_child(wheel)

	btn_girar.pressed.connect(_on_girar)
	btn_estatisticas.pressed.connect(_on_estatisticas)
	pokemon_select.item_selected.connect(_on_pokemon_selected)

	_populate_pokemon_select()


# ═══════════════════════════════════════════════ Aba Tabuleiro ═══════════════
func _on_criar_partida() -> void:
	var deck_p1 := _parse_deck(deck_p1_edit.text)
	var deck_p2 := _parse_deck(deck_p2_edit.text)

	if deck_p1.is_empty() or deck_p2.is_empty():
		board_status.text = "⚠ Informe ao menos 1 id válido em cada deck."
		return

	board_status.text = "Criando partida de teste..."
	btn_criar_partida.disabled = true

	ApiClient.request("POST", "/debug/partida",
		{"deckP1": deck_p1, "deckP2": deck_p2},
		func(result):
			btn_criar_partida.disabled = false
			if result == null:
				board_status.text = "❌ Erro ao criar partida. O backend está rodando (mvn spring-boot:run)?"
				return

			GameState.current_match_id = result.get("matchId", "")
			GameState.player_id        = result.get("player1Id", "debug-player-1")
			GameState.is_debug_match   = true
			GameState.is_solo_match    = false

			get_tree().change_scene_to_file("res://scenes/board/Board.tscn")
	)


func _parse_deck(text: String) -> Array:
	var ids: Array = []
	for raw in text.split(","):
		var id := raw.strip_edges()
		if id.is_empty(): continue
		if not GameState.catalog_by_id.has(id):
			board_status.text = "⚠ Pokémon desconhecido: \"%s\"" % id
			return []
		ids.append(id)
	return ids


# ═══════════════════════════════════════════════════ Aba Roleta ══════════════
func _populate_pokemon_select() -> void:
	pokemon_select.clear()

	if GameState.catalog.is_empty():
		result_label.text = "⚠ Catálogo vazio — o backend está rodando (mvn spring-boot:run)?"
		btn_girar.disabled = true
		btn_estatisticas.disabled = true
		return

	for i in range(GameState.catalog.size()):
		var p: Dictionary = GameState.catalog[i]
		pokemon_select.add_item("ID-%d  %s" % [p.get("dexId", 0), p.get("name", "?")], i)

	pokemon_select.select(0)
	_on_pokemon_selected(0)


func _on_pokemon_selected(index: int) -> void:
	if index < 0 or index >= GameState.catalog.size(): return
	current_pokemon = GameState.catalog[index]
	wheel.set_moves(current_pokemon.get("moves", []))
	result_label.text = "Pronto para girar."
	_populate_moves_list()


func _populate_moves_list() -> void:
	for c in moves_list.get_children(): c.queue_free()

	var moves: Array = current_pokemon.get("moves", [])
	var total := 0
	for mv in moves: total += int(mv.get("percentage", 0))
	if total <= 0: total = 1

	var title := Label.new()
	title.text = "Roleta de %s (peso total: %d):" % [current_pokemon.get("name","?"), total]
	title.add_theme_font_size_override("font_size", 11)
	moves_list.add_child(title)

	for mv in moves:
		var pct: float = float(mv.get("percentage", 0)) * 100.0 / float(total)
		var l := Label.new()
		l.text = "• %s [%s] — %.1f%% — dano %s" % [
			mv.get("name","?"), mv.get("color","?"), pct, str(mv.get("damage", 0))]
		l.add_theme_font_size_override("font_size", 10)
		moves_list.add_child(l)


func _on_girar() -> void:
	if wheel.is_spinning: return
	var moves: Array = current_pokemon.get("moves", [])
	if moves.is_empty():
		result_label.text = "Este Pokémon não tem moves cadastrados."
		return

	btn_girar.disabled = true
	result_label.text = "Girando..."

	# Ângulo final uniformemente aleatório + voltas extras — como os
	# segmentos já são proporcionais ao peso de cada move, isso reproduz
	# exatamente a mesma distribuição de probabilidade do servidor.
	var target: float = wheel.current_rotation + 360.0 * 4.0 + randf_range(0.0, 360.0)
	wheel.spin_to(target, 1.4, func():
		btn_girar.disabled = false
		var idx: int = wheel.segment_at_pointer()
		if idx < 0 or idx >= moves.size():
			result_label.text = "Giro concluído (não foi possível identificar o segmento)."
			return
		var mv: Dictionary = moves[idx]
		var effect: String = str(mv.get("statusEffect", mv.get("effect", "")))
		var extra: String = " — %s" % effect if not effect.is_empty() and effect != "NONE" else ""
		result_label.text = "🎯 %s [%s] — dano %s%s" % [
			mv.get("name","?"), mv.get("color","?"), str(mv.get("damage", 0)), extra]
	)


func _on_estatisticas() -> void:
	var pid: String = str(current_pokemon.get("id",""))
	if pid.is_empty(): return
	var vezes: int = clamp(int(vezes_edit.text) if vezes_edit.text.is_valid_int() else 100, 1, 1000)

	result_label.text = "Consultando estatísticas no backend..."
	ApiClient.request("GET", "/debug/roleta/%s?vezes=%d" % [pid, vezes], {}, func(result):
		if result == null:
			result_label.text = "❌ Erro ao consultar estatísticas. O backend está rodando?"
			return
		var freq: Dictionary = result.get("frequencia", {})
		var lines: Array = []
		for k in freq.keys():
			lines.append("%s: %d/%d" % [k, freq[k], vezes])
		result_label.text = "📊 %d giros:\n%s" % [vezes, "\n".join(lines)]
	)
