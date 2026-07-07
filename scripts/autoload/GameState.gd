extends Node

## GameState
## Singleton (autoload) que guarda dados de sessão compartilhados entre cenas:
##  - jogador atual (id, username, figuras desbloqueadas, deck)
##  - catálogo de Pokémon (cache local, carregado de /figuras na inicialização)
##  - sala/partida atual (multiplayer)

# ── Jogador ──────────────────────────────────────────────────────────────────
var player_id: String = ""
var username: String = ""
var unlocked_pokemon_ids: Array = []
var deck_ids: Array = []          # até 6 ids de Pokémon
var plate_ids: Array = []         # até 6 ids de placas de poder

# ── Catálogo (cache) ─────────────────────────────────────────────────────────
## Array de Dictionary, cada um representando um Pokémon (ver Pokemon.java)
var catalog: Array = []

## Mapa id (String) -> Dictionary, para lookup rápido
var catalog_by_id: Dictionary = {}

# ── Multiplayer ──────────────────────────────────────────────────────────────
var current_room_id: String = ""
var current_match_id: String = ""

# ── Modo Solo — contexto da partida atual (usado pelo Board para saber quando
#    disparar o turno do bot automaticamente e quando abrir a caixa de
#    recompensa ao vencer o chefe) ─────────────────────────────────────────────
var is_solo_match: bool = false
var is_debug_match: bool = false  # Área de Teste — reaproveita o Board.gd real
var solo_slot_atual: int = -1   # 0-4 = inimigo, 5 = chefe


func _ready() -> void:
	load_catalog()


## Carrega o catálogo completo a partir do backend (GET /figuras).
## Pode ser chamado novamente para recarregar.
func load_catalog(on_done: Callable = Callable()) -> void:
	ApiClient.get_figuras(func(result):
		if result == null:
			push_warning("GameState: falha ao carregar catálogo de /figuras")
			if on_done.is_valid():
				on_done.call(false)
			return

		catalog = result
		catalog_by_id.clear()
		for p in catalog:
			catalog_by_id[p["id"]] = p

		print("GameState: catálogo carregado (%d Pokémon)" % catalog.size())
		if on_done.is_valid():
			on_done.call(true)
	)


## Retorna o Dictionary do Pokémon com o id dado, ou null se não encontrado.
func get_pokemon(id: String) -> Variant:
	return catalog_by_id.get(id, null)


## Caminho do sprite de ÍCONE (res://) — usado no tabuleiro, banco e deck.
## Não existe variante shiny para ícones (só aparecem na batalha).
func sprite_path(pokemon: Dictionary) -> String:
	var file: String = pokemon.get("spriteFile", "%s.png" % pokemon.get("id", ""))
	return "res://assets/sprites/pokemon/icons/%s" % file


## Caminho do sprite de BATALHA (res://) — front (adversário) ou back (jogador),
## com variante shiny quando `shiny` é true.
## pokemon_or_figure: Dictionary com "id"/"pokemonId" (ou "spriteFile").
func battle_sprite_path(pokemon_or_figure: Dictionary, is_back: bool, shiny: bool = false) -> String:
	var file: String = str(pokemon_or_figure.get("spriteFile", ""))
	if file.is_empty():
		var pid: String = str(pokemon_or_figure.get("id", pokemon_or_figure.get("pokemonId", "")))
		file = "%s.png" % pid
	var side := "back" if is_back else "front"
	var shiny_dir := "shiny/" if shiny else ""
	return "res://assets/sprites/pokemon/battle/%s/%s%s" % [side, shiny_dir, file]


## Limpa o deck atual.
func clear_deck() -> void:
	deck_ids.clear()
	plate_ids.clear()


## Adiciona um Pokémon ao deck (máx. 6, deve estar desbloqueado).
func add_to_deck(pokemon_id: String) -> bool:
	if deck_ids.size() >= 6:
		return false
	if not unlocked_pokemon_ids.has(pokemon_id):
		return false
	if deck_ids.has(pokemon_id):
		return false
	deck_ids.append(pokemon_id)
	return true


func remove_from_deck(pokemon_id: String) -> void:
	deck_ids.erase(pokemon_id)


# ── Modo Solo ─────────────────────────────────────────────────────────────────
## Nível e slot escolhidos na seleção do modo solo
var solo_nivel_selecionado: int = 1

## Progresso: Dict { nivel: int → slots_desbloqueados: Array[int] }
## Slot 0 do nivel 1 sempre acessível; os demais desbloqueiam ao vencer.
var _solo_progress: Dictionary = {}


## Verifica se um nível está desbloqueado para seleção.
## Nível 1 sempre acessível. Niveis 2–6 desbloqueiam após concluir o chefe
## (slot 5) do nível anterior.
func is_solo_level_unlocked(nivel: int) -> bool:
	if nivel == 1: return true
	# Nível N desbloqueia quando slot 5 (chefe) do nível N-1 está concluído
	var slots_prev: Array = _solo_progress.get(nivel - 1, [])
	return slots_prev.has(5)


## Verifica se um slot (oponente) dentro de um nível está desbloqueado.
## Slot 0 de cada nível é sempre acessível (para níveis desbloqueados).
func is_solo_slot_unlocked(nivel: int, slot: int) -> bool:
	if not is_solo_level_unlocked(nivel): return false
	if slot == 0: return true
	var slots: Array = _solo_progress.get(nivel, [])
	return slots.has(slot - 1)  # slot N requer que slot N-1 tenha sido vencido


## Verifica se um slot já foi vencido antes (para mostrar "Revanche").
func is_solo_slot_beaten(nivel: int, slot: int) -> bool:
	var slots: Array = _solo_progress.get(nivel, [])
	return slots.has(slot)


## Registra vitória no slot e desbloqueia o próximo.
func unlock_solo_slot(nivel: int, slot_vencido: int) -> void:
	if not _solo_progress.has(nivel):
		_solo_progress[nivel] = []
	var slots: Array = _solo_progress[nivel]
	if not slots.has(slot_vencido):
		slots.append(slot_vencido)
	# Se concluiu o chefe (slot 5), desbloqueia o próximo nível
	if slot_vencido == 5 and nivel < 6:
		if not _solo_progress.has(nivel + 1):
			_solo_progress[nivel + 1] = []
