package com.pokemonduel.service;

import com.pokemonduel.model.Move;
import com.pokemonduel.model.Pokemon;
import com.pokemonduel.model.enums.MoveColor;
import com.pokemonduel.model.enums.PokemonType;
import com.pokemonduel.model.enums.Rarity;
import com.pokemonduel.model.enums.StatusEffect;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Catálogo central de todos os Pokémon disponíveis no jogo.
 *
 * Carregado em memória na inicialização do servidor.
 * Em produção, estes dados viriam do figuras.json (Jackson).
 *
 * Regra: a soma de percentage de todos os moves de um Pokémon = 100.
 *
 * As 6 figuras comuns (C) iniciais para todo jogador:
 *   pikachu, eevee, machamp, clefairy, charizard, blastoise
 */
@Service
public class PokemonCatalogService {

    // IDs das figuras que todo jogador recebe ao criar conta
    public static final List<String> STARTER_IDS = List.of(
            "pikachu", "eevee", "machamp", "clefairy", "charizard", "blastoise"
    );

    private final Map<String, Pokemon> catalog = new LinkedHashMap<>();

    public PokemonCatalogService() {
        loadAll();
    }

    private void loadAll() {
        add(buildCharizard());
        add(buildBlastoise());
        add(buildVenusaur());
        add(buildPikachu());
        add(buildMewtwo());
        add(buildEevee());
        add(buildGengar());
        add(buildSnorlax());
        add(buildLapras());
        add(buildMachamp());
        add(buildDragonite());
        add(buildClefairy());
        validateAll();
    }

    private void add(Pokemon p) {
        catalog.put(p.getId(), p);
    }

    // ── Definições dos Pokémon ─────────────────────────────────────────────────

    private Pokemon buildCharizard() {
        return new Pokemon("charizard", "Charizard", PokemonType.FOGO, Rarity.R, 2)
            .addMove(Move.miss(8))
            .addMove(Move.damage("Ember",        MoveColor.WHITE,  20, 20, "Ataque de fogo básico, 20 de dano."))
            .addMove(Move.damage("Slash",         MoveColor.WHITE,  20, 30, "Arranhão poderoso, 30 de dano."))
            .addMove(Move.dodge ("Fly",                            12,      "Voa alto — vence tudo, sem dano direto."))
            .addMove(Move.status ("Fire Spin",    20, StatusEffect.IMMOBILIZED, 2, "Aprisiona o oponente por 2 turnos."))
            .addMove(Move.damage("Flamethrower",  MoveColor.GOLD,  20, 70, "Lança-chamas devastador, 70 de dano."));
    }

    private Pokemon buildBlastoise() {
        return new Pokemon("blastoise", "Blastoise", PokemonType.AGUA, Rarity.R, 2)
            .addMove(Move.miss(8))
            .addMove(Move.damage("Bite",       MoveColor.WHITE,  20, 20, "Mordida básica, 20 de dano."))
            .addMove(Move.damage("Water Gun",  MoveColor.WHITE,  20, 40, "Jato de água, 40 de dano."))
            .addMove(Move.dodge ("Shell Smash",               12,      "Proteção total — cancela a batalha."))
            .addMove(Move.damage("Hydro Pump", MoveColor.GOLD,  20, 80, "Canhão de água, 80 de dano."))
            .addMove(Move.status ("Icy Wind",  20, StatusEffect.REDUCED_PM, 1, "Reduz PM do oponente por 1 turno."));
    }

    private Pokemon buildVenusaur() {
        return new Pokemon("venusaur", "Venusaur", PokemonType.GRAMA, Rarity.R, 2)
            .addMove(Move.miss(8))
            .addMove(Move.damage("Vine Whip",  MoveColor.WHITE, 25, 30, "Chicotada com cipó, 30 de dano."))
            .addMove(Move.damage("Razor Leaf", MoveColor.WHITE, 25, 50, "Folhas cortantes, 50 de dano."))
            .addMove(Move.status ("Sleep Powder", 20, StatusEffect.SLEEP, 1, "Oponente pula o próximo turno dormindo."))
            .addMove(Move.damage("Solar Beam",  MoveColor.GOLD, 22, 90, "Raio solar, 90 de dano."));
        // soma: 8+25+25+20+22 = 100 ✓
    }

