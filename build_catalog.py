# -*- coding: utf-8 -*-
"""
Gera pokemon-catalog.json a partir dos dados da Lista_.pdf
(ID-2 Bulbasaur até ID-162 Genesect).

Cada Pokémon:
  dexId   - número de exibição (ID-N do documento)
  id      - slug minúsculo (usado como chave, nome do sprite)
  name    - nome de exibição
  type / type2 - tipo(s) elemental(is) (type2 = None se monotipo)
  rarity  - C, UC, R, EX, UX
  pm      - Movement (Pontos de Movimento)
  specialAbility - texto da habilidade (None se não houver)
  moves   - lista de segmentos da roleta (soma de "size" = 96)

Move:
  name, color (RED/WHITE/PURPLE/GOLD/BLUE), size (peso na roleta, soma=96),
  damage (dano numérico, 0 se não houver), effect (StatusEffect ou NONE),
  turns (duração do efeito), description (texto da nota adicional)
"""

POKEMON = []


def add(dexId, id_, name, type1, type2, rarity, pm, ability, moves):
    total = sum(mv["size"] for mv in moves)
    if total <= 0:
        raise ValueError(f"{name} (ID-{dexId}): roleta vazia")
    POKEMON.append({
        "dexId": dexId,
        "id": id_,
        "name": name,
        "type": type1,
        "type2": type2,
        "rarity": rarity,
        "pm": pm,
        "specialAbility": ability,
        "moves": moves,
        "wheelTotal": total,
    })


def m(name, color, size, damage=0, effect="NONE", turns=0, desc=""):
    return {
        "name": name,
        "color": color,
        "size": size,
        "damage": damage,
        "effect": effect,
        "turns": turns,
        "description": desc,
    }


# ═══════════════════════════════════════════════════════════════════════════
# ENTRADAS — preenchidas em lotes abaixo
# ═══════════════════════════════════════════════════════════════════════════

# ── Geração 1: Bulbasaur a Scyther (ID-2 a ID-15) ────────────────────────────

add(2, "bulbasaur", "Bulbasaur", "GRAMA", "VENENO", "UC", 3,
    "Semente Curativa - Enquanto este Pokémon estiver em campo, seus Pokémon do tipo Grama e Veneno não podem ficar envenenados ou tóxicos.",
    [
        m("Poison Powder", "PURPLE", 24, effect="POISON", desc="O oponente da batalha fica envenenado."),
        m("Seed Bomb", "WHITE", 20, damage=50),
        m("Sleep Powder", "PURPLE", 24, effect="SLEEP", desc="O oponente da batalha adormece."),
        m("Miss", "RED", 28),
    ])

add(3, "ivysaur", "Ivysaur", "GRAMA", "VENENO", "C", 2,
    "Semente Curativa - Enquanto este Pokémon estiver em campo, seus Pokémon do tipo Grama e Veneno não podem ficar envenenados ou tóxicos.",
    [
        m("Vine Whip", "WHITE", 28, damage=40),
        m("Razor Leaf", "WHITE", 24, damage=60),
        m("Sleep Powder", "PURPLE", 28, effect="SLEEP", desc="O oponente da batalha adormece."),
        m("Miss", "RED", 16),
    ])

add(4, "venusaur", "Venusaur", "GRAMA", "VENENO", "EX", 1,
    "Pólen Químico - Enquanto este Pokémon estiver em campo, todos os Pokémon envenenados ou tóxicos têm PM-1. Este efeito não acumula.",
    [
        m("Protect", "BLUE", 20, desc="Este Pokémon ganha Wait."),
        m("Solar Beam", "WHITE", 36, damage=150),
        m("Sleep Powder", "PURPLE", 32, effect="SLEEP", desc="O oponente da batalha adormece."),
        m("Miss", "RED", 8),
    ])

add(5, "charmander", "Charmander", "FOGO", None, "UC", 3, None,
    [
        m("Flame Tail", "WHITE", 24, damage=40),
        m("Smokescreen", "PURPLE", 28, effect="IMMOBILIZED", turns=1,
          desc="O oponente não pode se mover no próximo turno."),
        m("Scratch", "WHITE", 32, damage=10),
        m("Miss", "RED", 12),
    ])

add(6, "charmeleon", "Charmeleon", "FOGO", None, "C", 2, None,
    [
        m("Miss", "RED", 8),
        m("Flame", "PURPLE", 24, effect="BURN", desc="O oponente da batalha fica queimado."),
        m("Miss", "RED", 8),
        m("Iron Tail", "WHITE", 56, damage=50),
    ])

add(7, "charizard", "Charizard", "FOGO", "VOADOR", "EX", 2, None,
    [
        m("Miss", "RED", 8),
        m("Fire Spin", "WHITE", 56, damage=60,
          desc="Gira de novo enquanto Fire Spin sair - o dano é multiplicado pelo número de giros."),
        m("Miss", "RED", 4),
        m("Dragon Tail", "WHITE", 28, damage=100,
          desc="Coloca o oponente da batalha no banco (exceto se já tiver desmaiado). O oponente ganha Wait."),
    ])

add(8, "squirtle", "Squirtle", "AGUA", None, "UC", 3, None,
    [
        m("Miss", "RED", 12),
        m("Bubble", "WHITE", 44, damage=30),
        m("Withdraw", "BLUE", 40, desc="Este Pokémon ganha Wait."),
    ])

add(9, "wartortle", "Wartortle", "AGUA", None, "C", 2, None,
    [
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 8),
        m("Water Gun", "WHITE", 40, damage=40),
        m("Dodge", "BLUE", 8),
        m("Rapid Spin", "WHITE", 36, damage=20,
          desc="Gira de novo enquanto Rapid Spin sair - o dano é multiplicado pelo número de giros."),
    ])

add(10, "blastoise", "Blastoise", "AGUA", None, "EX", 2, None,
    [
        m("Miss", "RED", 8),
        m("Hydro Pump", "WHITE", 40, damage=140),
        m("Miss", "RED", 8),
        m("Mirror Coat", "PURPLE", 40,
          desc="O oponente da batalha é derrotado se ele girar roxo."),
    ])

add(11, "pikachu", "Pikachu", "ELETRICO", None, "UC", 2, None,
    [
        m("Thunder Shock", "WHITE", 24, damage=40),
        m("Miss", "RED", 12),
        m("Thunderbolt", "WHITE", 24, damage=100),
        m("Miss", "RED", 8),
        m("Thunder Shock", "WHITE", 24, damage=40),
        m("Dodge", "BLUE", 4),
    ])

add(12, "raichu", "Raichu", "ELETRICO", None, "UC", 2,
    "Aterramento - Este Pokémon não pode ser Paralisado.",
    [
        m("Miss", "RED", 8),
        m("Thunder", "WHITE", 28, damage=100),
        m("Miss", "RED", 8),
        m("Thunder Jolt", "WHITE", 52, damage=40,
          desc="Se este Pokémon for derrotado usando este ataque, o oponente da batalha fica paralisado."),
    ])

add(13, "machop", "Machop", "LUTADOR", None, "C", 2, None,
    [
        m("Miss", "RED", 8),
        m("Focus Punch", "WHITE", 56, damage=40),
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 28),
    ])

add(14, "machamp", "Machamp", "LUTADOR", None, "R", 2,
    "Determinação - Se este Pokémon tiver uma condição especial, causa +50 de dano.",
    [
        m("Miss", "RED", 4),
        m("Karate Chop", "WHITE", 24, damage=60),
        m("Submission", "WHITE", 28, damage=80),
        m("Karate Chop", "WHITE", 24, damage=60),
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 12),
    ])

add(15, "scyther", "Scyther", "INSETO", "VOADOR", "UC", 2, None,
    [
        m("False Swipe", "WHITE", 24, damage=20),
        m("Slash", "WHITE", 32, damage=50),
        m("Swords Dance", "WHITE", 36, damage=0,
          desc="Gira de novo até sair um ataque diferente de Swords Dance. Ataques de dano causam o dobro."),
        m("Miss", "RED", 4),
    ])

# ── Geração 1 (cont.): Magikarp a Mew (ID-16 a ID-31) ────────────────────────

add(16, "magikarp", "Magikarp", "AGUA", None, "C", 2,
    "Evolução Rápida - Quando este Pokémon desmaia em batalha, você pode evoluí-lo sem enviá-lo ao P.C.",
    [
        m("Splash", "WHITE", 48, damage=0),
        m("Flail", "WHITE", 44, damage=10),
        m("Miss", "RED", 4),
    ])

add(17, "gyarados", "Gyarados", "AGUA", "VOADOR", "EX", 2,
    "Devastador - Este Pokémon pode fazer MP Move através de outros Pokémon no campo. Pokémon derrotados pelos ataques deste Pokémon são temporariamente excluídos do duelo e podem ir ao P.C. após 5 turnos.",
    [
        m("Miss", "RED", 8),
        m("Waterfall", "WHITE", 36, damage=130,
          desc="Se o oponente da batalha for do tipo Água, este Pokémon não recebe dano e avança para um ponto além do oponente."),
        m("Miss", "RED", 4),
        m("Hurricane", "PURPLE", 28,
          desc="Move o oponente da batalha para o banco. O oponente e todos os Pokémon Voadores adversários no campo ganham Wait 3."),
    ])

add(18, "lapras", "Lapras", "AGUA", "GELO", "R", 2,
    "Muralha de Gelo - Este Pokémon não pode ficar queimado ou congelado.",
    [
        m("Ice Beam", "WHITE", 12, damage=50,
          desc="O oponente da batalha fica congelado."),
        m("Surf", "WHITE", 36, damage=100),
        m("Sing", "PURPLE", 32, effect="SLEEP", desc="O oponente da batalha adormece."),
        m("Dodge", "BLUE", 12),
        m("Miss", "RED", 4),
    ])

add(19, "eevee", "Eevee", "NORMAL", None, "R", 3,
    "Evolução Espontânea - Sempre que este Pokémon for do P.C. para o banco, ele pode evoluir.",
    [
        m("Dodge", "BLUE", 8),
        m("Tackle", "WHITE", 24, damage=30),
        m("Focus Energy", "WHITE", 28, damage=0),
        m("Quick Attack", "GOLD", 16, damage=20),
        m("Dodge", "BLUE", 12),
        m("Miss", "RED", 8),
    ])

add(20, "vaporeon", "Vaporeon", "AGUA", None, "R", 3, None,
    [
        m("Miss", "RED", 8),
        m("Dodge", "BLUE", 20),
        m("Water Slide", "PURPLE", 28,
          desc="O oponente da batalha é empurrado 1 passo para trás e ganha Wait."),
        m("Surf", "WHITE", 40, damage=40),
    ])

add(21, "jolteon", "Jolteon", "ELETRICO", None, "R", 3,
    "Raio Veloz - Este Pokémon pode fazer MP Move passando por Pokémon Paralisados.",
    [
        m("Dodge", "BLUE", 20),
        m("Thunder Jolt", "WHITE", 40, damage=40,
          desc="Se este Pokémon for derrotado usando este ataque, o oponente da batalha fica paralisado."),
        m("Quick Attack", "GOLD", 28, damage=30),
        m("Miss", "RED", 8),
    ])

add(22, "flareon", "Flareon", "FOGO", None, "R", 3, None,
    [
        m("Miss", "RED", 12),
        m("Flamethrower", "WHITE", 44, damage=40,
          desc="Se este Pokémon for derrotado usando este ataque, o oponente da batalha fica queimado."),
        m("Focus Energy", "WHITE", 20, damage=0,
          desc="Gira de novo até sair um ataque diferente de Focus Energy. Ataques de dano causam +20."),
        m("Dodge", "BLUE", 20),
    ])

add(23, "snorlax", "Snorlax", "NORMAL", None, "EX", 1,
    "Quietude - Este Pokémon não pode ser movido pelos ataques de outros Pokémon (exceto Sweet Scent). Só pode ser afetado pela condição Sono.",
    [
        m("Miss", "RED", 4),
        m("Big Yawn", "PURPLE", 36, effect="SLEEP",
          desc="Este Pokémon e o oponente da batalha adormecem."),
        m("Body Slam", "WHITE", 32, damage=150,
          desc="O oponente da batalha fica paralisado."),
        m("Giga Impact", "WHITE", 24, damage=180),
    ])

