extends Control

## LevelSelect — Tela de seleção de nível do Modo Solo.
##
## Exibe 6 botões de nível em grade 2×3. Cada nível mostra o número e se está
## desbloqueado (nível 1 sempre disponível; próximo desbloqueia após concluir o
## chefe — rastreado em GameState.solo_levels_unlocked, não persistido entre
## sessões pois o projeto não tem sistema de save ainda).

@onready var btn_voltar:        Button     = %BtnVoltar
@onready var nivel_grid:        GridContainer = %NivelGrid
@onready var status_label:      Label      = %StatusLabel

func _ready() -> void:
	btn_voltar.pressed.connect(func():
		get_tree().change_scene_to_file("res://scenes/menu/MainMenu.tscn"))

	_build_level_buttons()


func _build_level_buttons() -> void:
	for c in nivel_grid.get_children(): c.queue_free()

	for lvl in range(1, 7):
		var btn := Button.new()
		btn.custom_minimum_size = Vector2(120, 80)

		var unlocked: bool = GameState.is_solo_level_unlocked(lvl)
		btn.text = "Nível %d\n%s" % [lvl, "" if unlocked else "🔒"]
		btn.disabled = not unlocked

		var lvl_captured: int = lvl
		btn.pressed.connect(func(): _on_nivel_pressed(lvl_captured))

		nivel_grid.add_child(btn)


func _on_nivel_pressed(nivel: int) -> void:
	GameState.solo_nivel_selecionado = nivel
	get_tree().change_scene_to_file("res://scenes/solo/SoloBattle.tscn")