    private Pokemon buildPikachu() {
        return new Pokemon("pikachu", "Pikachu", PokemonType.ELETRICO, Rarity.C, 3)
            .addMove(Move.miss(8))
            .addMove(Move.damage("Quick Attack",  MoveColor.WHITE,  25, 10, "Ataque rápido, 10 de dano."))
            .addMove(Move.damage("Thundershock",  MoveColor.WHITE,  25, 30, "Raio leve, 30 de dano."))
            .addMove(Move.status ("Thunder Wave", 22, StatusEffect.PARALYSIS, 1, "Paralisa o oponente — não pode agir no próximo turno."))
            .addMove(Move.damage("Thunder",        MoveColor.GOLD,  20, 50, "Relâmpago poderoso, 50 de dano."));
        // soma: 8+25+25+22+20 = 100 ✓
    }

    private Pokemon buildMewtwo() {
        return new Pokemon("mewtwo", "Mewtwo", PokemonType.PSIQUICO, Rarity.EX, 3)
            .addMove(Move.miss(4))
            .addMove(Move.damage("Swift",      MoveColor.WHITE,   15, 30, "Ataque certeiro, 30 de dano."))
            .addMove(Move.status ("Psybeam",   20, StatusEffect.CONFUSION, 2, "Confunde — movimento aleatório no tabuleiro."))
            .addMove(Move.dodge  ("Barrier",                      15,     "Barreira mental — cancela a batalha."))
            .addMove(Move.damage("Psystrike",  MoveColor.GOLD,    26, 100,"Ataque psíquico máximo, 100 de dano."))
            .addMove(Move.status ("Amnesia",   20, StatusEffect.PARALYSIS, 1, "Bloqueia o próximo ataque do oponente."));
        // soma: 4+15+20+15+26+20 = 100 ✓
    }

    private Pokemon buildEevee() {
        return new Pokemon("eevee", "Eevee", PokemonType.NORMAL, Rarity.C, 2)
            .addMove(Move.miss(12))
            .addMove(Move.damage("Tackle",       MoveColor.WHITE, 30, 10, "Investida simples, 10 de dano."))
            .addMove(Move.status ("Tail Whip",   20, StatusEffect.REDUCED_PM, 1, "Baixa guarda — reduz PM do oponente."))
            .addMove(Move.damage("Quick Attack", MoveColor.WHITE, 38, 20, "Ataque rápido, 20 de dano."));
        // soma: 12+30+20+38 = 100 ✓
    }

    private Pokemon buildGengar() {
        return new Pokemon("gengar", "Gengar", PokemonType.FANTASMA, Rarity.UC, 3)
            .addMove(Move.miss(6))
            .addMove(Move.damage("Lick",          MoveColor.WHITE,   15, 10, "Lambida gelada, 10 de dano."))
            .addMove(Move.damage("Shadow Ball",   MoveColor.GOLD,    25, 60, "Bola de sombra, 60 de dano."))
            .addMove(Move.status ("Hypnosis",     25, StatusEffect.SLEEP, 2, "Dorme o oponente por 2 turnos."))
            .addMove(Move.dodge  ("Curse",                           14,     "Maldição — cancela batalha, aplica 10 de dano/turno por 3 turnos."))
            .addMove(Move.status ("Destiny Bond", 15, StatusEffect.DESTINY_BOND, 1, "Se for K.O., o oponente também vai ao P.C."));
        // soma: 6+15+25+25+14+15 = 100 ✓
    }

    private Pokemon buildSnorlax() {
        return new Pokemon("snorlax", "Snorlax", PokemonType.NORMAL, Rarity.UC, 1)
            .addMove(Move.miss(6))
            .addMove(Move.damage("Tackle",      MoveColor.WHITE, 20, 20, "Investida pesada, 20 de dano."))
            .addMove(Move.damage("Body Slam",   MoveColor.WHITE, 20, 50, "Esmagamento, 50 de dano."))
            .addMove(Move.status ("Snore",      18, StatusEffect.SLEEP, 1, "Ronco — oponente dorme 1 turno."))
            .addMove(Move.damage("Hyper Beam",  MoveColor.GOLD,  36,120, "Hiper raio, 120 de dano. Maior do jogo."));
        // soma: 6+20+20+18+36 = 100 ✓
    }

