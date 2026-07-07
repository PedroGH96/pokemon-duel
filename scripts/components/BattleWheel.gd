extends Control
class_name BattleWheel

## BattleWheel
## Roleta universal desenhada 100% por código — funciona para qualquer um
## dos 161 Pokémon, adaptando-se automaticamente ao número de segmentos e
## ao peso ("percentage") de cada um, exatamente como vem do backend.
##
## Uso:
##   var wheel := BattleWheel.new()
##   add_child(wheel)
##   wheel.set_moves(pokemon_dict["moves"])   # Array de Dictionary (Move)
##   wheel.spin_to(move_index, angle_graus, on_finished_callback)
##
## Cores por MoveColor (seguem a tabela oficial do jogo):
##   RED → vermelho (Miss)
##   WHITE → branco (dano numérico)
##   PURPLE → roxo (efeito de status)
##   GOLD → dourado (dano numérico, vence roxo)
##   BLUE → azul (cancela a batalha, vence tudo)

signal spin_finished(move_index: int)

const COLOR_MAP := {
	"RED":    Color(0.85, 0.25, 0.25),
	"WHITE":  Color(0.92, 0.92, 0.92),
	"PURPLE": Color(0.55, 0.35, 0.75),
	"GOLD":   Color(0.85, 0.65, 0.15),
	"BLUE":   Color(0.25, 0.55, 0.9),
}

const TEXT_ON_COLOR := {
	"RED":    Color.WHITE,
	"WHITE":  Color(0.15, 0.15, 0.15),
	"PURPLE": Color.WHITE,
	"GOLD":   Color(0.2, 0.15, 0.0),
	"BLUE":   Color.WHITE,
}

@export var radius: float = 90.0
@export var pointer_size: float = 14.0

var moves: Array = []          # Array de Dictionary {name, color, percentage, damage,...}
var current_rotation: float = 0.0  # graus, aplicado ao desenho
var is_spinning: bool = false

func _ready() -> void:
	custom_minimum_size = Vector2(radius * 2 + 20, radius * 2 + 40)


func set_moves(new_moves: Array) -> void:
	moves = new_moves
	current_rotation = 0.0
	queue_redraw()


## Anima a roleta girando até o ângulo informado (em graus, já incluindo
## as voltas extras — o backend já manda esse valor pronto em "anguloAtacante"
## ou "anguloDefensor" do resultado de batalha).
func spin_to(target_angle: float, duration: float = 1.2, on_done: Callable = Callable()) -> void:
	is_spinning = true
	var tween := create_tween()
	tween.set_trans(Tween.TRANS_CUBIC).set_ease(Tween.EASE_OUT)
	tween.tween_method(_set_rotation_deg, current_rotation, target_angle, duration)
	tween.finished.connect(func():
		is_spinning = false
		current_rotation = fmod(target_angle, 360.0)
		if on_done.is_valid(): on_done.call()
		spin_finished.emit(-1)
	)


func _set_rotation_deg(deg: float) -> void:
	current_rotation = deg
	queue_redraw()


func _draw() -> void:
	if moves.is_empty(): return

	var center: Vector2 = Vector2(radius + 10, radius + 10)
	var total: int = 0
	for mv: Dictionary in moves:
		total += int(mv.get("percentage", 0))
	if total <= 0: return

	# Desenha cada segmento como um arco (Polygon do tipo "pizza slice")
	var start_angle: float = -90.0 + current_rotation  # começa no topo (12h)
	for mv: Dictionary in moves:
		var weight: int = int(mv.get("percentage", 0))
		var sweep: float = (float(weight) / float(total)) * 360.0
		var color_name: String = str(mv.get("color", "RED"))
		var color: Color = COLOR_MAP.get(color_name, Color.GRAY)

		_draw_segment(center, radius, start_angle, sweep, color)

		# Label do nome no meio do segmento (só se o segmento for largo o
		# suficiente para não poluir visualmente)
		if sweep > 18.0:
			var mid_angle_rad: float = deg_to_rad(start_angle + sweep / 2.0)
			var label_pos: Vector2 = center + Vector2(cos(mid_angle_rad), sin(mid_angle_rad)) * (radius * 0.62)
			var text_color: Color = TEXT_ON_COLOR.get(color_name, Color.BLACK)
			draw_string(ThemeDB.fallback_font, label_pos - Vector2(20, 0), str(mv.get("name","")).left(10),
				HORIZONTAL_ALIGNMENT_CENTER, 40, 10, text_color)

		start_angle += sweep

	# Borda externa
	draw_arc(center, radius, 0, TAU, 64, Color(0.1,0.1,0.1), 3.0)

	# Ponteiro fixo no topo (sempre aponta para cima — é a roleta que gira)
	var p1: Vector2 = center + Vector2(0, -radius - 4)
	var p2: Vector2 = center + Vector2(-pointer_size * 0.5, -radius - 4 - pointer_size)
	var p3: Vector2 = center + Vector2(pointer_size * 0.5, -radius - 4 - pointer_size)
	draw_polygon(PackedVector2Array([p1, p2, p3]), PackedColorArray([Color(1,0.85,0.1)]))

	# Centro (pino)
	draw_circle(center, 6.0, Color(0.15, 0.15, 0.15))


func _draw_segment(center: Vector2, r: float, start_deg: float, sweep_deg: float, color: Color) -> void:
	var points := PackedVector2Array()
	points.append(center)
	var steps: int = max(2, int(sweep_deg / 6.0))
	for i in range(steps + 1):
		var a: float = deg_to_rad(start_deg + sweep_deg * (float(i) / float(steps)))
		points.append(center + Vector2(cos(a), sin(a)) * r)
	draw_colored_polygon(points, color)
	# Linha divisória entre segmentos
	draw_line(center, center + Vector2(cos(deg_to_rad(start_deg)), sin(deg_to_rad(start_deg))) * r,
		Color(0.1,0.1,0.1), 1.5)


## Retorna o índice do segmento que está sob o ponteiro (topo) na rotação atual.
## Útil para destacar visualmente qual Move foi sorteado após a animação parar.
func segment_at_pointer() -> int:
	if moves.is_empty(): return -1
	var total: int = 0
	for mv: Dictionary in moves:
		total += int(mv.get("percentage", 0))
	if total <= 0: return -1

	# Ângulo efetivo do ponteiro relativo ao início da roleta (topo = -90°)
	var effective: float = fmod(360.0 - fmod(current_rotation, 360.0), 360.0)
	var acc: float = 0.0
	for i in range(moves.size()):
		var weight: int = int(moves[i].get("percentage", 0))
		var sweep: float = (float(weight) / float(total)) * 360.0
		if effective >= acc and effective < acc + sweep:
			return i
		acc += sweep
	return moves.size() - 1
