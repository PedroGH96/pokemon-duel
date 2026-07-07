extends Control

## Tela 3 — Lobby Multiplayer
## Lista de salas públicas disponíveis (polling em /salas).
## Botões: Criar Sala Pública, Criar Sala Privada (com código), Entrar.
##
## NOTA: os endpoints /salas ainda não existem no backend (Fase 3 do roadmap).
## Esta tela já está pronta para consumi-los assim que forem implementados —
## por enquanto, exibe um aviso e permite voltar ao menu.

const POLL_INTERVAL := 3.0

@onready var room_list: VBoxContainer = %RoomList
@onready var status_label: Label = %StatusLabel
@onready var btn_back: Button = %BtnBack
@onready var btn_create_public: Button = %BtnCreatePublic
@onready var btn_create_private: Button = %BtnCreatePrivate
@onready var poll_timer: Timer = %PollTimer


func _ready() -> void:
	btn_back.pressed.connect(func(): get_tree().change_scene_to_file("res://scenes/menu/MainMenu.tscn"))
	btn_create_public.pressed.connect(func(): _create_room(false))
	btn_create_private.pressed.connect(func(): _create_room(true))

	poll_timer.wait_time = POLL_INTERVAL
	poll_timer.timeout.connect(_refresh_rooms)
	poll_timer.start()

	_refresh_rooms()


func _refresh_rooms() -> void:
	ApiClient.listar_salas(func(result):
		if result == null:
			status_label.text = "Endpoint /salas ainda não disponível no backend.\n(Aguardando implementação multiplayer — Fase 3 do roadmap)"
			room_list.get_children().map(func(c): c.queue_free())
			return

		_populate_rooms(result)
	)


func _populate_rooms(rooms: Array) -> void:
	for child in room_list.get_children():
		child.queue_free()

	if rooms.is_empty():
		status_label.text = "Nenhuma sala pública disponível. Crie uma!"
		return

	status_label.text = "%d sala(s) disponível(is):" % rooms.size()

	for room in rooms:
		var row := HBoxContainer.new()

		var name_label := Label.new()
		# O backend serializa o campo como "name" (Jackson padrão)
		var room_name: String = room.get("name", room.get("nome", "Sala sem nome"))
		var room_private: bool = room.get("privateRoom", false)
		name_label.text = "%s%s" % [room_name, " 🔒" if room_private else ""]
		name_label.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		row.add_child(name_label)

		var join_btn := Button.new()
		join_btn.text = "Entrar"
		join_btn.pressed.connect(func(): _join_room(room.get("id", "")))
		row.add_child(join_btn)

		room_list.add_child(row)


func _create_room(private: bool) -> void:
	var room_name: String = "Sala de %s" % (GameState.username if not GameState.username.is_empty() else "Treinador")
	# jogadorId é necessário — usa um ID temporário se não estiver registrado
	var pid: String = GameState.player_id if not GameState.player_id.is_empty() else "guest-" + str(randi())
	ApiClient.criar_sala_com_jogador(room_name, private, pid, func(result):
		if result == null:
			status_label.text = "Não foi possível criar a sala."
			return
		GameState.current_room_id = result.get("id", "")
		status_label.text = "Sala '%s' criada! Aguardando outro jogador..." % room_name
	)


func _join_room(room_id: String) -> void:
	var pid: String = GameState.player_id if not GameState.player_id.is_empty() else "guest-" + str(randi())
	ApiClient.entrar_sala_com_jogador(room_id, pid, func(result):
		if result == null:
			status_label.text = "Não foi possível entrar na sala."
			return
		GameState.current_room_id = room_id
		GameState.current_match_id = result.get("matchId", "")
		GameState.is_solo_match = false
		GameState.is_debug_match = false
		GameState.player_id = pid
		get_tree().change_scene_to_file("res://scenes/board/Board.tscn")
	)