    private Pokemon buildLapras() {
        return new Pokemon("lapras", "Lapras", PokemonType.GELO, Rarity.UC, 2)
            .addMove(Move.miss(8))
            .addMove(Move.damage("Water Gun",    MoveColor.WHITE, 25, 30, "Jato de água, 30 de dano."))
            .addMove(Move.damage("Ice Beam",     MoveColor.WHITE, 20, 50, "Raio de gelo, 50 de dano."))
            .addMove(Move.status ("Confuse Ray", 20, StatusEffect.CONFUSION, 1, "Confunde o oponente."))
            .addMove(Move.damage("Blizzard",     MoveColor.GOLD,  27, 80, "Nevasca, 80 de dano."));
        // soma: 8+25+20+20+27 = 100 ✓
    }

    private Pokemon buildMachamp() {
        return new Pokemon("machamp", "Machamp", PokemonType.LUTADOR, Rarity.C, 2)
            .addMove(Move.miss(8))
            .addMove(Move.damage("Karate Chop",   MoveColor.WHITE, 20, 30, "Golpe karatê, 30 de dano."))
            .addMove(Move.damage("Low Kick",      MoveColor.WHITE, 20, 40, "Chute baixo, 40 de dano."))
            .addMove(Move.dodge  ("Seismic Toss",                  12,    "Cancela batalha e empurra oponente 1 casa."))
            .addMove(Move.status ("No Guard",     15, StatusEffect.REDUCED_PM, 1, "Próximo ataque do oponente cai sempre em Branco."))
            .addMove(Move.damage("Dynamic Punch", MoveColor.GOLD,  25, 80, "Soco dinâmico, 80 de dano."));
        // soma: 8+20+20+12+15+25 = 100 ✓
    }

    private Pokemon buildDragonite() {
        return new Pokemon("dragonite", "Dragonite", PokemonType.DRAGAO, Rarity.EX, 3)
            .addMove(Move.miss(4))
            .addMove(Move.damage("Dragon Claw",    MoveColor.WHITE,  18, 50, "Garra de dragão, 50 de dano."))
            .addMove(Move.status ("Dragon Dance",  18, StatusEffect.REDUCED_PM, -1, "+1 PM no próximo turno (bônus)."))
            .addMove(Move.dodge  ("Extremespeed",                    20,       "Move 2 casas extras sem batalha."))
            .addMove(Move.damage("Hyper Beam",     MoveColor.GOLD,   40, 90,  "Hiper raio dragão, 90 de dano."));
        // soma: 4+18+18+20+40 = 100 ✓
    }

    private Pokemon buildClefairy() {
        return new Pokemon("clefairy", "Clefairy", PokemonType.NORMAL, Rarity.C, 2)
            .addMove(Move.miss(10))
            .addMove(Move.damage("Pound",       MoveColor.WHITE, 30, 10, "Batida fraca, 10 de dano."))
            .addMove(Move.status ("Sing",       25, StatusEffect.SLEEP, 1, "Canta para dormir o oponente por 1 turno."))
            .addMove(Move.damage("Moonblast",   MoveColor.WHITE, 20, 30, "Explosão lunar, 30 de dano."))
            .addMove(Move.dodge  ("Metronome",                   15,    "Usa ataque aleatório de qualquer Pokémon."));
        // soma: 10+30+25+20+15 = 100 ✓
    }

    // ── Validação ─────────────────────────────────────────────────────────────

    private void validateAll() {
        for (Pokemon p : catalog.values()) {
            if (!p.isWheelValid()) {
                int sum = p.getMoves().stream().mapToInt(Move::getPercentage).sum();
                throw new IllegalStateException(
                    "Roleta inválida para " + p.getName() + ": soma = " + sum + " (esperado 100)");
            }
        }
        System.out.println("[PokemonCatalog] " + catalog.size() + " Pokémon carregados. Todas as roletas válidas.");
    }

    // ── API pública ───────────────────────────────────────────────────────────

    public Pokemon get(String id) {
        return catalog.get(id);
    }

    public List<Pokemon> getAll() {
        return new ArrayList<>(catalog.values());
    }

    public List<Pokemon> getStarters() {
        return STARTER_IDS.stream().map(catalog::get).toList();
    }

    public boolean exists(String id) {
        return catalog.containsKey(id);
    }
}
