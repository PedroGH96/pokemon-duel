extends Node

## ApiClient
## Singleton (autoload) responsável por toda comunicação HTTP/JSON com o
## backend Spring Boot (pokemon-duel-backend).
##
## Uso típico:
##   ApiClient.get_figuras(func(figuras): minha_lista = figuras)
##   ApiClient.criar_sala(nome, func(sala): print(sala))
##
## Todas as funções recebem um Callable (callback) chamado com o resultado
## já decodificado (Array/Dictionary) ou `null` em caso de erro.

## Endereço base da API.
## - Desenvolvimento local: http://127.0.0.1:8080
## - Emulador Android (apontando pro seu PC): http://10.0.2.2:8080
## - Produção (Render, etc.): a URL pública do backend hospedado
##
## Pra alternar, mude só a linha USE_PRODUCTION abaixo — não precisa mexer
## em mais nada nem comentar/descomentar URL.
const USE_PRODUCTION := true

const URL_LOCAL      := "http://127.0.0.1:8080"
const URL_PRODUCTION := "https://pokemon-duel.onrender.com"

const BASE_URL := URL_PRODUCTION if USE_PRODUCTION else URL_LOCAL

const HEADERS := ["Content-Type: application/json"]


# ─────────────────────────────────────────────────────────────────────────
# Helpers genéricos
# ─────────────────────────────────────────────────────────────────────────

## Faz uma requisição HTTP genérica.
## method: HTTPClient.Method (ex: HTTPClient.METHOD_GET)
## path: caminho relativo (ex: "/figuras")
## body: Dictionary a ser enviado como JSON (ou {} se não houver corpo)
## callback: Callable(result_or_null) — chamado com o JSON decodificado em sucesso, ou null em erro
## on_error: Callable(String) opcional — chamado com a mensagem de erro real do
##           backend (texto puro do ResponseEntity.badRequest().body(...)),
##           além do callback(null) normal. Use para mostrar a causa real ao usuário.
func _request(method: int, path: String, body: Dictionary, callback: Callable, on_error: Callable = Callable()) -> void:
	var http := HTTPRequest.new()
	add_child(http)

	var error := http.request_completed.connect(
		func(_result: int, response_code: int, _headers: PackedStringArray, body_bytes: PackedByteArray):
			http.queue_free()
			var text: String = body_bytes.get_string_from_utf8()

			if response_code < 200 or response_code >= 300:
				push_warning("ApiClient: %s %s -> HTTP %d" % [
					_method_name(method), path, response_code])
				if on_error.is_valid():
					# O corpo do erro pode ser texto puro ou JSON; tenta extrair
					# a mensagem mais útil possível para mostrar ao usuário.
					var msg: String = text if not text.is_empty() else "HTTP %d" % response_code
					var json_err := JSON.new()
					if json_err.parse(text) == OK and json_err.data is Dictionary:
						var err_dict: Dictionary = json_err.data
						msg = str(err_dict.get("message", err_dict.get("error", text)))
					on_error.call(msg)
				if callback.is_valid(): callback.call(null)
				return

			if text.is_empty():
				if callback.is_valid(): callback.call({})
				return

			var json := JSON.new()
			if json.parse(text) != OK:
				push_warning("ApiClient: resposta JSON inválida em %s" % path)
				if on_error.is_valid():
					on_error.call("Resposta JSON inválida do servidor")
				if callback.is_valid(): callback.call(null)
				return

			if callback.is_valid(): callback.call(json.data)
	)

	if error != OK:
		push_warning("ApiClient: falha ao conectar signal")
		if on_error.is_valid():
			on_error.call("Falha interna ao conectar (signal)")
		if callback.is_valid(): callback.call(null)
		return

	var url := BASE_URL + path
	var json_body: String = "" if body.is_empty() else JSON.stringify(body)

	var err := http.request(url, HEADERS, method, json_body)
	if err != OK:
		push_warning("ApiClient: falha ao iniciar requisição para %s (err=%d)" % [url, err])
		http.queue_free()
		if callback.is_valid(): callback.call(null)


