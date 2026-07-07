extends CanvasLayer
class_name BattleOverlay

## BattleOverlay
## Tela de transição de batalha — overlay 2D por cima do tabuleiro (3D ou 2D).
## Mostra os dois Pokémon se enfrentando, cada um com sua própria roleta,
## girando simultaneamente até pararem nos ângulos vindos do backend
## (battle.anguloAtacante / battle.anguloDefensor), e then revela o resultado
## (outcome da tabela de cores) com destaque de cor.
##
## Uso:
##   var overlay := preload("res://scripts/components/BattleOverlay.tscn").instantiate()
##   get_tree().root.add_child(overlay)
##   overlay.show_battle(atacante_pokemon_dict, defensor_pokemon_dict, battle_result_dict, func(): overlay.queue_free())

signal battle_finished

@onready var fade_root:     Control       = %FadeRoot
@onready var bg_dim:        ColorRect     = %BgDim
@onready var attacker_name: Label         = %AttackerName
@onready var defender_name: Label         = %DefenderName
@onready var attacker_sprite: TextureRect = %AttackerSprite
@onready var defender_sprite: TextureRect = %DefenderSprite
@onready var attacker_wheel_slot: Control = %AttackerWheelSlot
@onready var defender_wheel_slot: Control = %DefenderWheelSlot
@onready var result_label:  Label         = %ResultLabel
@onready var vs_label:      Label         = %VsLabel
@onready var continue_hint: Label         = %ContinueHint

var attacker_wheel: BattleWheel
var defender_wheel: BattleWheel
var _on_done: Callable

const OUTCOME_TEXT := {
	"ATTACKER_WINS": "Atacante vence!",
	"DEFENDER_WINS": "Defensor vence!",
	"COMPARE_ATTACKER_WINS": "Atacante vence (comparação)!",
	"COMPARE_DEFENDER_WINS": "Defensor vence (comparação)!",
	"COMPARE_TIE": "Empate — ambos seguem em campo",
	"DRAW": "Batalha cancelada (Azul)",
	"BOTH_FAINT": "Ambos desmaiam!",
}

const OUTCOME_COLOR := {
	"ATTACKER_WINS": Color(0.3, 0.85, 0.4),
	"COMPARE_ATTACKER_WINS": Color(0.3, 0.85, 0.4),
	"DEFENDER_WINS": Color(0.9, 0.35, 0.3),
	"COMPARE_DEFENDER_WINS": Color(0.9, 0.35, 0.3),
	"COMPARE_TIE": Color(0.8, 0.8, 0.3),
	"DRAW": Color(0.4, 0.6, 0.9),
	"BOTH_FAINT": Color(0.9, 0.5, 0.2),
}


func _ready() -> void:
	layer = 100  # sempre por cima de tudo, inclusive do Viewport 3D
	result_label.visible = false
	continue_hint.visible = false

	attacker_wheel = BattleWheel.new()
	attacker_wheel.radius = 70
	attacker_wheel_slot.add_child(attacker_wheel)

	defender_wheel = BattleWheel.new()
	defender_wheel.radius = 70
	defender_wheel_slot.add_child(defender_wheel)

	bg_dim.gui_input.connect(func(event):
		if result_label.visible and event is InputEventMouseButton and event.pressed:
			_finish()
	)


