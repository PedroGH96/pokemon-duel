extends Control

## Tela 1 — Menu Principal
## Botões: Montar Deck, Modo Solo, Multiplayer.
## Exibe as 6 figuras iniciais (starters) do jogador.

@onready var status_label: Label = %StatusLabel
@onready var starters_container: HBoxContainer = %StartersContainer
@onready var btn_deck: Button = %BtnDeck
@onready var btn_solo: Button = %BtnSolo
@onready var btn_multiplayer: Button = %BtnMultiplayer
@onready var btn_teste: Button = %BtnTeste


func _ready() -> void:
	btn_deck.pressed.connect(_on_deck_pressed)
	btn_solo.pressed.connect(_on_solo_pressed)
	btn_multiplayer.pressed.connect(_on_multiplayer_pressed)
	btn_teste.pressed.connect(_on_teste_pressed)

	status_label.text = "Conectando ao servidor..."

	if GameState.catalog.is_empty():
		GameState.load_catalog(func(success): _on_catalog_ready(success))
	else:
		_on_catalog_ready(true)


func _on_catalog_ready(success: bool) -> void:
	if not success:
		status_label.text = "Não foi possível conectar ao servidor (%s).\nVerifique se o backend está em execução." % ApiClient.BASE_URL
		return

	status_label.text = "%d Pokémon carregados do servidor." % GameState.catalog.size()
	_show_starters()


## Mostra as figuras iniciais do jogador (ou os starters padrão, se ainda
## não houver um jogador registrado).
func _show_starters() -> void:
	for child in starters_container.get_children():
		child.queue_free()

	var ids: Array = GameState.unlocked_pokemon_ids
	if ids.is_empty():
		# Sem jogador registrado ainda — mostra os 6 starters padrão do backend
		# (a lista oficial vem de PokemonCatalogService.STARTER_IDS).
		ids = ["bulbasaur", "charmander", "squirtle", "pikachu", "machop", "eevee"]

	for id in ids:
		var pokemon = GameState.get_pokemon(id)
		if pokemon == null:
			continue

		var box := VBoxContainer.new()

		var sprite := TextureRect.new()
		sprite.custom_minimum_size = Vector2(64, 64)
		sprite.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
		sprite.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		var tex := load(GameState.sprite_path(pokemon))
		if tex:
			sprite.texture = tex
		box.add_child(sprite)

		var label := Label.new()
		label.text = pokemon.get("name", "?")
		label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		box.add_child(label)

		starters_container.add_child(box)


func _on_deck_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/deck_builder/DeckBuilder.tscn")


func _on_solo_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/solo/LevelSelect.tscn")


func _on_multiplayer_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/lobby/Lobby.tscn")


func _on_teste_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/debug/TestArea.tscn")