func _method_name(method: int) -> String:
	match method:
		HTTPClient.METHOD_GET: return "GET"
		HTTPClient.METHOD_POST: return "POST"
		HTTPClient.METHOD_PUT: return "PUT"
		HTTPClient.METHOD_DELETE: return "DELETE"
		_: return "?"


# ─────────────────────────────────────────────────────────────────────────
# /figuras — catálogo de Pokémon
# ─────────────────────────────────────────────────────────────────────────

## GET /figuras -> Array de Pokémon (Dictionary)
func get_figuras(callback: Callable) -> void:
	_request(HTTPClient.METHOD_GET, "/figuras", {}, callback)


## GET /figuras/{id} -> Pokémon (Dictionary) ou null
func get_figura(id: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_GET, "/figuras/%s" % id, {}, callback)


# ─────────────────────────────────────────────────────────────────────────
# /jogadores — perfis de jogador
# ─────────────────────────────────────────────────────────────────────────

## POST /jogadores/registrar { "username": "..." } -> Player (Dictionary)
func registrar_jogador(username: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_POST, "/jogadores/registrar",
		{"username": username}, callback)


## GET /jogadores/{id}/figuras -> Array de ids de figuras desbloqueadas
func get_figuras_desbloqueadas(player_id: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_GET, "/jogadores/%s/figuras" % player_id, {}, callback)


# ─────────────────────────────────────────────────────────────────────────
# /salas — multiplayer (a implementar no backend)
# ─────────────────────────────────────────────────────────────────────────

## GET /salas -> Array de salas disponíveis
func listar_salas(callback: Callable) -> void:
	_request(HTTPClient.METHOD_GET, "/salas", {}, callback)


## POST /salas { "nome": "...", "privada": bool, "jogadorId": "..." } -> Sala criada
func criar_sala_com_jogador(nome: String, privada: bool, jogador_id: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_POST, "/salas",
		{"nome": nome, "privada": privada, "jogadorId": jogador_id}, callback)


## POST /salas/{id}/entrar { "jogadorId": "..." } -> Sala atualizada (com matchId)
func entrar_sala_com_jogador(sala_id: String, jogador_id: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_POST, "/salas/%s/entrar" % sala_id,
		{"jogadorId": jogador_id}, callback)


## POST /partidas/{id}/entrar { "figuraId": "...", "jogadorId": "..." }
func entrar_tabuleiro(partida_id: String, figura_id: String, jogador_id: String, node_id: int, callback: Callable, on_error: Callable = Callable()) -> void:
	_request(HTTPClient.METHOD_POST, "/partidas/%s/entrar" % partida_id,
		{"figuraId": figura_id, "jogadorId": jogador_id, "nodeId": node_id}, callback, on_error)


## GET /partidas/{id}/movimentos?figuraId=X&jogadorId=Y
func get_movimentos(partida_id: String, figura_id: String, jogador_id: String, callback: Callable, on_error: Callable = Callable()) -> void:
	_request(HTTPClient.METHOD_GET,
		"/partidas/%s/movimentos?figuraId=%s&jogadorId=%s" % [partida_id, figura_id, jogador_id],
		{}, callback, on_error)


## POST /partidas/{id}/passar { "jogadorId": "..." }
## Encerra o turno sem usar o PM restante de uma ação pendente (figura que
## acabou de entrar ou se mover e ainda tinha PM sobrando).
func passar_vez(partida_id: String, jogador_id: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_POST, "/partidas/%s/passar" % partida_id,
		{"jogadorId": jogador_id}, callback)


# ─────────────────────────────────────────────────────────────────────────
# /partidas — estado e ações de jogo (a implementar no backend)
# ─────────────────────────────────────────────────────────────────────────

## GET /partidas/{id}/estado -> estado atual da partida (para polling)
func get_estado_partida(partida_id: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_GET, "/partidas/%s/estado" % partida_id, {}, callback)