## Inicia a sequência de batalha.
## attacker / defender: Dictionary do Pokémon (precisa de "name", "moves", sprite opcional)
## battle: Dictionary vindo de MatchController (attackerMoveName, attackerColor,
##         attackerDamage, attackerAngle, defenderMoveName, defenderColor,
##         defenderDamage, defenderAngle, outcome, ...)
func show_battle(attacker: Dictionary, defender: Dictionary, battle: Dictionary, on_done: Callable = Callable()) -> void:
	_on_done = on_done
	attacker_name.text = str(attacker.get("name", attacker.get("pokemonName", "?")))
	defender_name.text = str(defender.get("name", defender.get("pokemonName", "?")))

	_try_load_sprite(attacker_sprite, attacker)
	_try_load_sprite(defender_sprite, defender)

	attacker_wheel.set_moves(attacker.get("moves", []))
	defender_wheel.set_moves(defender.get("moves", []))

	result_label.visible = false
	continue_hint.visible = false
	vs_label.text = "VS"

	fade_root.modulate.a = 0.0
	var fade_in := create_tween()
	fade_in.tween_property(fade_root, "modulate:a", 1.0, 0.3)
	await fade_in.finished

	var atk_angle: float = float(battle.get("attackerAngle", battle.get("anguloAtacante", 720.0)))
	var def_angle: float = float(battle.get("defenderAngle", battle.get("anguloDefensor", 720.0)))

	var pending: int = 2
	var revealed: bool = false
	var reveal_once := func():
		if revealed: return
		revealed = true
		_reveal_result(battle)
	var on_one_done := func():
		pending -= 1
		if pending <= 0:
			reveal_once.call()

	attacker_wheel.spin_to(atk_angle, 1.4, func(): on_one_done.call())
	defender_wheel.spin_to(def_angle, 1.4, func(): on_one_done.call())

	# Rede de segurança: se por algum motivo uma das rodas não terminar de
	# girar (nó removido da árvore, tween interrompido, etc.), revela o
	# resultado mesmo assim depois de um tempo de margem — o jogo nunca deve
	# ficar travado nessa tela sem nenhuma forma de continuar.
	await get_tree().create_timer(2.5).timeout
	reveal_once.call()


## Carrega o sprite de BATALHA (front = adversário / back = seu Pokémon),
## usando a variante shiny quando a figura for shiny (bônus da caixa de
## recompensa do Modo Solo). pokemon precisa de "spriteFile" (ou "id"/
## "pokemonId"), "is_back" (bool) e opcionalmente "shiny" (bool).
func _try_load_sprite(rect: TextureRect, pokemon: Dictionary) -> void:
	var is_back: bool = bool(pokemon.get("is_back", false))
	var shiny:   bool = bool(pokemon.get("shiny", false))
	var path: String = GameState.battle_sprite_path(pokemon, is_back, shiny)
	if ResourceLoader.exists(path):
		rect.texture = load(path)
	elif shiny:
		# fallback: se a variante shiny não existir por algum motivo, usa a normal
		var normal_path: String = GameState.battle_sprite_path(pokemon, is_back, false)
		if ResourceLoader.exists(normal_path):
			rect.texture = load(normal_path)


func _reveal_result(battle: Dictionary) -> void:
	var atk_move: String = str(battle.get("attackerMoveName", battle.get("atacante", "?")))
	var def_move: String = str(battle.get("defenderMoveName", battle.get("defensor", "?")))
	var atk_dmg: int = int(battle.get("attackerDamage", 0))
	var def_dmg: int = int(battle.get("defenderDamage", 0))

	var atk_extra: String = (" (%d dano)" % atk_dmg) if atk_dmg > 0 else ""
	var def_extra: String = (" (%d dano)" % def_dmg) if def_dmg > 0 else ""
	vs_label.text = "%s%s   VS   %s%s" % [atk_move, atk_extra, def_move, def_extra]

	var outcome: String = str(battle.get("outcome", "?"))
	result_label.text = OUTCOME_TEXT.get(outcome, outcome)
	result_label.add_theme_color_override("font_color", OUTCOME_COLOR.get(outcome, Color.WHITE))
	result_label.visible = true
	continue_hint.visible = true

	# Pequeno "pulso" de destaque no Pokémon vencedor
	var winner_sprite: TextureRect = null
	if outcome in ["ATTACKER_WINS", "COMPARE_ATTACKER_WINS"]:
		winner_sprite = attacker_sprite
	elif outcome in ["DEFENDER_WINS", "COMPARE_DEFENDER_WINS"]:
		winner_sprite = defender_sprite
	if winner_sprite:
		var pulse := create_tween()
		pulse.set_loops(2)
		pulse.tween_property(winner_sprite, "scale", Vector2(1.15, 1.15), 0.25)
		pulse.tween_property(winner_sprite, "scale", Vector2(1.0, 1.0), 0.25)


func _finish() -> void:
	var fade_out := create_tween()
	fade_out.tween_property(fade_root, "modulate:a", 0.0, 0.25)
	await fade_out.finished
	battle_finished.emit()
	if _on_done.is_valid(): _on_done.call()
	queue_free()  # crítico: sem isso o BgDim (tela cheia) continua vivo,
	              # invisível, mas bloqueando todo clique futuro no tabuleiro.
