extends Control

## SoloBattle — Seleção de oponente do Modo Solo.
##
## Mostra os 5 inimigos + 1 chefe do nível selecionado em GameState.
## O jogador clica num oponente desbloqueado para lutar via POST /solo/partida.
## Após criar a partida, navega para o tabuleiro real (Board.tscn), que passa
## a cuidar de tudo: movimentação, roleta de batalha, turno automático do bot
## e, ao vencer o chefe (slot 5), abertura da caixa de recompensa.

@onready var titulo_label:     Label          = %TituloLabel
@onready var oponentes_grid:   GridContainer  = %OponentesGrid
@onready var info_label:       Label          = %InfoLabel
@onready var btn_voltar:       Button         = %BtnVoltar

var nivel: int = 1
var nivel_data: Array = []
var player_id: String = ""

func _ready() -> void:
	nivel     = GameState.solo_nivel_selecionado
	player_id = GameState.player_id if not GameState.player_id.is_empty() else "solo-player-1"
	GameState.player_id = player_id

	titulo_label.text = "🎮 Modo Solo — Nível %d" % nivel
	btn_voltar.pressed.connect(_on_voltar_nivel)

	info_label.text = "Carregando nível %d..." % nivel
	ApiClient.request("GET", "/solo/niveis", {}, func(result):
		if result == null:
			info_label.text = "Erro ao carregar níveis. O backend está rodando (mvn spring-boot:run)?"
			return
		for n in result:
			if n.get("nivel", 0) == nivel:
				nivel_data = n.get("oponentes", [])
				break
		_build_opponent_grid()
	)


func _build_opponent_grid() -> void:
	for c in oponentes_grid.get_children(): c.queue_free()

	if nivel_data.is_empty():
		info_label.text = "Nenhum dado de nível encontrado."
		return

	for oponente in nivel_data:
		var slot: int      = oponente.get("slot", 0)
		var tipo: String    = oponente.get("tipo", "INIMIGO")
		var nome: String    = oponente.get("nome", "?")
		var avatar: String  = oponente.get("avatar", "")

		var unlocked: bool = GameState.is_solo_slot_unlocked(nivel, slot)
		var beaten: bool   = GameState.is_solo_slot_beaten(nivel, slot)

		var panel := PanelContainer.new()
		panel.custom_minimum_size = Vector2(100, 140)

		var vbox := VBoxContainer.new()
		vbox.alignment = BoxContainer.ALIGNMENT_CENTER
		panel.add_child(vbox)

		var tex_path := "res://assets/sprites/npc/" + avatar
		if ResourceLoader.exists(tex_path):
			var img := TextureRect.new()
			img.texture = load(tex_path)
			img.custom_minimum_size = Vector2(64, 64)
			img.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
			img.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
			img.modulate = Color(1,1,1,1) if unlocked else Color(0.4,0.4,0.4,1)
			vbox.add_child(img)
		else:
			var placeholder := Label.new()
			placeholder.text = "❓"
			placeholder.add_theme_font_size_override("font_size", 32)
			placeholder.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
			vbox.add_child(placeholder)

		var nome_label := Label.new()
		nome_label.text = ("⭐ " if tipo == "CHEFE" else "") + nome
		nome_label.add_theme_font_size_override("font_size", 9)
		nome_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		nome_label.autowrap_mode = TextServer.AUTOWRAP_WORD
		vbox.add_child(nome_label)

		var btn := Button.new()
		if not unlocked:
			btn.text = "🔒"
		elif beaten:
			btn.text = "Revanche"
		else:
			btn.text = "Lutar"
		btn.disabled = not unlocked
		var slot_cap: int = slot
		btn.pressed.connect(func(): _lutar(slot_cap))
		vbox.add_child(btn)

		oponentes_grid.add_child(panel)

	info_label.text = "Selecione um oponente para batalhar."


func _lutar(slot: int) -> void:
	info_label.text = "Criando partida..."

	var body := {
		"nivel":     nivel,
		"slot":      slot,
		"jogadorId": player_id,
		"deckJogador": GameState.deck_ids if not GameState.deck_ids.is_empty() \
		                else ["bulbasaur","charmander","squirtle","pikachu","machop","eevee"]
	}
	ApiClient.request("POST", "/solo/partida", body, func(result):
		if result == null:
			info_label.text = "Erro ao criar partida. O backend está rodando?"
			return

		var match_id: String = result.get("matchId", "")
		GameState.current_match_id = match_id
		GameState.is_solo_match    = true
		GameState.is_debug_match   = false
		GameState.solo_slot_atual  = slot

		get_tree().change_scene_to_file("res://scenes/board/Board.tscn")
	)


func _on_voltar_nivel() -> void:
	get_tree().change_scene_to_file("res://scenes/solo/LevelSelect.tscn")