## POST /partidas/{id}/mover { "figuraId": "...", "destino": {"nodeId": N}, "jogadorId": "..." }
func mover_figura(partida_id: String, figura_id: String, node_id: int, jogador_id: String, callback: Callable, on_error: Callable = Callable()) -> void:
	_request(HTTPClient.METHOD_POST, "/partidas/%s/mover" % partida_id,
		{"figuraId": figura_id, "destino": {"nodeId": node_id}, "jogadorId": jogador_id}, callback, on_error)


## POST /partidas/{id}/batalhar { "atacanteId": "...", "defensorId": "..." }
func batalhar(partida_id: String, atacante_id: String, defensor_id: String, callback: Callable) -> void:
	_request(HTTPClient.METHOD_POST, "/partidas/%s/batalhar" % partida_id,
		{"atacanteId": atacante_id, "defensorId": defensor_id}, callback)


# ─────────────────────────────────────────────────────────────────────────
# /debug — endpoints de teste (criar partida vs bot, testar roleta/batalha)
# ─────────────────────────────────────────────────────────────────────────

## POST /debug/partida { "deckP1": [...], "deckP2": [...] } -> partida criada
func debug_criar_partida(callback: Callable, deck_p1: Array = [], deck_p2: Array = []) -> void:
	var body: Dictionary = {}
	if not deck_p1.is_empty(): body["deckP1"] = deck_p1
	if not deck_p2.is_empty(): body["deckP2"] = deck_p2
	_request(HTTPClient.METHOD_POST, "/debug/partida", body, callback)


## POST /debug/partida/{id}/bot -> executa um turno do bot
func debug_turno_bot(match_id: String, callback: Callable, on_error: Callable = Callable()) -> void:
	_request(HTTPClient.METHOD_POST, "/debug/partida/%s/bot" % match_id, {}, callback, on_error)


## POST /debug/batalha { "pokemon1": "...", "pokemon2": "...", "vezes": N }
func debug_testar_batalha(pokemon1: String, pokemon2: String, vezes: int, callback: Callable, on_error: Callable = Callable()) -> void:
	_request(HTTPClient.METHOD_POST, "/debug/batalha",
		{"pokemon1": pokemon1, "pokemon2": pokemon2, "vezes": vezes}, callback, on_error)


## GET /debug/roleta/{pokemonId}?vezes=N
func debug_testar_roleta(pokemon_id: String, vezes: int, callback: Callable, on_error: Callable = Callable()) -> void:
	_request(HTTPClient.METHOD_GET, "/debug/roleta/%s?vezes=%d" % [pokemon_id, vezes], {}, callback, on_error)


# ── Solo ─────────────────────────────────────────────────────────────────────

## GET /solo/niveis -> Array de níveis com oponentes
func get_solo_niveis(callback: Callable) -> void:
	_request(HTTPClient.METHOD_GET, "/solo/niveis", {}, callback)


## POST /solo/partida { nivel, slot, jogadorId, deckJogador } -> match criado
func post_solo_partida(body: Dictionary, callback: Callable, on_error: Callable = Callable()) -> void:
	_request(HTTPClient.METHOD_POST, "/solo/partida", body, callback, on_error)


## POST /solo/recompensa { jogadorId, nivel } -> { pokemonId, pokemonName, shiny, novo }
func post_solo_recompensa(jogador_id: String, nivel: int, callback: Callable) -> void:
	_request(HTTPClient.METHOD_POST, "/solo/recompensa",
		{"jogadorId": jogador_id, "nivel": nivel}, callback)


## Wrapper público genérico para chamadas que usam método como String
## ("GET" ou "POST"). Converte para HTTPClient.Method automaticamente.
func request(method_str: String, path: String, body: Dictionary, callback: Callable) -> void:
	var method: int = HTTPClient.METHOD_GET if method_str == "GET" else HTTPClient.METHOD_POST
	_request(method, path, body, callback)
