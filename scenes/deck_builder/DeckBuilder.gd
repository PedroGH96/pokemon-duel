extends Control

## Tela 2 — Montagem de Deck
## Grade com todas as figuras desbloqueadas (raridade indicada pela cor da borda).
## Clique para selecionar até 6 figuras para o deck.
## (Placas de poder: TODO — aguardando catálogo de placas no backend)

const RARITY_COLORS := {
	"C": Color(0.7, 0.7, 0.7),     # cinza
	"UC": Color(0.4, 0.8, 0.4),    # verde
	"R": Color(0.4, 0.6, 1.0),     # azul
	"EX": Color(0.8, 0.4, 0.9),    # roxo
	"UX": Color(1.0, 0.75, 0.1),   # dourado
}

@onready var grid: GridContainer = %CatalogGrid
@onready var deck_container: HBoxContainer = %DeckContainer
@onready var deck_label: Label = %DeckLabel
@onready var btn_back: Button = %BtnBack
@onready var detail_panel: PanelContainer = %DetailPanel
@onready var detail_name: Label = %DetailName
@onready var detail_info: Label = %DetailInfo
@onready var detail_ability: Label = %DetailAbility
@onready var detail_moves: VBoxContainer = %DetailMoves


func _ready() -> void:
	btn_back.pressed.connect(func(): get_tree().change_scene_to_file("res://scenes/menu/MainMenu.tscn"))

	# Em desenvolvimento: se o jogador ainda não tiver figuras desbloqueadas,
	# usa o catálogo inteiro para poder testar a tela.
	if GameState.unlocked_pokemon_ids.is_empty():
		for p in GameState.catalog:
			GameState.unlocked_pokemon_ids.append(p["id"])

	_populate_grid()
	_update_deck_view()

	if GameState.catalog.is_empty():
		detail_name.text = "⚠ Nenhum Pokémon carregado"
		detail_info.text = "O catálogo está vazio. Verifique se o backend está rodando " + \
			"(mvn spring-boot:run) e volte para o Menu Principal para recarregar."


func _populate_grid() -> void:
	for child in grid.get_children():
		child.queue_free()

	for pokemon_id in GameState.unlocked_pokemon_ids:
		var pokemon = GameState.get_pokemon(pokemon_id)
		if pokemon == null:
			continue
		grid.add_child(_make_card(pokemon))


func _make_card(pokemon: Dictionary) -> Control:
	var panel := PanelContainer.new()
	panel.custom_minimum_size = Vector2(96, 116)

	var style := StyleBoxFlat.new()
	style.bg_color = Color(0.15, 0.15, 0.18)
	var rarity: String = pokemon.get("rarity", "C")
	style.border_color = RARITY_COLORS.get(rarity, Color.WHITE)
	style.set_border_width_all(3)
	style.set_corner_radius_all(6)
	panel.add_theme_stylebox_override("panel", style)

	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 2)
	panel.add_child(box)

	var sprite := TextureRect.new()
	sprite.custom_minimum_size = Vector2(64, 64)
	sprite.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
	sprite.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
	var tex := load(GameState.sprite_path(pokemon))
	if tex:
		sprite.texture = tex
	box.add_child(sprite)

	var name_label := Label.new()
	name_label.text = pokemon.get("name", "?")
	name_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	name_label.add_theme_font_size_override("font_size", 11)
	box.add_child(name_label)

	var rarity_label := Label.new()
	rarity_label.text = "ID-%d · %s" % [pokemon.get("dexId", 0), rarity]
	rarity_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	rarity_label.add_theme_font_size_override("font_size", 9)
	box.add_child(rarity_label)

	var button := Button.new()
	button.flat = true
	button.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	button.size_flags_vertical = Control.SIZE_EXPAND_FILL
	button.add_theme_color_override("font_color", Color(0, 0, 0, 0))
	button.add_theme_color_override("font_hover_color", Color(0, 0, 0, 0))
	panel.add_child(button)
	button.pressed.connect(func(): _on_card_pressed(pokemon))

	return panel


func _on_card_pressed(pokemon: Dictionary) -> void:
	_show_detail(pokemon)

	if GameState.deck_ids.has(pokemon["id"]):
		GameState.remove_from_deck(pokemon["id"])
	else:
		if not GameState.add_to_deck(pokemon["id"]):
			deck_label.text = "Deck completo (máx. 6) ou figura já adicionada."
			return

	_update_deck_view()


func _show_detail(pokemon: Dictionary) -> void:
	detail_panel.visible = true
	detail_name.text = "ID-%d %s" % [pokemon.get("dexId", 0), pokemon.get("name", "?")]

	var type_str: String = pokemon.get("type", "")
	if pokemon.get("type2", null) != null:
		type_str += " / %s" % pokemon["type2"]

	detail_info.text = "Tipo: %s   |   Raridade: %s   |   PM: %d" % [
		type_str, pokemon.get("rarity", "?"), pokemon.get("pm", 0)]

	var ability = pokemon.get("specialAbility", null)
	detail_ability.text = ability if ability != null else "(sem habilidade especial)"

	for child in detail_moves.get_children():
		child.queue_free()

	for move in pokemon.get("moves", []):
		var line := Label.new()
		var dmg: int = move.get("damage", 0)
		var dmg_str: String = (" — %d dano" % dmg) if dmg > 0 else ""
		line.text = "[%2d] %-18s (%s)%s" % [
			move.get("percentage", 0), move.get("name", "?"), move.get("color", "?"), dmg_str]
		line.add_theme_font_size_override("font_size", 12)
		detail_moves.add_child(line)


func _update_deck_view() -> void:
	for child in deck_container.get_children():
		child.queue_free()

	deck_label.text = "Deck: %d / 6" % GameState.deck_ids.size()

	for pokemon_id in GameState.deck_ids:
		var pokemon = GameState.get_pokemon(pokemon_id)
		if pokemon == null:
			continue
		var sprite := TextureRect.new()
		sprite.custom_minimum_size = Vector2(48, 48)
		sprite.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		sprite.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		var tex := load(GameState.sprite_path(pokemon))
		if tex:
			sprite.texture = tex
		deck_container.add_child(sprite)
