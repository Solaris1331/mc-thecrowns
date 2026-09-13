# The Crowns 1.5.3 — Pack Developer Guide

Target: Minecraft 1.20.1, Forge 47.4.x, Java 17, Curios 5.14.1+, network protocol 12.

## Server config

Forge creates the authoritative config at `<world>/serverconfig/glitchedcrown-server.toml`. It is not a global `.minecraft/config` file.

Existing groups control targeting, shared Glitched/Unleashed stats, progression gates, revive, removal ray, execution, nullification, movement/status protection, and Unleashed damage behavior. Version 1.5.3 adds 38 live values:

- `temporalCrown`: movement/attack/flight speed, consumable speed, out-of-combat delay, Rewind/Time Warp cooldowns, warp duration/radius/slow
- `frostCrown`: attack damage, armor/toughness, cold-creature radius/biome threshold, Frost stacks/slow/decay/freeze, bonus cold damage
- `divineCrown`: flat/percent health, armor/toughness, healing multiplier, negative-effect cleanse healing, positive-effect duration, Sanctify cooldown/radius
- `cursedCrown`: attribute penalty, looting/luck, XP multiplier, Liberation duration/cooldown/radius/heal

Tooltips and lorebook numeric text use the active config. Cursed binding, legal unequip exceptions, curse conversion safety, and death retention are integrity rules and are deliberately not configurable.

## Crown progression and curse conversion

Crown Builder tiers are enforced by the custom `glitchedcrown:crown_builder` recipe type. A curse converts only this explicit Tier I-III allowlist:

- Tier I: Burning, Ironforged, Frost
- Tier II: Bloody, Darkened, Warrior, Divine
- Tier III: Crown of Light, Dimensional, Angelic, Temporal

Glitched, Unleashed, U2, Cursed, and any future Crown are excluded unless deliberately added. Conversion removes every curse enchantment and preserves non-curse enchantments and supported stack data.

## Datapack and API integration

Entity protection tags:

- `#glitchedcrown:protected_from_crown_offense`
- `#glitchedcrown:protected_from_removal_ray`
- `#glitchedcrown:protected_from_annihilation`
- `#glitchedcrown:protected_from_execution`

Cancelable Forge events are under `com.glitchedcrown.api.event`:

- `CrownAbilityTargetEvent`
- `CrownReviveEvent`

Use these hooks for quest NPCs, scripted bosses, or custom revive rules. Ability packets only request actions; the server rechecks equipment, config, progression, cooldowns, target safety, and events.

## Diagnostics

- `/crown inspect <player>` reports the equipped Crown and key runtime state.
- `/crown diagnostics` reports whether every critical protection Mixin applied. A failed critical audit is also logged after a player joins. Treat `FAIL` as release-blocking.

## Optional compatibility

| Integration | Declared range | Tested 2026-08-19 | 1.5.3 integration |
|---|---:|---:|---|
| Curios | 5.14.1+ | 5.14.1 PASS | Required; vanilla head and Curios `head`/`hat` |
| JEI | 15.20+ | 15.20.0.110 PASS | Crown Builder category, catalysts, 12 public recipes, transfer |
| EMI | 1.1.x | 1.1.24 PASS | Native category, workstations, 12 public recipes, transfer handler |
| REI | 12.x | 12.1.785 PASS | Native category, workstations, displays, transfer handler |
| FTB Teams | Optional | Not run | Reflective same-team protection |
| Vestiges of the Present | Optional | Not run | Isolated soft compatibility |
| Ice and Fire | Optional | Not run | Registry-ID-only corpse preservation handling |
| T.O Magic / Iron's Spells / L2 Hostility / Cataclysm | Optional | Not run | Centralized forced-movement recognition |
| Corail Tombstone | Optional | Not run | Early Crown death-retention design; must be verified in a combined runtime before claiming full compatibility |

Exact viewer builds and runtime results are recorded in `PROJECT_STATE_KO.md`. Compile-only API stubs are never packaged into The Crowns JAR.

## Distribution and monetization

Personal use and inclusion of the unmodified JAR in free, non-monetized modpacks are permitted. Because project metadata remains `All Rights Reserved`, monetized packs, direct redistribution, modified binaries, forks, and commercial use require separate permission unless a broader license is published.

## Release artifact naming

The current 1.5.3 build keeps its existing artifact name for reproducibility. On the next version bump, remove the duplicated version segment by changing the archive base name so Gradle appends `project.version` exactly once, then verify the JAR manifest, expanded `mods.toml`, and compatibility matrix before distribution.