add(27, "dratini", "Dratini", "DRAGAO", None, "R", 2, None,
    [
        m("Dragon Rage", "WHITE", 20, damage=80),
        m("Push Aside", "PURPLE", 24,
          desc="O oponente da batalha é movido para o banco e ganha Wait."),
        m("Miss", "RED", 8),
        m("Freeze Tail", "WHITE", 44, damage=30,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica congelado."),
    ])

add(28, "dragonair", "Dragonair", "DRAGAO", None, "R", 2,
    "Troca de Pele - Este Pokémon se recupera de qualquer efeito de status no início do seu turno, e o turno termina. Ao fazer isso, você pode evoluí-lo, se possível.",
    [
        m("Miss", "RED", 4),
        m("Magnetic Storm", "PURPLE", 32,
          desc="Este Pokémon e todos os vizinhos ficam Paralisados."),
        m("Miss", "RED", 12),
        m("Dragon Tail", "WHITE", 48, damage=80,
          desc="Coloca o oponente da batalha no banco (exceto se já desmaiou). O oponente ganha Wait."),
    ])

add(29, "dragonite", "Dragonite", "DRAGAO", "VOADOR", "EX", 1,
    "Impulso de Velocidade - Quando este Pokémon evolui, ganha +1 PM.",
    [
        m("Extreme Speed", "GOLD", 28, damage=100),
        m("Miss", "RED", 12),
        m("Sightseeing", "PURPLE", 28,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além, ganhando Wait."),
        m("Dragon Tail", "WHITE", 28, damage=140,
          desc="Coloca o oponente da batalha no banco (exceto se já desmaiou). O oponente ganha Wait."),
    ])

add(24, "articuno", "Articuno", "GELO", "VOADOR", "EX", 2,
    "Voo Alto - Este Pokémon pode voar sobre Pokémon que não tenham Voo Alto usando um MP Move.",
    [
        m("Steel Wing", "WHITE", 16, damage=130),
        m("Cold Crush", "WHITE", 32, damage=90),
        m("Miss", "RED", 12),
        m("Ice Charge", "PURPLE", 20,
          desc="Anexa um Marcador de Carga a este Pokémon. Ao remover o marcador (no início de uma batalha em seu turno), o oponente e os Pokémon na mesma fileira ficam Congelados, e o turno termina."),
    ])

add(25, "zapdos", "Zapdos", "ELETRICO", "VOADOR", "EX", 2,
    "Voo Alto - Este Pokémon pode voar sobre Pokémon que não tenham Voo Alto usando um MP Move.",
    [
        m("Steel Wing", "WHITE", 16, damage=50),
        m("Thunder Crash", "GOLD", 32, damage=100),
        m("Roost", "BLUE", 16,
          desc="Este Pokémon se recupera de todos os efeitos de status. Em troca, ganha Wait 3."),
        m("Thunder Charge", "PURPLE", 20,
          desc="Anexa um Marcador de Carga a este Pokémon. Ao remover o marcador, o oponente e os Pokémon na mesma fileira ficam Paralisados, e o turno termina."),
    ])

add(26, "moltres", "Moltres", "FOGO", "VOADOR", "EX", 2,
    "Voo Alto - Este Pokémon pode voar sobre Pokémon que não tenham Voo Alto usando um MP Move.",
    [
        m("Steel Wing", "WHITE", 16, damage=60),
        m("Crushing Flames", "WHITE", 32, damage=110),
        m("Miss", "RED", 12),
        m("Roost", "BLUE", 16,
          desc="Este Pokémon se recupera de todos os efeitos de status. Em troca, ganha Wait 3."),
        m("Flame Charge", "PURPLE", 20,
          desc="Anexa um Marcador de Carga a este Pokémon. Ao remover o marcador, o oponente e os Pokémon na mesma fileira ficam Queimados, e o turno termina."),
    ])

add(30, "mewtwo", "Mewtwo", "PSIQUICO", None, "EX", 2,
    "Avanço Veloz - Todos os ataques Azuis dos oponentes de batalha deste Pokémon se tornam Miss.",
    [
        m("Psychic Shove", "PURPLE", 32,
          desc="O oponente da batalha é empurrado o mais longe possível em linha reta. Pokémon atingidos também são empurrados e ganham Wait."),
        m("Annihilate", "BLUE", 16, desc="Este Pokémon se move 2 passos."),
        m("Psycho Cut", "WHITE", 40, damage=70,
          desc="Gira de novo - se sair Psycho Cut novamente, causa +50 de dano."),
        m("Miss", "RED", 8),
    ])

add(31, "mew", "Mew", "PSIQUICO", None, "EX", 3, None,
    [
        m("Miss", "RED", 4),
        m("Psychic", "WHITE", 24, damage=100),
        m("Hyper Sonic", "GOLD", 48, damage=30,
          desc="Se o ataque do oponente for 100 de dano ou mais, este Pokémon não pode ser derrotado."),
        m("Genesis Supernova", "WHITE", 20, damage=200,
          desc="Todos os Pokémon adversários em campo giram. Os que sortearem um ataque Roxo são derrotados. Este Pokémon é excluído do duelo."),
    ])

# ── Geração 2: Chikorita a Murkrow (ID-32 a ID-48) ───────────────────────────

add(32, "chikorita", "Chikorita", "GRAMA", None, "UC", 3,
    "Aroma Refrescante - Quando este Pokémon vai do banco para o campo, remove condições especiais de todos os seus Pokémon em campo.",
    [
        m("Sweet Scent", "PURPLE", 24,
          desc="Um Pokémon a até 3 passos é atraído 1-2 passos para mais perto deste Pokémon e ganha Wait."),
        m("Miss", "RED", 16),
        m("Grass Knot", "WHITE", 56, damage=40,
          desc="Se o ataque do oponente for 120 de dano ou mais, o oponente é derrotado em vez deste Pokémon."),
    ])

add(33, "bayleef", "Bayleef", "GRAMA", None, "C", 2,
    "Aroma Picante - Este Pokémon causa +20 de dano em batalhas no seu turno.",
    [
        m("Miss", "RED", 12),
        m("Poison Powder", "PURPLE", 28, effect="POISON", desc="O oponente da batalha fica envenenado."),
        m("Body Slam", "WHITE", 56, damage=80),
    ])

add(34, "meganium", "Meganium", "GRAMA", None, "EX", 2,
    "Guarda Foliar - Este Pokémon só pode ser afetado pela condição Sono. Não recebe marcadores dos ataques do oponente.",
    [
        m("Body Slam", "WHITE", 20, damage=100, desc="O oponente da batalha fica paralisado."),
        m("Petal Blizzard", "WHITE", 36, damage=160,
          desc="Todos os Pokémon adversários em campo giram. Os que sortearem Miss são derrotados."),
        m("Synthesis", "PURPLE", 32,
          desc="Enquanto estiverem em campo, seus Pokémon do tipo Grama causam +20 de dano ao oponente."),
        m("Miss", "RED", 8),
    ])

add(35, "cyndaquil", "Cyndaquil", "FOGO", None, "UC", 3,
    "Determinação Crescente - O Ember deste Pokémon ganha +20 de dano para cada Pokémon seu no P.C.",
    [
        m("Ember", "WHITE", 20, damage=20),
        m("Tackle", "WHITE", 32, damage=30),
        m("Ember", "WHITE", 20, damage=20),
        m("Miss", "RED", 24),
    ])

add(36, "quilava", "Quilava", "FOGO", None, "C", 2,
    "Determinação Crescente - O Ember deste Pokémon ganha +20 de dano para cada Pokémon seu no P.C.",
    [
        m("Flamethrower", "WHITE", 52, damage=40,
          desc="Se este Pokémon for derrotado usando este ataque, o oponente da batalha fica queimado."),
        m("Ember", "WHITE", 32, damage=30),
        m("Miss", "RED", 12),
    ])

add(37, "typhlosion", "Typhlosion", "FOGO", None, "EX", 1,
    "Aceleração Flamejante - PM+1 para este Pokémon para cada Pokémon que você tem no P.C.",
    [
        m("Miss", "RED", 8),
        m("Ember", "WHITE", 12, damage=50),
        m("Hot Air", "PURPLE", 28, effect="BURN", desc="Todos os Pokémon vizinhos ficam queimados."),
        m("Ember", "WHITE", 12, damage=50),
        m("Miss", "RED", 8),
        m("Fire Blast", "WHITE", 28, damage=90),
    ])

add(38, "totodile", "Totodile", "AGUA", None, "UC", 3,
    "Instinto de Mordida - Este Pokémon deve batalhar se possível após fazer um MP move.",
    [
        m("Bite", "WHITE", 28, damage=90),
        m("Miss", "RED", 12),
        m("Water Gun", "WHITE", 48, damage=60),
    ])

add(39, "croconaw", "Croconaw", "AGUA", None, "C", 2,
    "Movimento Mordaz - Este Pokémon pode sempre atacar após se mover.",
    [
        m("Scary Face", "PURPLE", 28,
          desc="O oponente é movido 1 passo para trás deste Pokémon (o oponente escolhe o ponto) e ganha Wait."),
        m("Miss", "RED", 20),
        m("Bite", "WHITE", 48, damage=80),
    ])

add(40, "feraligatr", "Feraligatr", "AGUA", None, "EX", 1,
    "Impulso de Velocidade - Quando este Pokémon evolui, ganha +1 PM.",
    [
        m("Miss", "RED", 8),
        m("Hydro Pump", "WHITE", 24, damage=160),
        m("Bite", "WHITE", 36, damage=100),
        m("Miss", "RED", 4),
        m("Wash Away", "PURPLE", 28,
          desc="O oponente da batalha é empurrado 2 passos para trás e ganha Wait."),
    ])

add(41, "mareep", "Mareep", "ELETRICO", None, "C", 2, None,
    [
        m("Thunder Wave", "PURPLE", 32, effect="PARALYSIS", desc="O oponente da batalha fica paralisado."),
        m("Thunder Shock", "WHITE", 48, damage=40),
        m("Miss", "RED", 16),
    ])

add(42, "flaaffy", "Flaaffy", "ELETRICO", None, "UC", 2,
    "Pelagem Elétrica - Se este Pokémon não estiver afetado por condição especial, todos os ataques Azuis dos oponentes não-Terrestres se tornam Miss. Este Pokémon não pode ser Paralisado.",
    [
        m("Thunder Wave", "PURPLE", 36, effect="PARALYSIS", desc="O oponente da batalha fica paralisado."),
        m("Thunder Punch", "WHITE", 48, damage=40,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica paralisado."),
        m("Miss", "RED", 12),
    ])

add(43, "ampharos", "Ampharos", "ELETRICO", None, "EX", 2,
    "Sobrecarga Elétrica - Enquanto este Pokémon estiver em campo, seus Pokémon do tipo Elétrico causam +10 de dano para cada Pokémon paralisado em campo. Não acumula.",
    [
        m("Miss", "RED", 4),
        m("Dragon Pulse", "WHITE", 32, damage=90,
          desc="Causa +50 de dano se o oponente da batalha for do tipo Dragão."),
        m("Miss", "RED", 4),
        m("Override", "WHITE", 32, damage=100, desc="Todos os Pokémon adjacentes ficam paralisados."),
        m("Beacon Light", "BLUE", 20, desc="Remove Wait de todos os seus Pokémon em campo."),
    ])

add(44, "marill", "Marill", "AGUA", "FADA", "C", 2,
    "Força Bruta - Se este Pokémon não estiver afetado por condição especial, causa +50 de dano.",
    [
        m("Tackle", "WHITE", 20, damage=30),
        m("Water Gun", "WHITE", 28, damage=40),
        m("Rollout", "WHITE", 28, damage=30,
          desc="Gira de novo enquanto Rollout sair - o dano é multiplicado pelo número de giros."),
        m("Miss", "RED", 20),
    ])

add(45, "azumarill", "Azumarill", "AGUA", "FADA", "R", 2, None,
    [
        m("Aqua Jet", "GOLD", 20, damage=30),
        m("Play Rough", "WHITE", 32, damage=80,
          desc="No próximo turno, o oponente não pode usar placas."),
        m("Belly Drum", "PURPLE", 28,
          desc="Anexa um marcador Final Song a todos os Pokémon a até 2 passos. Pokémon com esse marcador são derrotados 5 turnos depois. Seus Pokémon com Belly Drum causam +50 de dano enquanto esse Pokémon estiver em campo."),
        m("Miss", "RED", 16),
    ])

add(46, "espeon", "Espeon", "PSIQUICO", None, "R", 3,
    "Previsão Comportamental - Este Pokémon ganha +1 estrela.",
    [
        m("Miss", "RED", 8),
        m("Dodge", "BLUE", 20),
        m("Magic Coat", "PURPLE", 32,
          desc="Se o oponente usar um ataque que anexaria marcador ou causaria condição especial neste Pokémon, esse efeito é redirecionado ao oponente."),
        m("Psybeam", "WHITE", 36, damage=40,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica confuso."),
    ])

add(47, "umbreon", "Umbreon", "SOMBRIO", None, "R", 3,
    "Sincronia - Quando este Pokémon fica envenenado, tóxico, paralisado, queimado etc. por um ataque, o Pokémon que causou a condição também fica com ela.",
    [
        m("Miss", "RED", 8),
        m("Dodge", "BLUE", 20),
        m("Toxic", "PURPLE", 32, effect="NOXIOUS", desc="O oponente da batalha fica tóxico."),
        m("Faint Attack", "WHITE", 36, damage=40),
    ])

add(48, "murkrow", "Murkrow", "SOMBRIO", "VOADOR", "R", 3, None,
    [
        m("Sucker Punch", "GOLD", 28, damage=10),
        m("Whirlwind", "PURPLE", 32,
          desc="O oponente da batalha volta ao banco e ganha Wait."),
        m("Astonish", "WHITE", 28, damage=30),
        m("Miss", "RED", 8),
    ])

# ── Geração 2 (cont.) e Legendários: Scizor a Celebi (ID-49 a ID-61) ─────────

add(49, "scizor", "Scizor", "INSETO", "ACO", "EX", 2,
    "Quebra-Aço - Se um oponente de batalha deste Pokémon girar um ataque de 130 ou mais de dano, anexa um marcador Cracked ao oponente.",
    [
        m("Miss", "RED", 4),
        m("Crushing Squeeze", "WHITE", 32, damage=150,
          desc="Se o oponente da batalha tiver um marcador Cracked, este Pokémon não recebe dano e o oponente é derrotado (exceto se for do tipo Fogo)."),
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 24),
        m("Bullet Punch", "GOLD", 32, damage=30,
          desc="Dano original ilegível no documento (OCR mostrou 'Gold' na coluna de dano); valor estimado com base em Pokémon semelhantes."),
    ])

add(50, "heracross", "Heracross", "INSETO", "LUTADOR", "EX", 2,
    "Chifre Real - Enquanto este Pokémon estiver em campo, seus Pokémon ganham +1 giro em ataques de 'gire de novo'. Enquanto um marcador Cracked estiver anexado, os ataques Azuis do oponente se tornam Miss.",
    [
        m("Miss", "RED", 8),
        m("Pin Missile", "WHITE", 60, damage=40,
          desc="Gira de novo enquanto Pin Missile sair - o dano é multiplicado pelo número de giros."),
        m("Miss", "RED", 4),
        m("Heavy Hurl", "BLUE", 24,
          desc="O oponente da batalha é movido para um ponto a 2 passos e recebe um marcador MP-2."),
    ])

add(51, "houndour", "Houndour", "SOMBRIO", "FOGO", "UC", 3,
    "Caça Infernal - Este Pokémon e os Pokémon que batalham com ele ficam queimados depois da batalha.",
    [
        m("Miss", "RED", 12),
        m("Crunch", "WHITE", 52, damage=40),
        m("Miss", "RED", 8),
        m("Roar", "PURPLE", 24,
          desc="O oponente da batalha é empurrado 1 passo para trás (o oponente escolhe o ponto) e ganha Wait."),
    ])

add(52, "houndoom", "Houndoom", "SOMBRIO", "FOGO", "EX", 2,
    "Fogo Maligno - Enquanto este Pokémon estiver em campo, seus Pokémon do tipo Fogo causam +10 de dano para cada Pokémon queimado em campo. Se este Pokémon estiver queimado, os Pokémon que batalharem com ele ficam queimados.",
    [
        m("Miss", "RED", 8),
        m("Flamethrower", "WHITE", 36, damage=130,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica queimado."),
        m("Fiery Breath", "PURPLE", 28, effect="BURN",
          desc="O oponente da batalha fica queimado. Você pode girar por um Pokémon a até 2 passos - se sair ataque Branco, esse Pokémon também fica queimado."),
        m("Sucker Punch", "GOLD", 16, damage=60),
        m("Miss", "RED", 8),
    ])

add(56, "larvitar", "Larvitar", "ROCHA", "TERRENO", "UC", 2, None,
    [
        m("Bite", "WHITE", 28, damage=20),
        m("Thrash", "WHITE", 40, damage=50, desc="Este Pokémon fica confuso."),
        m("Crunch", "WHITE", 20, damage=70),
        m("Miss", "RED", 8),
    ])

add(57, "pupitar", "Pupitar", "ROCHA", "TERRENO", "UC", 3,
    "Atordoar - Todos os ataques Azuis dos oponentes de batalha deste Pokémon se tornam Miss.",
    [
        m("Miss", "RED", 4),
        m("Thrash", "WHITE", 64, damage=50, desc="Este Pokémon fica confuso."),
        m("Bite", "WHITE", 16, damage=20),
        m("Protect", "BLUE", 12, desc="Este Pokémon ganha Wait."),
    ])

add(58, "tyranitar", "Tyranitar", "ROCHA", "SOMBRIO", "EX", 2, None,
    [
        m("Taunt", "PURPLE", 16,
          desc="Até o final do seu próximo turno, se o Pokémon atingido por este ataque girar Roxo ou Azul, o resultado é deslocado em sentido horário até sair um ataque que não seja Roxo nem Azul."),
        m("Miss", "RED", 4),
        m("Rock Slide", "WHITE", 36, damage=150,
          desc="Se o oponente ou Pokémon adversários adjacentes a ele tiverem Wait, esses Pokémon são derrotados. O oponente e os adjacentes ganham Wait 3."),
        m("Mountain Topple", "WHITE", 40, damage=170,
          desc="Todos os outros Pokémon em campo giram. Os que sortearem ataque Azul são derrotados."),
    ])

add(53, "raikou", "Raikou", "ELETRICO", None, "EX", 2,
    "Sem Parar - Este Pokémon não pode ter Wait. Pode fazer MP Move passando por Pokémon paralisados ou com Wait.",
    [
        m("Miss", "RED", 4),
        m("Thunder", "WHITE", 24, damage=110),
        m("Thunderous Blow", "GOLD", 24, damage=80,
          desc="O oponente da batalha e uma sucessão de Pokémon adjacentes a ele (excluindo este) ficam Paralisados."),
        m("Bite", "WHITE", 24, damage=50),
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 16),
    ])

add(54, "entei", "Entei", "FOGO", None, "EX", 2,
    "Avanço Flamejante - Pode fazer MP Move passando por Pokémon queimados. Este Pokémon no banco ganha +1 PM.",
    [
        m("Stomp", "WHITE", 24, damage=60),
        m("Sacred Fire", "WHITE", 36, damage=140, desc="O oponente da batalha fica queimado."),
        m("Fierce Roar", "PURPLE", 24,
          desc="Troca o oponente da batalha com um Pokémon adversário adjacente a ele. Esses Pokémon ganham Wait."),
        m("Miss", "RED", 12),
    ])

add(55, "suicune", "Suicune", "AGUA", None, "EX", 2,
    "Purificação - Quando este Pokémon vai do banco para o campo, remove condições especiais e marcadores Curse de todos os seus Pokémon no duelo.",
    [
        m("Dodge", "BLUE", 8),
        m("Ice Beam", "WHITE", 24, damage=60,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica congelado."),
        m("Sheer Cold", "PURPLE", 24,
          desc="O oponente da batalha é derrotado. Se estiver congelado, é excluído do duelo."),
        m("Hydro Pump", "WHITE", 24, damage=100),
        m("Dodge", "BLUE", 8),
        m("Miss", "RED", 8),
    ])

add(59, "lugia", "Lugia", "PSIQUICO", "VOADOR", "EX", 2,
    "Furor Marinho - Este Pokémon causa +10 de dano para cada Pokémon do tipo Água no banco de cada jogador.",
    [
        m("Miss", "RED", 8),
        m("Aeroblast", "WHITE", 24, damage=120),
        m("Cyclone", "PURPLE", 24,
          desc="O oponente da batalha é movido para o banco e ganha Wait."),
        m("Aeroblast", "WHITE", 24, damage=120),
        m("Miss", "RED", 8),
        m("Dodge", "BLUE", 8),
    ])

add(60, "ho-oh", "Ho-Oh", "FOGO", "VOADOR", "EX", 2,
    "Luz Serena - Quando este Pokémon é derrotado, todos os Pokémon no seu P.C. vão para o banco (exceto este).",
    [
        m("Gust", "WHITE", 28, damage=50),
        m("Fire Blast", "WHITE", 32, damage=100),
        m("Rainbow Wing", "PURPLE", 24, desc="Este Pokémon se move 2 passos."),
        m("Miss", "RED", 12),
    ])

add(61, "celebi", "Celebi", "PSIQUICO", "GRAMA", "EX", 3,
    "Viagem no Tempo - Se não estiver afetado por condição especial, pode fazer MP Move sobre Pokémon adversários não-Voadores em campo. No início do seu turno, pode usar esta habilidade para retornar o duelo ao início do seu turno anterior e excluir este Pokémon do duelo (o tempo restante de ambos não muda). Até o fim do próximo turno do oponente, nenhum jogador pode usar Viagem no Tempo novamente.",
    [
        m("Miss", "RED", 4),
        m("Energy Ball", "PURPLE", 32,
          desc="Enquanto o oponente da batalha estiver em campo, todos os seus ataques Azuis que não sejam Dodge se tornam Miss."),
        m("Grass Knot", "WHITE", 44, damage=40,
          desc="Se o ataque do oponente for 120 de dano ou mais, o oponente é derrotado em vez deste Pokémon."),
        m("Backtrack", "BLUE", 18,
          desc="Este Pokémon pode ir para o banco. Se fizer isso, seu oponente de batalha também vai para o banco e retorna à forma que tinha no início do duelo."),
    ])

# ── Geração 3 (parte 1): Treecko a Slaking (ID-62 a ID-79) ───────────────────

add(62, "treecko", "Treecko", "GRAMA", None, "UC", 3,
    "Escalada de Parede - Enquanto no banco, este Pokémon pode fazer MP move sobre Pokémon que não sejam do tipo Fantasma.",
    [
        m("Quick Attack", "GOLD", 16, damage=20),
        m("Slam", "WHITE", 56, damage=40),
        m("Dodge", "BLUE", 20),
        m("Miss", "RED", 4),
    ])

add(63, "grovyle", "Grovyle", "GRAMA", None, "R", 2,
    "Salto na Floresta - No início do seu turno, em vez de um MP move, você pode mover este Pokémon para um ponto ao lado de um Pokémon do tipo Grama adjacente. Se houver uma sucessão de Pokémon do tipo Grama adjacentes, pode mover-se para junto de qualquer um deles.",
    [
        m("Quick Attack", "GOLD", 24, damage=20),
        m("Leaf Blade", "WHITE", 44, damage=70,
          desc="Gira de novo - se sair Leaf Blade novamente, causa +50 de dano."),
        m("Miss", "RED", 8),
        m("Dodge", "BLUE", 20),
    ])

add(64, "sceptile", "Sceptile", "GRAMA", None, "EX", 2, None,
    [
        m("Quick Attack", "GOLD", 20, damage=30),
        m("Leaf Blade", "WHITE", 44, damage=90,
          desc="Gira de novo - se sair Leaf Blade novamente, causa +50 de dano."),
        m("Miss", "RED", 4),
        m("Stealth Hit", "BLUE", 28,
          desc="Este Pokémon pode saltar sobre o oponente da batalha e cair 1 passo além."),
    ])

add(65, "torchic", "Torchic", "FOGO", None, "UC", 3,
    "Vazamento de Fogo - Qualquer Pokémon do tipo Grama ou Inseto que batalhar com este Pokémon ficará queimado.",
    [
        m("Peck", "WHITE", 24, damage=30),
        m("Ember", "WHITE", 48, damage=40),
        m("Miss", "RED", 24),
    ])

add(66, "combusken", "Combusken", "FOGO", "LUTADOR", "R", 2, None,
    [
        m("Miss", "RED", 8),
        m("Cyclone Kick", "PURPLE", 28,
          desc="Move o oponente da batalha para o P.C."),
        m("Miss", "RED", 8),
        m("Weak Spot Kick", "WHITE", 52, damage=60, desc="O oponente da batalha ganha Wait 4."),
    ])

add(67, "blaziken", "Blaziken", "FOGO", "LUTADOR", "EX", 2, None,
    [
        m("Miss", "RED", 8),
        m("Cyclone Kick", "PURPLE", 24,
          desc="Move o oponente da batalha para o P.C."),
        m("Jet Kick", "GOLD", 24, damage=20,
          desc="Se o oponente da batalha for derrotado, este Pokémon se move 2 passos além após a batalha e ganha Wait."),
        m("Miss", "RED", 8),
        m("Flare Blitz", "WHITE", 32, damage=130,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica queimado."),
    ])

add(68, "mudkip", "Mudkip", "AGUA", None, "UC", 3, None,
    [
        m("Water Gun", "WHITE", 48, damage=30),
        m("Take Down", "WHITE", 24, damage=50),
        m("Dodge", "BLUE", 16),
        m("Miss", "RED", 8),
    ])

add(69, "marshtomp", "Marshtomp", "AGUA", "TERRENO", "R", 1,
    "Cobertura de Lama - Se houver 3 ou mais Pokémon do tipo Água em campo, seus Pokémon do tipo Terrestre com PM1 ou menor ganham +1PM.",
    [
        m("Mud Shot", "WHITE", 44, damage=80, desc="Anexa um marcador MP-1 ao oponente da batalha."),
        m("Muddy Water", "PURPLE", 24,
          desc="O oponente da batalha e um Pokémon vizinho diretamente atrás dele são empurrados 1 passo para trás e ganham Wait."),
        m("Protect", "BLUE", 16, desc="Este Pokémon ganha Wait."),
        m("Miss", "RED", 12),
    ])

add(70, "swampert", "Swampert", "AGUA", "TERRENO", "EX", 1,
    "Cobertura de Lama - Se houver 3 ou mais Pokémon do tipo Água em campo, seus Pokémon do tipo Terrestre com PM1 ou menor ganham +1PM.",
    [
        m("Hammer Arm", "WHITE", 36, damage=150, desc="Este Pokémon ganha Wait."),
        m("Tractor", "BLUE", 28,
          desc="Você pode empurrar este Pokémon 1 passo para trás e mover o oponente da batalha para a posição anterior deste Pokémon. Se fizer isso, o oponente ganha Wait."),
        m("Ice Punch", "WHITE", 20, damage=80,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica congelado."),
        m("Miss", "RED", 12),
    ])

add(71, "lotad", "Lotad", "AGUA", "GRAMA", "C", 2,
    "Folha Aquática - Seus Pokémon do tipo Inseto podem se mover sobre este Pokémon usando um MP move.",
    [
        m("Miss", "RED", 12),
        m("Bubble Beam", "WHITE", 28, damage=40),
        m("Grass Knot", "WHITE", 28, damage=30,
          desc="Se o ataque do oponente for 120 de dano ou mais, o oponente é derrotado em vez deste Pokémon."),
        m("Dodge", "BLUE", 28),
    ])

add(72, "lombre", "Lombre", "AGUA", "GRAMA", "UC", 2,
    "Escorregadio - Apenas uma vez, antes de ser cercado, este Pokémon pode trocar de lugar com um Pokémon adjacente.",
    [
        m("Miss", "RED", 12),
        m("Bubble Beam", "WHITE", 28, damage=50),
        m("Grass Knot", "WHITE", 28, damage=40,
          desc="Se o ataque do oponente for 120 de dano ou mais, o oponente é derrotado em vez deste Pokémon."),
        m("Sabotage", "PURPLE", 28, effect="IMMOBILIZED", turns=3,
          desc="O oponente da batalha ganha Wait 3."),
    ])

add(73, "ludicolo", "Ludicolo", "AGUA", "GRAMA", "R", 2,
    "Venha Aqui - Quando este Pokémon vai do banco para o campo, ou aparece como Evolução, cada jogador move um Pokémon do banco para seu ponto de entrada (o oponente primeiro).",
    [
        m("Miss", "RED", 12),
        m("Hydro Pump", "WHITE", 28, damage=70),
        m("Grass Knot", "WHITE", 28, damage=50,
          desc="Se o ataque do oponente for 120 de dano ou mais, o oponente é derrotado em vez deste Pokémon."),
        m("Lively Dance", "PURPLE", 28,
          desc="Todos os Pokémon em campo giram. Os que sortearem ataque Branco ganham Wait."),
    ])

add(74, "ralts", "Ralts", "PSIQUICO", "FADA", "R", 3,
    "Sensor Fantasma - Pokémon adversários não podem fazer MP move através deste Pokémon usando o efeito de uma Habilidade.",
    [
        m("Confusion", "WHITE", 40, damage=30),
        m("Imprison", "PURPLE", 44,
          desc="Anexa um marcador Imprisoned ao oponente da batalha. Enquanto o marcador estiver anexado, o ataque que o oponente girar nesse turno se torna Miss."),
        m("Miss", "RED", 12),
    ])

add(75, "kirlia", "Kirlia", "PSIQUICO", "FADA", "R", 2,
    "Sensor Fantasma - Pokémon adversários não podem fazer MP move através deste Pokémon usando o efeito de uma Habilidade.",
    [
        m("Moonblast", "WHITE", 20, damage=60),
        m("Super Psy", "WHITE", 32, damage=40,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica confuso."),
        m("Psycho Switch", "PURPLE", 32,
          desc="Coloca o oponente da batalha no banco. Este Pokémon então troca de lugar com outro dos seus Pokémon do campo, banco ou P.C."),
        m("Miss", "RED", 12),
    ])

add(76, "gardevoir", "Gardevoir", "PSIQUICO", "FADA", "EX", 2,
    "Sensor Fantasma - Pokémon adversários não podem fazer MP move através deste Pokémon usando o efeito de uma Habilidade.",
    [
        m("Warp Hole", "PURPLE", 32,
          desc="Troca o oponente da batalha por um Pokémon do campo, banco ou P.C. Em seguida, esses Pokémon devem esperar (Wait 3)."),
        m("Moonblast", "WHITE", 32, damage=140),
        m("Super Psy", "WHITE", 24, damage=100,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica confuso."),
        m("Miss", "RED", 8),
    ])

add(77, "slakoth", "Slakoth", "NORMAL", None, "UC", 1,
    "Sonolência - Este Pokémon desperta da condição Sono no início do seu turno e pode ser usado novamente.",
    [
        m("Scratch", "WHITE", 32, damage=50),
        m("Big Yawn", "PURPLE", 48, effect="SLEEP",
          desc="Este Pokémon e o oponente da batalha adormecem."),
        m("Miss", "RED", 16),
    ])

add(78, "vigoroth", "Vigoroth", "NORMAL", None, "UC", 3,
    "Espírito Vital Máximo - Este Pokémon pode fazer MP move até o limite do seu alcance de PM.",
    [
        m("Fury Swipes", "WHITE", 36, damage=30,
          desc="Gira de novo enquanto Fury Swipes sair - o dano é multiplicado pelo número de giros."),
        m("Slash", "WHITE", 40, damage=50),
        m("Miss", "RED", 20),
    ])

add(79, "slaking", "Slaking", "NORMAL", None, "R", 2,
    "Negligência - Este Pokémon não pode atacar no mesmo turno em que fez um MP move.",
    [
        m("Hammer Arm", "WHITE", 44, damage=80, desc="Este Pokémon ganha Wait."),
        m("Rest", "PURPLE", 16, desc="Este Pokémon adormece."),
        m("Miss", "RED", 12),
        m("Fake Sleep", "PURPLE", 24, desc="Permite que este Pokémon gire novamente uma vez."),
    ])

# ── Geração 3 (parte 2): Aron a Salamence (ID-80 a ID-91) ────────────────────

add(80, "aron", "Aron", "ACO", "ROCHA", "UC", 2, None,
    [
        m("Tackle", "WHITE", 24, damage=20),
        m("Big Bite", "WHITE", 48, damage=40,
          desc="Se o tipo do oponente da batalha for Aço, este Pokémon não recebe dano e o oponente é derrotado."),
        m("Protect", "BLUE", 20, desc="Este Pokémon ganha Wait."),
        m("Miss", "RED", 4),
    ])

add(81, "lairon", "Lairon", "ACO", "ROCHA", "C", 2, None,
    [
        m("Miss", "RED", 4),
        m("Tackle", "WHITE", 24, damage=30),
        m("Take Down", "WHITE", 20, damage=90),
        m("Harden", "WHITE", 28, damage=0,
          desc="Se o dano causado a este Pokémon for 100 ou menos, ele não desmaia."),
        m("Protect", "BLUE", 20, desc="Este Pokémon ganha Wait."),
    ])

add(82, "aggron", "Aggron", "ACO", "ROCHA", "EX", 1,
    "Territorialidade - Enquanto este Pokémon estiver no banco, ganha +1 PM. Pokémon adversários não podem passar por ele com MP move. Qualquer Pokémon adversário que usar MP move para ficar ao lado deste Pokémon deve atacá-lo nesse turno.",
    [
        m("Miss", "RED", 8),
        m("Heavy Slam", "WHITE", 16, damage=90,
          desc="Se o oponente da batalha tiver PM3 ou mais, este Pokémon não recebe dano e o oponente é derrotado."),
        m("Protect", "BLUE", 16, desc="Este Pokémon ganha Wait."),
        m("Brutal Swing", "PURPLE", 16,
          desc="Pokémon adversários adjacentes são empurrados 1 passo para trás e ganham Wait."),
        m("Miss", "RED", 4),
        m("Iron Tail", "WHITE", 36, damage=140),
    ])

add(83, "trapinch", "Trapinch", "TERRENO", None, "UC", 1,
    "Armadilha de Areia - Qualquer Pokémon adjacente a este, exceto do tipo Voador, não pode usar MP move (mas ainda pode passar por ele).",
    [
        m("Miss", "RED", 4),
        m("Protect", "BLUE", 20, desc="Este Pokémon ganha Wait."),
        m("Crunch", "WHITE", 28, damage=70),
        m("Bite", "WHITE", 20, damage=20),
        m("Dig", "PURPLE", 24,
          desc="Este Pokémon se afasta de qualquer Pokémon anexado a ele."),
    ])

add(84, "vibrava", "Vibrava", "TERRENO", "DRAGAO", "UC", 2,
    "Som Vibrante - Pokémon adversários não podem passar ao lado deste Pokémon usando MP move. Se o Pokémon que o oponente moveu no último turno estiver ao lado deste, ele não pode atacar.",
    [
        m("Miss", "RED", 4),
        m("Supersonic", "PURPLE", 24, effect="CONFUSION", desc="O oponente da batalha fica confuso."),
        m("Dragon Breath", "WHITE", 16, damage=70),
        m("Screech", "PURPLE", 24, desc="Todos os Pokémon vizinhos ganham Wait."),
        m("Mud Shot", "WHITE", 28, damage=40, desc="Anexa um marcador MP-1 ao oponente da batalha."),
    ])

add(85, "flygon", "Flygon", "TERRENO", "DRAGAO", "R", 2,
    "Tempestade de Areia - Os efeitos Fly Away do oponente são anulados. Quando este Pokémon está em campo e evoluído, os efeitos Soar dos Pokémon em campo são anulados.",
    [
        m("Miss", "RED", 4),
        m("Dragon Claw", "WHITE", 16, damage=100),
        m("Draco Meteor", "PURPLE", 28,
          desc="Outro Pokémon em campo gira - se sair Miss ou ataque Branco de 70 ou mais, é derrotado."),
        m("Dragon Claw", "WHITE", 16, damage=100),
        m("Miss", "RED", 4),
        m("Fly Away", "PURPLE", 28,
          desc="Este Pokémon salta sobre o oponente da batalha e cai em um ponto ao redor."),
    ])

add(88, "absol", "Absol", "SOMBRIO", None, "EX", 2,
    "Pressão - Enquanto este Pokémon estiver em campo, Wait dura 1 turno mais para Pokémon adversários quando você os faz ganhar Wait.",
    [
        m("Sucker Punch", "GOLD", 44, damage=80),
        m("Miss", "RED", 4),
        m("Swords Dance", "WHITE", 24, damage=0,
          desc="Gira de novo até sair um ataque diferente de Swords Dance. Ataques de dano causam o dobro."),
        m("Dodge", "BLUE", 24),
    ])

add(86, "feebas", "Feebas", "AGUA", None, "UC", 2,
    "Evolução para a Beleza - Quando este Pokémon desmaia em batalha, pode evoluir sem ir para o P.C.",
    [
        m("Flail", "WHITE", 48, damage=10),
        m("Waterfall", "WHITE", 44, damage=20,
          desc="Se o oponente da batalha for do tipo Água, este Pokémon não recebe dano e avança para um ponto além do oponente."),
        m("Miss", "RED", 4),
    ])

add(87, "milotic", "Milotic", "AGUA", None, "EX", 2,
    "Escama Maravilhosa - Quando este Pokémon vai do banco para o campo, ou aparece como evolução, remove a condição Queimado dos seus Pokémon. Se este Pokémon estiver afetado por condição especial, seus oponentes causam -50 de dano a ele.",
    [
        m("Aqua Tail", "WHITE", 36, damage=130, desc="O oponente da batalha ganha Wait."),
        m("Magic Coat", "PURPLE", 16,
          desc="Se o ataque do oponente fosse anexar marcador ou condição especial a este Pokémon, o efeito é redirecionado ao oponente."),
        m("Miss", "RED", 8),
        m("Blue Pulse", "BLUE", 36,
          desc="Este Pokémon pode fazer um oponente a até 2 passos ficar confuso."),
    ])

add(89, "bagon", "Bagon", "DRAGAO", None, "UC", 2, None,
    [
        m("Bite", "WHITE", 32, damage=30),
        m("Headbutt", "WHITE", 20, damage=90),
        m("Bite", "WHITE", 32, damage=30),
        m("Miss", "RED", 12),
    ])

add(90, "shelgon", "Shelgon", "DRAGAO", None, "C", 1, None,
    [
        m("Miss", "RED", 4),
        m("Flamethrower", "WHITE", 24, damage=90, desc="O oponente da batalha fica queimado."),
        m("Miss", "RED", 4),
        m("Conceal", "WHITE", 64, damage=10,
          desc="Se o dano causado a este Pokémon for 100 ou menos, ele não desmaia."),
    ])

add(91, "salamence", "Salamence", "DRAGAO", "VOADOR", "R", 2, None,
    [
        m("Fly Away", "PURPLE", 24,
          desc="Este Pokémon salta sobre o oponente da batalha e cai em um ponto ao redor."),
        m("Dragon Breath", "WHITE", 64, damage=70),
        m("Miss", "RED", 8),
    ])

# ── Geração 3 (parte 3) e Legendários: Beldum a Deoxys (ID-92 a ID-103) ──────

add(92, "beldum", "Beldum", "ACO", "PSIQUICO", "UC", 3,
    "Corpo Magnético - No início do seu turno, se algum outro Metang ou Metagross seu estiver a até 2 passos, você pode mover este Pokémon para um ponto a 2 passos de distância em vez de um MP move. Se fizer isso, seu turno termina.",
    [
        m("Miss", "RED", 4),
        m("Electromagnetic Blaster", "WHITE", 68, damage=50,
          desc="Se houver um Pokémon do tipo Elétrico ao lado deste, +50 de dano."),
        m("Dodge", "BLUE", 24),
    ])

add(93, "metang", "Metang", "ACO", "PSIQUICO", "R", 2,
    "Corpo Magnético - No início do seu turno, se algum outro Metang ou Metagross seu estiver a até 2 passos, você pode mover este Pokémon para um ponto a 2 passos de distância em vez de um MP move. Se fizer isso, seu turno termina.",
    [
        m("Miss", "RED", 4),
        m("Bullet Punch", "GOLD", 28, damage=30),
        m("Tractor Beam", "BLUE", 16,
          desc="Move outro Pokémon do tipo Aço a até 2 passos deste para um ponto a 1-2 passos dele."),
        m("Metal Claw", "WHITE", 32, damage=90),
        m("Tractor Beam", "BLUE", 16,
          desc="Move outro Pokémon do tipo Aço a até 2 passos deste para um ponto a 1-2 passos dele."),
    ])

add(94, "metagross", "Metagross", "ACO", "PSIQUICO", "EX", 1, None,
    [
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 4),
        m("Metal Claw", "WHITE", 16, damage=130),
        m("Dodge", "BLUE", 4),
        m("Hyper Beam", "WHITE", 24, damage=160,
          desc="Se o oponente da batalha for derrotado, o próximo turno será sempre do outro jogador."),
        m("Dodge", "BLUE", 4),
        m("Metal Claw", "WHITE", 16, damage=130),
        m("Dodge", "BLUE", 4),
        m("Bullet Punch", "GOLD", 20, damage=50),
    ])

add(95, "regirock", "Regirock", "ROCHA", None, "EX", 2,
    "Bloqueio: Azul - Se este Pokémon não estiver afetado por condição especial, em vez de um MP move, ele pode forçar um Pokémon adversário a até 2 passos a girar. Se sair ataque Azul, anexa um marcador Lock-On a esse Pokémon. Seu turno termina.",
    [
        m("Miss", "RED", 4),
        m("Protect", "BLUE", 16, desc="Seus Pokémon ganham Wait."),
        m("Hammer Arm", "WHITE", 32, damage=160, desc="Este Pokémon ganha Wait."),
        m("Explosion", "PURPLE", 20, desc="Este Pokémon e todos os vizinhos desmaiam."),
        m("Multiblast", "WHITE", 24, damage=120,
          desc="Derrota todos os Pokémon que tiverem marcadores Lock-On anexados."),
    ])

add(96, "regice", "Regice", "GELO", None, "EX", 2,
    "Bloqueio: Roxo - No início do seu turno, em vez de um MP move, este Pokémon pode forçar Pokémon adversários a até 2 passos a girar. Se sair ataque Roxo, anexa um marcador Lock-On a esse Pokémon. Seu turno termina.",
    [
        m("Miss", "RED", 4),
        m("Protect", "BLUE", 16, desc="Seus Pokémon ganham Wait."),
        m("Hammer Arm", "WHITE", 32, damage=160, desc="Este Pokémon ganha Wait."),
        m("Ice Beam", "WHITE", 20, damage=90,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica congelado."),
        m("Multiblast", "WHITE", 24, damage=120,
          desc="Derrota todos os Pokémon que tiverem marcadores Lock-On anexados."),
    ])

add(97, "registeel", "Registeel", "ACO", None, "EX", 2,
    "Bloqueio: Dourado - Se este Pokémon não estiver afetado por condição especial, em vez de um MP move, ele pode forçar um Pokémon adversário a até 2 passos a girar. Se sair ataque Dourado, anexa um marcador Lock-On a esse Pokémon. Seu turno termina.",
    [
        m("Miss", "RED", 4),
        m("Protect", "BLUE", 16, desc="Seus Pokémon ganham Wait."),
        m("Hammer Arm", "WHITE", 32, damage=160, desc="Este Pokémon ganha Wait."),
        m("Thunder Wave", "PURPLE", 20, effect="PARALYSIS", desc="O oponente da batalha fica paralisado."),
        m("Multiblast", "WHITE", 24, damage=120,
          desc="Derrota todos os Pokémon que tiverem marcadores Lock-On anexados."),
    ])

add(98, "latias", "Latias", "DRAGAO", "PSIQUICO", "EX", 3,
    "Alucinação - Se este Pokémon for derrotado, ele é removido do duelo.",
    [
        m("Fly Away", "PURPLE", 12,
          desc="Este Pokémon salta sobre o oponente da batalha e cai em um ponto ao redor."),
        m("Mist Ball", "WHITE", 20, damage=140),
        m("Dragon Breath", "WHITE", 32, damage=120),
        m("Fly Away", "PURPLE", 12,
          desc="Este Pokémon salta sobre o oponente da batalha e cai em um ponto ao redor."),
        m("Miss", "RED", 4),
        m("Psychic", "WHITE", 16, damage=80),
    ])

add(99, "latios", "Latios", "DRAGAO", "PSIQUICO", "EX", 3,
    "Alucinação - Se este Pokémon for derrotado, ele é removido do duelo.",
    [
        m("Dodge", "BLUE", 12),
        m("Luster Purge", "WHITE", 20, damage=150),
        m("Dragon Breath", "WHITE", 32, damage=120),
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 12),
        m("Psychic", "WHITE", 16, damage=90),
    ])

add(100, "kyogre", "Kyogre", "AGUA", None, "EX", 2,
    "Furor Primordial - Se o oponente tiver Groudon, Kyogre ou Rayquaza em campo, este Pokémon causa +20 de dano.",
    [
        m("Break Energy", "WHITE", 20, damage=90, desc="Os efeitos de Placas de Energia do oponente são perdidos."),
        m("Dodge", "BLUE", 8),
        m("Tidal Wave", "PURPLE", 32,
          desc="Todos os Pokémon vizinhos são empurrados 3 passos para trás e ganham Wait."),
        m("Dodge", "BLUE", 8),
        m("Hydro Pump", "WHITE", 20, damage=130),
        m("Miss", "RED", 8),
    ])

add(101, "groudon", "Groudon", "TERRENO", None, "EX", 2,
    "Furor Primordial - Se o oponente tiver Groudon, Kyogre ou Rayquaza em campo, este Pokémon causa +20 de dano.",
    [
        m("Miss", "RED", 8),
        m("Smash", "WHITE", 48, damage=130),
        m("Break Energy", "WHITE", 20, damage=90, desc="Os efeitos de Placas de Energia do oponente são perdidos."),
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 16),
    ])

add(102, "rayquaza", "Rayquaza", "DRAGAO", "VOADOR", "EX", 1,
    "Furor Primordial - Se o oponente tiver Groudon, Kyogre ou Rayquaza em campo, este Pokémon causa +20 de dano.",
    [
        m("Miss", "RED", 4),
        m("Fly", "PURPLE", 24,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Miss", "RED", 4),
        m("Extreme Speed", "GOLD", 36, damage=140),
        m("Miss", "RED", 4),
        m("Break Energy", "WHITE", 24, damage=60, desc="Os efeitos de Placas de Energia do oponente são perdidos."),
    ])

add(103, "deoxys", "Deoxys", "PSIQUICO", None, "EX", 2, None,
    [
        m("Miss", "RED", 8),
        m("Psychic", "WHITE", 16, damage=50),
        m("Psycho Boost", "WHITE", 24, damage=90),
        m("Psychic", "WHITE", 16, damage=50),
        m("Teleport", "PURPLE", 24, desc="Este Pokémon se move 2 passos."),
    ])

# ── Geração 4 (parte 1): Turtwig a Skorupi (ID-104 a ID-119) ─────────────────

add(104, "turtwig", "Turtwig", "GRAMA", None, "UC", 2, None,
    [
        m("Tackle", "WHITE", 28, damage=20),
        m("Bite", "WHITE", 32, damage=50),
        m("Withdraw", "BLUE", 32, desc="Este Pokémon ganha Wait."),
        m("Miss", "RED", 4),
    ])

add(105, "grotle", "Grotle", "GRAMA", None, "C", 2,
    "Alicerce - Seus Pokémon podem se mover sobre este Pokémon.",
    [
        m("Miss", "RED", 4),
        m("Tackle", "WHITE", 28, damage=40),
        m("Miss", "RED", 4),
        m("Bite", "WHITE", 28, damage=50),
        m("Miss", "RED", 4),
        m("Withdraw", "BLUE", 28, desc="Este Pokémon ganha Wait."),
    ])

add(106, "torterra", "Torterra", "GRAMA", "TERRENO", "EX", 1,
    "Pedra Angular - Outros Pokémon podem se mover sobre este Pokémon usando um MP move.",
    [
        m("Bite", "WHITE", 28, damage=100),
        m("Miss", "RED", 4),
        m("Earthquake", "WHITE", 28, damage=120,
          desc="Todos os Pokémon em campo giram suas roletas. Quem sair Miss vai para o P.C."),
        m("Miss", "RED", 4),
        m("Withdraw", "BLUE", 28, desc="Este Pokémon ganha Wait."),
        m("Miss", "RED", 4),
    ])

add(107, "chimchar", "Chimchar", "FOGO", None, "UC", 3, None,
    [
        m("Miss", "RED", 24),
        m("Fury Swipes", "WHITE", 48, damage=20,
          desc="Gira de novo enquanto Fury Swipes sair - o dano é multiplicado pelo número de giros."),
        m("Taunt", "PURPLE", 24,
          desc="Se este Pokémon ou o oponente girar Roxo, devem girar de novo neste turno."),
    ])

add(108, "monferno", "Monferno", "FOGO", "LUTADOR", "C", 2, None,
    [
        m("Miss", "RED", 12),
        m("Fury Swipes", "WHITE", 40, damage=40,
          desc="Gira de novo enquanto Fury Swipes sair - o dano é multiplicado pelo número de giros."),
        m("Dodge", "BLUE", 12),
        m("Mid-Air Strike", "WHITE", 32, damage=60,
          desc="O Pokémon adversário é empurrado 1 espaço para trás. Este Pokémon ganha Wait."),
    ])

add(109, "infernape", "Infernape", "FOGO", "LUTADOR", "EX", 2, None,
    [
        m("Detect", "BLUE", 20, desc="No próximo turno, este Pokémon ganha +1 PM."),
        m("Drive Kick", "WHITE", 36, damage=150,
          desc="Se o oponente da batalha for derrotado, este Pokémon ocupa a posição anterior dele e ganha Wait."),
        m("Miss", "RED", 16),
        m("Mach Punch", "GOLD", 28, damage=70),
    ])

add(110, "piplup", "Piplup", "AGUA", None, "UC", 3, None,
    [
        m("Miss", "RED", 12),
        m("Bubble Beam", "WHITE", 12, damage=60),
        m("Miss", "RED", 8),
        m("Peck", "WHITE", 64, damage=30),
    ])

add(111, "prinplup", "Prinplup", "AGUA", None, "C", 2,
    "Entrada de Mergulho - Este Pokémon pode se mover um espaço adicional ao se mover do banco.",
    [
        m("Miss", "RED", 8),
        m("Hydro Pump", "WHITE", 24, damage=70),
        m("Miss", "RED", 8),
        m("Metal Claw", "WHITE", 56, damage=40),
    ])

add(112, "empoleon", "Empoleon", "AGUA", "ACO", "EX", 2,
    "Entrada de Mergulho - Ao mover este Pokémon do banco, ele só pode ir a um espaço de distância do ponto de entrada. Seu turno termina.",
    [
        m("Miss", "RED", 4),
        m("Ice Beam", "WHITE", 20, damage=70,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica congelado."),
        m("Aqua Jet", "GOLD", 32, damage=30),
        m("Hydro Pump", "WHITE", 32, damage=150),
        m("Miss", "RED", 4),
    ])

add(113, "honchkrow", "Honchkrow", "SOMBRIO", "VOADOR", "EX", 2,
    "Insônia - Este Pokémon não pode ser afetado pela condição Sono.",
    [
        m("Call", "BLUE", 24, desc="Move o Murkrow do seu P.C. para o seu banco."),
        m("Sucker Punch", "GOLD", 36, damage=60),
        m("Night Slash", "WHITE", 28, damage=120,
          desc="Se este Pokémon não for derrotado, você pode trocá-lo por um dos seus Pokémon no banco."),
        m("Miss", "RED", 8),
    ])

add(114, "gible", "Gible", "DRAGAO", "TERRENO", "UC", 1,
    "Avanço Repentino - Em vez de um MP move, este Pokémon pode se mover até 3 espaços de um Pokémon do oponente.",
    [
        m("Scary Face", "PURPLE", 20,
          desc="O oponente da batalha é empurrado 1 passo para trás (o oponente escolhe o ponto) e ganha Wait."),
        m("Slash", "WHITE", 44, damage=80),
        m("Scary Face", "PURPLE", 20,
          desc="O oponente da batalha é empurrado 1 passo para trás (o oponente escolhe o ponto) e ganha Wait."),
        m("Miss", "RED", 12),
    ])

add(115, "gabite", "Gabite", "DRAGAO", "TERRENO", "R", 2,
    "Véu de Areia - Se este Pokémon for atacado, ele pode passar por baixo do Pokémon do oponente para outro espaço ao lado dele (não ocorre batalha).",
    [
        m("Slash", "WHITE", 28, damage=90),
        m("Dragon Claw", "WHITE", 28, damage=120),
        m("Sand-Attack", "PURPLE", 28, effect="IMMOBILIZED", turns=1,
          desc="O oponente da batalha agora tem Wait."),
        m("Miss", "RED", 12),
    ])

add(116, "garchomp", "Garchomp", "DRAGAO", "TERRENO", "EX", 2,
    "Véu de Areia - Se este Pokémon for atacado, ele pode passar por baixo do Pokémon do oponente para outro espaço ao lado dele (não ocorre batalha).",
    [
        m("Miss", "RED", 8),
        m("Fire Fang", "WHITE", 20, damage=110,
          desc="O oponente da batalha fica queimado e ganha Wait."),
        m("Dragon Rush", "WHITE", 36, damage=160),
        m("Double Flight", "PURPLE", 28,
          desc="Este Pokémon salta sobre o oponente e cai em um ponto ao redor. Se o Pokémon adversário estiver com condição especial, é derrotado. Depois de se mover, este Pokémon pode atacar outro Pokémon adversário novamente, mas só uma vez."),
        m("Miss", "RED", 4),
    ])

add(117, "riolu", "Riolu", "LUTADOR", None, "UC", 3,
    "Foco Interior - Este Pokémon não pode entrar em estado Wait enquanto estiver em campo.",
    [
        m("Dodge", "BLUE", 12),
        m("Shockwave", "WHITE", 44, damage=40,
          desc="O oponente da batalha e todos os Pokémon diretamente atrás dele recebem Wait."),
        m("Miss", "RED", 12),
        m("Shake", "WHITE", 28, damage=30),
    ])

add(118, "lucario", "Lucario", "LUTADOR", "ACO", "EX", 2, None,
    [
        m("Dodge", "BLUE", 24),
        m("Aura Sphere", "PURPLE", 28,
          desc="O oponente da batalha ou um Pokémon atrás dele em linha reta a partir deste Pokémon é derrotado."),
        m("Miss", "RED", 8),
        m("Metal Claw", "WHITE", 36, damage=70),
    ])

add(119, "skorupi", "Skorupi", "VENENO", "INSETO", "UC", 2,
    "Picada Irritante - Qualquer Pokémon do tipo Psíquico que batalhar com este Pokémon ficará confuso.",
    [
        m("Bite", "WHITE", 28, damage=40),
        m("Miss", "RED", 8),
        m("Poison Sting", "PURPLE", 28, effect="POISON", desc="O oponente da batalha fica envenenado."),
        m("Protect", "BLUE", 24, desc="Este Pokémon ganha Wait."),
    ])

# ── Geração 4 (parte 2) e Legendários: Drapion a Zoroark (ID-120 a ID-135) ───

add(120, "drapion", "Drapion", "VENENO", "SOMBRIO", "R", 2,
    "Armadura de Batalha - Se o oponente da batalha girar um ataque com 10 ou mais de dano, você pode forçá-lo a girar de novo uma vez por turno.",
    [
        m("Cross Poison", "WHITE", 28, damage=70),
        m("Miss", "RED", 8),
        m("Venom Whip", "PURPLE", 28,
          desc="Um Pokémon a até 2 passos fica tóxico."),
        m("Noxious Fang", "WHITE", 28, damage=50,
          desc="Se este Pokémon for derrotado, o Pokémon oponente fica tóxico."),
        m("Miss", "RED", 4),
    ])

add(121, "leafeon", "Leafeon", "GRAMA", None, "R", 2,
    "Deslize na Mata - Este Pokémon pode fazer MP Move passando por Pokémon do tipo Grama em campo.",
    [
        m("Miss", "RED", 8),
        m("Dodge", "BLUE", 20),
        m("Razor Leaf", "WHITE", 40, damage=40),
        m("Grass Whistle", "PURPLE", 28, effect="SLEEP", desc="O oponente da batalha adormece."),
    ])

add(122, "glaceon", "Glaceon", "GELO", None, "R", 3, None,
    [
        m("Dodge", "BLUE", 20),
        m("Tackle", "WHITE", 44, damage=40),
        m("Diamond Dust", "PURPLE", 24, effect="FROZEN", desc="O oponente da batalha fica congelado."),
        m("Miss", "RED", 8),
    ])

add(123, "dialga", "Dialga", "ACO", "DRAGAO", "UX", 2,
    "Deslize Temporal - Este Pokémon não pode ter Wait enquanto estiver em campo. Se for derrotado por dano em batalha, o próximo turno será sempre seu.",
    [
        m("4-D Drive", "GOLD", 28, damage=100,
          desc="Move o oponente da batalha para o P.C. dele. O oponente não é derrotado."),
        m("Roar of Time", "WHITE", 32, damage=180,
          desc="Retorna o oponente da batalha ao estágio do início do duelo (exceto evolução)."),
        m("Aura Sphere", "PURPLE", 28,
          desc="O oponente da batalha ou um Pokémon atrás dele em linha reta a partir deste Pokémon é derrotado."),
        m("Miss", "RED", 8),
    ])

add(124, "palkia", "Palkia", "AGUA", "DRAGAO", "UX", 2,
    "Deslize Espacial - Quando este Pokémon é derrotado por dano em batalha, ele se recupera de condições especiais e se move para um dos seus pontos de entrada, se possível. Se fizer isso, ganha Wait.",
    [
        m("4-D Drive", "GOLD", 28, damage=100,
          desc="Move o oponente da batalha para o P.C. dele. O oponente não é derrotado."),
        m("Spacial Rend", "WHITE", 32, damage=170,
          desc="Se a batalha terminar em empate, o oponente é excluído do duelo."),
        m("Aura Sphere", "PURPLE", 28,
          desc="O oponente da batalha ou um Pokémon atrás dele em linha reta a partir deste Pokémon é derrotado."),
        m("Miss", "RED", 8),
    ])

add(125, "heatran", "Heatran", "FOGO", "ACO", "EX", 2,
    "Ataque Surpresa - Se houver um Pokémon do oponente no seu ponto de entrada e este Pokémon estiver no banco, no início do seu turno ele pode se mover ao lado desse Pokémon e batalhar. Após a batalha, se não for derrotado, vai para o seu banco.",
    [
        m("Miss", "RED", 4),
        m("Crunch", "WHITE", 12, damage=70),
        m("Miss", "RED", 4),
        m("Iron Head", "WHITE", 24, damage=150),
        m("Miss", "RED", 4),
        m("Crunch", "WHITE", 12, damage=70),
        m("Miss", "RED", 4),
        m("Magma Slide", "PURPLE", 32, effect="BURN",
          desc="O oponente da batalha é empurrado 2 passos para trás, fica queimado e ganha Wait."),
    ])

add(126, "regigigas", "Regigigas", "NORMAL", None, "UX", 0,
    "Início Lento - Este Pokémon ganha +1 PM para cada espécie de Regirock, Regice e Registeel em campo. Não pode ser movido por placas, Habilidades ou ataques de outros Pokémon.",
    [
        m("Hammer", "WHITE", 40, damage=300,
          desc="Se o oponente da batalha for derrotado, este Pokémon ocupa a posição anterior dele e ganha Wait."),
        m("Multiblast", "WHITE", 32, damage=150,
          desc="Derrota todos os Pokémon que tiverem marcadores Lock-On anexados."),
        m("Revenge", "WHITE", 16, damage=100,
          desc="O dano deste ataque aumenta em 20 para cada Pokémon seu no P.C."),
        m("Miss", "RED", 8),
    ])

add(127, "giratina", "Giratina", "FANTASMA", "DRAGAO", "UX", 1,
    "Distorção - No início do seu turno, em vez de um MP move, este Pokémon pode se mover através de um Pokémon adjacente para um ponto a 1-2 passos dele, podendo mudar de forma. Se mudar de forma, até o fim do seu próximo turno, efeitos de Habilidades do oponente que aumentariam dano de ataque o diminuem em vez disso. Seu turno termina.",
    [
        m("Draco Meteor", "PURPLE", 20,
          desc="Outro Pokémon em campo gira - se sair Miss ou ataque Branco de 70 ou mais, é derrotado."),
        m("Miss", "RED", 4),
        m("Shadow Claw", "WHITE", 44, damage=160,
          desc="Na próxima batalha do oponente, os ataques Roxos dele se tornam Miss."),
        m("Miss", "RED", 4),
        m("Shadow Sneak", "GOLD", 20, damage=80),
        m("Miss", "RED", 4),
    ])

add(128, "cresselia", "Cresselia", "PSIQUICO", None, "EX", 2,
    "Véu Crescente - Se este Pokémon estiver em campo e não afetado por condição especial, Moonblast dos seus Pokémon causa +50 de dano.",
    [
        m("Miss", "RED", 4),
        m("Lunar Dance", "PURPLE", 28,
          desc="Remove a condição Dormindo dos seus Pokémon e move todos os seus Pokémon que estão no P.C. para o banco. Em seguida, este Pokémon vai para o seu P.C."),
        m("Ice Beam", "WHITE", 20, damage=120,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica congelado."),
        m("Dodge", "BLUE", 4),
        m("Moonblast", "WHITE", 40, damage=110),
    ])

add(129, "darkrai", "Darkrai", "SOMBRIO", None, "UX", 2,
    "Sono Profundo - Este Pokémon pode fazer MP move sobre Pokémon não-Voadores em campo. Enquanto este estiver em campo, a condição Dormindo não é removida por efeitos de Habilidades. Se não estiver afetado por condição especial, Pokémon adormecidos a até 2 passos dele não podem ser trocados (tag).",
    [
        m("Miss", "RED", 4),
        m("Spacial Rend", "WHITE", 28, damage=100,
          desc="Se a batalha terminar em empate, o oponente é excluído do duelo."),
        m("Dark Void", "PURPLE", 28,
          desc="O oponente da batalha e os Pokémon adversários adjacentes a ele giram; quem sair ataque Branco adormece."),
        m("Miss", "RED", 4),
    ])

add(130, "arceus", "Arceus", "NORMAL", None, "UX", 2,
    "Multitipo - Quando este Pokémon vai para o campo, escolha um Pokémon em campo; enquanto estiver em campo, o tipo deste Pokémon se torna o tipo do Pokémon escolhido. Este Pokémon não é excluído do duelo nem movido ao Espaço Ultra pelos efeitos de ataque de Pokémon adversários.",
    [
        m("Miss", "RED", 4),
        m("Extreme Speed", "GOLD", 44, damage=150),
        m("Judgement", "WHITE", 28, damage=170,
          desc="Move o oponente da batalha para o P.C. dele. O oponente não é derrotado."),
        m("Return", "WHITE", 20, damage=100),
    ])

add(131, "victini", "Victini", "PSIQUICO", "FOGO", "EX", 3,
    "Estrela da Vitória - Se não estiver afetado por condição especial, pode fazer MP move sobre Pokémon adversários não-Voadores em campo. Pode girar de novo apenas uma vez em batalhas no seu turno.",
    [
        m("Fusion Bolt", "WHITE", 20, damage=40,
          desc="Se não afetado por condição especial e algum outro Pokémon seu em campo tiver Fusion Flare, exclui do duelo os Pokémon derrotados por este ataque."),
        m("Miss", "RED", 8),
        m("V-Create", "WHITE", 8, damage=160, desc="Anexa um marcador MP-1 a este Pokémon."),
        m("Miss", "RED", 8),
        m("Searing Shot", "PURPLE", 32, effect="BURN",
          desc="O oponente da batalha e o Pokémon em linha reta diretamente atrás dele ficam queimados."),
        m("Fusion Flare", "WHITE", 20, damage=40,
          desc="Se não afetado por condição especial e algum outro Pokémon seu em campo tiver Fusion Bolt, exclui do duelo os Pokémon derrotados por este ataque."),
    ])

add(132, "trubbish", "Trubbish", "VENENO", None, "UC", 2,
    "Fedor - Pokémon adversários do tipo Grama e Fada a até 2 passos deste Pokémon têm PM-1.",
    [
        m("Belch", "PURPLE", 28,
          desc="O oponente da batalha é empurrado 1 passo para trás. Se for do tipo Fada, vai para o banco."),
        m("Gunk Shot", "WHITE", 32, damage=60,
          desc="Envenena um Pokémon adversário a até 2 passos. Anula o efeito de uma das placas em uso desse Pokémon (a placa anulada conta como usada)."),
        m("Toxic Spikes", "PURPLE", 28, effect="POISON",
          desc="O oponente da batalha fica envenenado. Este Pokémon recua um passo."),
        m("Miss", "RED", 8),
    ])

add(133, "garbodor", "Garbodor", "VENENO", None, "R", 2,
    "Fedor - Pokémon adversários do tipo Grama e Fada a até 2 passos deste Pokémon têm PM-1.",
    [
        m("Acid Spray", "PURPLE", 28,
          desc="Enquanto o oponente da batalha estiver em campo, todos os ataques Azuis dele que não sejam Dodge se tornam Miss."),
        m("Gunk Shot", "WHITE", 40, damage=120,
          desc="Envenena um Pokémon adversário a até 2 passos. Anula o efeito de uma das placas em uso desse Pokémon (a placa anulada conta como usada)."),
        m("Toxic Spikes", "PURPLE", 20, effect="POISON",
          desc="O oponente da batalha fica envenenado. Este Pokémon recua um passo."),
        m("Miss", "RED", 8),
    ])

add(134, "zorua", "Zorua", "SOMBRIO", None, "R", 3,
    "Ilusão - Quando este Pokémon está no banco (ou ainda não batalhou após ir para o campo) e outro dos seus Pokémon na mesma condição é atacado, antes da batalha esse Pokémon pode trocar de lugar com este.",
    [
        m("Miss", "RED", 8),
        m("Punishment", "WHITE", 56, damage=50,
          desc="Se o oponente da batalha estiver sob efeito que aumenta dano de ataque, este ataque causa +50."),
        m("Sucker Punch", "GOLD", 32, damage=30),
    ])

add(135, "zoroark", "Zoroark", "SOMBRIO", None, "EX", 2,
    "Ilusão - Quando este Pokémon está no banco (ou ainda não batalhou após ir para o campo) e outro dos seus Pokémon na mesma condição é atacado, antes da batalha esse Pokémon pode trocar de lugar com este.",
    [
        m("Miss", "RED", 4),
        m("Crosscounter", "WHITE", 36, damage=90,
          desc="Se o oponente da batalha girar um ataque de 110 de dano ou mais, o oponente é derrotado em vez deste Pokémon."),
        m("Dodge", "BLUE", 4),
        m("Sucker Punch", "GOLD", 24, damage=50),
        m("Night Daze", "WHITE", 24, damage=110,
          desc="Na próxima batalha do oponente, os ataques Brancos dele se tornam Miss."),
    ])

# ── Geração 5 (parte 1): Solosis a Larvesta (ID-136 a ID-150) ────────────────

add(136, "solosis", "Solosis", "PSIQUICO", None, "UC", 2,
    "Cérebros Duplos - Se um Reuniclus, Duosion ou Solosis seu estiver adjacente, causa o dobro de dano.",
    [
        m("Psychic", "WHITE", 72, damage=40),
        m("Miss", "RED", 24),
    ])

add(137, "duosion", "Duosion", "PSIQUICO", None, "UC", 2,
    "Cérebros Duplos - Se um Reuniclus, Duosion ou Solosis seu estiver adjacente, causa o dobro de dano.",
    [
        m("Psychic", "WHITE", 40, damage=40),
        m("Light Screen", "PURPLE", 40, desc="(Efeito não especificado no documento original.)"),
        m("Miss", "RED", 16),
    ])

add(138, "reuniclus", "Reuniclus", "PSIQUICO", None, "R", 2,
    "Rede Psíquica - Você pode refazer giros de batalha até o número de outros Reuniclus seus em campo.",
    [
        m("Miss", "RED", 12),
        m("Brain Link", "WHITE", 32, damage=50,
          desc="Causa o dobro de dano se um Reuniclus seu estiver a até 2 passos deste Pokémon."),
        m("Hyper Beam", "WHITE", 20, damage=120,
          desc="Se o oponente da batalha for derrotado, o próximo turno será sempre do outro jogador."),
        m("Confuse Ray", "PURPLE", 24, effect="CONFUSION", desc="O oponente da batalha fica confuso."),
        m("Miss", "RED", 8),
    ])

add(139, "joltik", "Joltik", "INSETO", "ELETRICO", "UC", 3,
    "Atração Elétrica - Enquanto este Pokémon estiver em campo, seus outros Pokémon causam +1 de dano.",
    [
        m("Dodge", "BLUE", 8),
        m("Leech Life", "WHITE", 40, damage=30),
        m("Miss", "RED", 12),
        m("Electric Thread", "PURPLE", 36, effect="PARALYSIS",
          desc="O oponente da batalha fica paralisado e ganha Wait 3."),
    ])

add(140, "galvantula", "Galvantula", "INSETO", "ELETRICO", "R", 2,
    "Teia Elétrica - Pokémon adversários que passaram sobre este Pokémon usando Fly Away, Fly ou Soar ficam paralisados.",
    [
        m("Dodge", "BLUE", 8),
        m("Electro Ball", "WHITE", 44, damage=60,
          desc="Se o oponente da batalha estiver paralisado, causa o dobro de dano."),
        m("Miss", "RED", 12),
        m("Electric Thread", "PURPLE", 32, effect="PARALYSIS",
          desc="O oponente da batalha fica paralisado e ganha Wait 3."),
    ])

add(144, "axew", "Axew", "DRAGAO", None, "UC", 3, None,
    [
        m("Outrage", "WHITE", 16, damage=80, desc="Este Pokémon fica confuso."),
        m("Miss", "RED", 4),
        m("Dragon Claw", "WHITE", 52, damage=50),
        m("Miss", "RED", 4),
        m("Guillotine", "PURPLE", 20, desc="O oponente da batalha é derrotado."),
    ])

add(145, "fraxure", "Fraxure", "DRAGAO", None, "R", 2,
    "Territorialidade - Enquanto este Pokémon estiver no banco, ganha +1 PM. Pokémon adversários não podem passar por ele com MP move. Qualquer Pokémon adversário que usar MP move para ficar ao lado deste Pokémon deve atacá-lo nesse turno, se possível.",
    [
        m("Dragon Claw", "WHITE", 32, damage=100),
        m("Dual Chop", "WHITE", 32, damage=80,
          desc="Gira de novo - se sair Dual Chop novamente, causa o dobro de dano."),
        m("Miss", "RED", 4),
        m("Guillotine", "PURPLE", 24, desc="O oponente da batalha é derrotado."),
        m("Miss", "RED", 4),
    ])

add(146, "haxorus", "Haxorus", "DRAGAO", None, "EX", 2,
    "Machado Duplo - Quando este Pokémon sofreria um efeito de ataque Branco do oponente que o derrotaria, o oponente é derrotado em vez disso.",
    [
        m("Outrage", "WHITE", 20, damage=160, desc="Este Pokémon fica confuso."),
        m("Dragon Claw", "WHITE", 40, damage=150),
        m("Miss", "RED", 4),
        m("Guillotine", "PURPLE", 28,
          desc="O oponente da batalha é derrotado. Se estiver congelado, é excluído do duelo."),
        m("Miss", "RED", 4),
    ])

add(141, "litwick", "Litwick", "FANTASMA", "FOGO", "UC", 1,
    "Vela Flutuante - Quando este Pokémon se move do banco, só pode ir para um ponto a 1 passo de um ponto de entrada (seu turno termina). Seus Pokémon em campo podem passar por este usando MP move. Se este Pokémon queimar o oponente, pode evoluir.",
    [
        m("Miss", "RED", 8),
        m("Will-O-Wisp", "PURPLE", 56, effect="BURN", desc="O oponente da batalha fica queimado."),
        m("Miss", "RED", 8),
        m("Ember", "WHITE", 24, damage=20),
    ])

add(142, "lampent", "Lampent", "FANTASMA", "FOGO", "UC", 2,
    "Luz Sombria - Pode fazer MP move através de outros Pokémon em campo. Anexa um marcador Branded a Pokémon queimados pelos quais este Pokémon passar.",
    [
        m("Miss", "RED", 4),
        m("Branded", "PURPLE", 20,
          desc="Se o oponente da batalha estiver queimado, anexa um marcador Branded a ele. Efeitos de Habilidades de Pokémon Branded que aumentam ou diminuem dano de ataque são anulados."),
        m("Miss", "RED", 4),
        m("Will-O-Wisp", "PURPLE", 28, effect="BURN", desc="O oponente da batalha fica queimado."),
        m("Miss", "RED", 4),
        m("Flamethrower", "WHITE", 36, damage=50,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica queimado."),
    ])

add(143, "chandelure", "Chandelure", "FANTASMA", "FOGO", "EX", 2,
    "Queimador de Almas - Se este Pokémon for derrotado, derrota Pokémon adversários com marcador Branded. Pode fazer MP move através de outros Pokémon em campo.",
    [
        m("Miss", "RED", 4),
        m("Dark Rite", "BLUE", 20, desc="Derrota um Pokémon que tenha um marcador Branded anexado."),
        m("Branded", "PURPLE", 24,
          desc="Se o oponente da batalha estiver queimado, anexa um marcador Branded a ele. Efeitos de Habilidades de Pokémon Branded que aumentam ou diminuem dano de ataque são anulados."),
        m("Miss", "RED", 4),
        m("Inferno", "WHITE", 44, damage=130, desc="O oponente da batalha fica queimado."),
    ])

add(147, "deino", "Deino", "SOMBRIO", "DRAGAO", "C", 2, None,
    [
        m("Miss", "RED", 16),
        m("Bite", "WHITE", 48, damage=40),
        m("Miss", "RED", 12),
        m("Protect", "BLUE", 20, desc="Este Pokémon ganha Wait."),
    ])

add(148, "zweilous", "Zweilous", "SOMBRIO", "DRAGAO", "UC", 2,
    "Ataque em Banda - Este Pokémon causa +10 de dano para cada Hydreigon, Zweilous ou Deino em campo. Se este Pokémon tiver evoluído, ganha +1 PM.",
    [
        m("Roar", "PURPLE", 36,
          desc="O oponente da batalha é empurrado 1 passo para trás (o oponente escolhe o ponto) e ganha Wait."),
        m("Dodge", "BLUE", 12),
        m("Bite", "WHITE", 36, damage=60),
        m("Miss", "RED", 12),
    ])

add(149, "hydreigon", "Hydreigon", "SOMBRIO", "DRAGAO", "R", 1,
    "Ataque em Banda - Este Pokémon causa +10 de dano para cada Hydreigon, Zweilous ou Deino em campo. Se este Pokémon tiver evoluído, ganha +1 PM.",
    [
        m("Draco Meteor", "PURPLE", 28,
          desc="Outro Pokémon em campo gira - se sair Miss ou ataque Branco de 70 ou mais, é derrotado."),
        m("Miss", "RED", 4),
        m("Flamethrower", "WHITE", 28, damage=80,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica queimado."),
        m("Miss", "RED", 4),
    ])

add(150, "larvesta", "Larvesta", "INSETO", "FOGO", "UC", 2,
    "Evolução Flamejante - Quando este Pokémon desmaia em batalha, pode evoluir sem ir para o P.C.",
    [
        m("Miss", "RED", 8),
        m("Sting Wrap", "PURPLE", 24, desc="Anexa um marcador MP-1 ao oponente da batalha."),
        m("U-turn", "PURPLE", 24, desc="Este Pokémon troca de lugar com um Pokémon do seu banco ou P.C."),
        m("Flare Blitz", "WHITE", 32, damage=70,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica queimado."),
        m("Miss", "RED", 8),
    ])

# ── Geração 5 (parte 2) e Lendários finais: Volcarona a Genesect (ID-151 a ID-162) ──

add(151, "volcarona", "Volcarona", "INSETO", "FOGO", "R", 2,
    "Escamas de Fogo - Este Pokémon pode fazer MP move sobre Pokémon não-Voadores em campo. Se este Pokémon estiver evoluído, os Pokémon que batalharem com ele ficam queimados após a batalha.",
    [
        m("Miss", "RED", 8),
        m("Quiver Dance", "WHITE", 24, damage=0,
          desc="Gira de novo até sair um ataque diferente de Quiver Dance, então dobra o dano dele, ou este Pokémon ganha +1 estrela."),
        m("Fiery Dance", "PURPLE", 28,
          desc="Anexa um marcador MP-2 ao oponente da batalha. Qualquer Pokémon adversário com marcador que reduza PM fica Queimado."),
        m("Fire Blast", "WHITE", 32, damage=90),
        m("Miss", "RED", 4),
    ])

add(152, "cobalion", "Cobalion", "ACO", "LUTADOR", "EX", 2,
    "Coração de Ferro - Este Pokémon e seus Pokémon adjacentes a ele não são derrotados por Ataques Dourados dos seus oponentes de batalha.",
    [
        m("Miss", "RED", 4),
        m("Dodge", "BLUE", 12),
        m("Sword of Justice", "WHITE", 40, damage=101),
        m("Dodge", "BLUE", 12),
        m("Miss", "RED", 4),
        m("Quick Attack", "GOLD", 24, damage=60),
    ])

add(153, "terrakion", "Terrakion", "ROCHA", "LUTADOR", "EX", 2,
    "Atordoar - Todos os ataques Azuis dos oponentes de batalha deste Pokémon se tornam Miss.",
    [
        m("Miss", "RED", 8),
        m("Protect", "BLUE", 8, desc="Este Pokémon ganha Wait."),
        m("Sword of Justice", "WHITE", 40, damage=101),
        m("Miss", "RED", 4),
        m("Stone Edge", "WHITE", 28, damage=130),
        m("Protect", "BLUE", 8, desc="Este Pokémon ganha Wait."),
    ])

add(154, "virizion", "Virizion", "GRAMA", "LUTADOR", "EX", 2,
    "Trabalho de Passos - Este Pokémon pode se mover sobre Pokémon não-Voadores em campo usando MP moves.",
    [
        m("Grass Knot", "WHITE", 24, damage=40,
          desc="Se o ataque do oponente for 120 de dano ou mais, o oponente é derrotado em vez deste Pokémon."),
        m("Sword of Justice", "WHITE", 36, damage=101),
        m("Typhoon Slash", "PURPLE", 28,
          desc="Este Pokémon pode trocar de lugar com o oponente da batalha."),
        m("Miss", "RED", 8),
    ])

add(155, "reshiram", "Reshiram", "DRAGAO", "FOGO", "EX", 2,
    "Pedra da Luz - Este Pokémon ganha Wait 9 no início do duelo.",
    [
        m("Miss", "RED", 8),
        m("Fly", "PURPLE", 12,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Blue Flare", "WHITE", 36, damage=140, desc="O oponente da batalha fica queimado."),
        m("Fly", "PURPLE", 12,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Dodge", "BLUE", 4),
        m("Fusion Flare", "WHITE", 24, damage=90,
          desc="Se você tiver um Pokémon com Fusion Bolt em campo e este não estiver afetado por condição especial, exclui do duelo os Pokémon derrotados por este ataque."),
    ])

add(156, "zekrom", "Zekrom", "DRAGAO", "ELETRICO", "EX", 2,
    "Pedra das Trevas - Este Pokémon ganha Wait 9 no início do duelo.",
    [
        m("Miss", "RED", 8),
        m("Fly", "PURPLE", 12,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Bolt Strike", "WHITE", 36, damage=130, desc="O oponente da batalha fica paralisado."),
        m("Fly", "PURPLE", 12,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Dodge", "BLUE", 4),
        m("Fusion Bolt", "WHITE", 24, damage=90,
          desc="Se você tiver um Pokémon com Fusion Flare em campo e este não estiver afetado por condição especial, exclui do duelo os Pokémon derrotados por este ataque."),
    ])

add(157, "kyurem", "Kyurem", "DRAGAO", "GELO", "EX", 2,
    "Geada Eterna - Se não estiver afetado por condição especial, a condição Congelado de Pokémon a até 2 passos deste não pode ser removida por troca (tag).",
    [
        m("Miss", "RED", 4),
        m("Fly", "PURPLE", 12,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Ice Beam", "WHITE", 40, damage=110,
          desc="Se este Pokémon for derrotado, o oponente da batalha fica congelado."),
        m("Outrage", "WHITE", 12, damage=180, desc="Este Pokémon fica confuso."),
        m("Miss", "RED", 4),
        m("Glaciate", "PURPLE", 24,
          desc="Todos os Pokémon adversários em campo que não sejam Fogo, Água, Gelo ou Aço giram. Anexa marcadores MP-1 aos que sortearem ataques Brancos."),
    ])

add(160, "kyurem-white", "White Kyurem", "DRAGAO", "GELO", "UX", 3,
    "Turbo Chama - Esta forma só pode ser definida no baralho. Efeitos de Habilidades do oponente que aumentam ou diminuem dano de ataque são anulados. Pode fazer MP move sobre outros Pokémon. Quando derrotado, pode mudar para a forma Kyurem sem ir ao P.C.; se houver Reshiram excluídos do duelo, move um deles para o banco.",
    [
        m("Miss", "RED", 4),
        m("Draco Meteor", "PURPLE", 16,
          desc="Outro Pokémon em campo gira - se sair Miss ou ataque Branco de 70 ou mais, é derrotado."),
        m("Ice Burn", "WHITE", 32, damage=140,
          desc="O oponente da batalha fica queimado. Se o oponente tiver Wait, ele é derrotado em vez deste Pokémon."),
        m("Draco Meteor", "PURPLE", 16,
          desc="Outro Pokémon em campo gira - se sair Miss ou ataque Branco de 70 ou mais, é derrotado."),
        m("Dodge", "BLUE", 4),
        m("Fusion Flare", "WHITE", 24, damage=100,
          desc="Se não afetado por condição especial e algum dos seus Pokémon em campo tiver Fusion Bolt, exclui do duelo os Pokémon derrotados por este ataque."),
    ])

add(159, "kyurem-black", "Black Kyurem", "DRAGAO", "GELO", "UX", 3,
    "Teravoltagem - Esta forma só pode ser definida no baralho. Efeitos de Habilidades do oponente que aumentam ou diminuem dano de ataque são anulados. Pode fazer MP move sobre outros Pokémon. Quando derrotado, pode mudar para a forma Kyurem sem ir ao P.C.; se houver Zekrom excluídos do duelo, move um deles para o banco.",
    [
        m("Miss", "RED", 4),
        m("Fly", "PURPLE", 16,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Freeze Shock", "WHITE", 32, damage=140,
          desc="O oponente da batalha fica paralisado. Se o oponente tiver Wait, ele é derrotado em vez deste Pokémon."),
        m("Fly", "PURPLE", 16,
          desc="Este Pokémon salta sobre o oponente da batalha e cai 1-2 passos além."),
        m("Dodge", "BLUE", 4),
        m("Fusion Bolt", "WHITE", 24, damage=100,
          desc="Se não afetado por condição especial e algum dos seus Pokémon em campo tiver Fusion Flare, exclui do duelo os Pokémon derrotados por este ataque."),
    ])

add(158, "keldeo", "Keldeo", "AGUA", "LUTADOR", "EX", 3,
    "Justiceiro - Este Pokémon causa +10 de dano para cada Pokémon adversário do tipo Sombrio em campo. Quando derrotado em batalha, pode mudar para a forma Keldeo Resolute sem ir ao P.C.",
    [
        m("Water Jump", "BLUE", 8,
          desc="Este Pokémon pode se mover para um ponto a 2 passos. Se fizer isso, ganha Wait."),
        m("Sacred Sword", "WHITE", 36, damage=71),
        m("Hydro Kick", "WHITE", 32, damage=40,
          desc="Coloca o Pokémon adversário no banco (exceto se já desmaiou). O oponente da batalha ganha Wait em seguida."),
        m("Miss", "RED", 12),
        m("Dodge", "BLUE", 8),
    ])

add(161, "keldeo-resolute", "Keldeo Resolute Form", "AGUA", "LUTADOR", "UX", 3,
    "Espadachim Sagrado - Válido apenas no seu turno. Enquanto este Pokémon estiver em campo, Sword of Justice dos seus Pokémon causa +30 de dano. O oponente de batalha desses Pokémon não pode ser movido por efeitos que não sejam de ataque desses Pokémon até o fim da batalha.",
    [
        m("Water Jump", "BLUE", 12,
          desc="Este Pokémon pode se mover para um ponto a 2 passos. Se fizer isso, ganha Wait."),
        m("Miss", "RED", 8),
        m("Secret Sword", "PURPLE", 24,
          desc="Gira para um Pokémon adversário a até 2 passos. Se sair Miss ou ataque Branco de até 100 de dano, ele é derrotado."),
        m("Water Jump", "BLUE", 12,
          desc="Este Pokémon pode se mover para um ponto a 2 passos. Se fizer isso, ganha Wait."),
        m("Aqua Jet", "GOLD", 36, damage=70),
        m("Miss", "RED", 8),
    ])

add(162, "genesect", "Genesect", "INSETO", "ACO", "EX", 3,
    "Jato Flutuante - Pode fazer MP move sobre seus Pokémon em campo. Se alguma das suas placas Drive tiver sido usada, quando este Pokémon for derrotado, uma delas volta a ficar usável (exceto uma ainda em uso naquele turno).",
    [
        m("Dodge", "BLUE", 8),
        m("Miss", "RED", 8),
        m("Techno Blast", "WHITE", 40, damage=50),
        m("Miss", "RED", 8),
        m("Dodge", "BLUE", 8),
        m("Techno Charge", "PURPLE", 24,
          desc="Este Pokémon deve esperar (Wait). Em troca, recebe um bônus de +50 de dano até a próxima vez que cair em Techno Blast."),
    ])

# === BATCH_MARKER ===

# ═══════════════════════════════════════════════════════════════════════════
# Validação + Saída
# ═══════════════════════════════════════════════════════════════════════════
if __name__ == "__main__":
    import json
    import sys

    seen_ids = set()
    seen_dex = set()
    for p in POKEMON:
        if p["id"] in seen_ids:
            print(f"AVISO: id duplicado: {p['id']}", file=sys.stderr)
        seen_ids.add(p["id"])
        if p["dexId"] in seen_dex:
            print(f"AVISO: dexId duplicado: {p['dexId']} ({p['name']})", file=sys.stderr)
        seen_dex.add(p["dexId"])

    print(f"Total de Pokémon: {len(POKEMON)}")

    off_totals = [(p["dexId"], p["name"], p["wheelTotal"]) for p in POKEMON if p["wheelTotal"] != 96]
    if off_totals:
        print(f"\n{len(off_totals)} Pokémon com soma de roleta != 96 (dados originais do PDF, mantidos como estão):")
        for dex, name, tot in off_totals:
            print(f"  ID-{dex} {name}: soma = {tot}")

    with open("pokemon-catalog.json", "w", encoding="utf-8") as f:
        json.dump(POKEMON, f, ensure_ascii=False, indent=2)

    print("pokemon-catalog.json gerado com sucesso.")
