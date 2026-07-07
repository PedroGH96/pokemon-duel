import json

with open("/home/claude/project/pokemon-catalog.json") as f:
    catalog = json.load(f)

out = []
for p in catalog:
    moves = []
    for mv in p["moves"]:
        moves.append({
            "name": mv["name"],
            "color": mv["color"],
            "percentage": mv["size"],
            "damage": mv["damage"],
            "statusEffect": mv["effect"],
            "statusTurns": mv["turns"],
            "description": mv["description"],
        })
    out.append({
        "id": p["id"],
        "dexId": p["dexId"],
        "name": p["name"],
        "type": p["type"],
        "type2": p["type2"],
        "rarity": p["rarity"],
        "pm": p["pm"],
        "specialAbility": p["specialAbility"],
        "spriteFile": p["id"] + ".png",
        "moves": moves,
    })

with open("/home/claude/project/backend/src/main/resources/data/pokemon-catalog.json", "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=2)

print(f"Exportado {len(out)} Pokémon para resources/data/pokemon-catalog.json")
