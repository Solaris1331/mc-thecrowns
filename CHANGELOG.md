# Changelog

## Unreleased

- Reduced server-side tick overhead without changing Crown stats, ranges, or ability durations: entities with no active Crown runtime state now skip the Advanced Crown living-tick work.
- Changed Time Warp to search every two ticks while keeping slow/stop states active until the next scan.
- Changed Cursed Crown inventory conversion and Darkened Crown Warden scans to ten-tick intervals.
- Combined the two Glitched Crown passive nearby-entity broad-phase searches into one shared query.
- Applied the same runtime scheduling changes to the NeoForge 1.21.1 source.

## 1.0.4

- Added the Shadow Crown and Abyssal Crown, including Crown Builder recipes, localized tooltips, Curios support, Lorebook entries, and dedicated item/armor textures.
- Added the Shadow and Abyssal fusion paths for the Glitched Crown, with selective inherited abilities.
- Fixed generic tooltip data so ordinary identical items do not split into separate inventory stacks.
- Added compatibility recognition for Enhanced AI `TeleportToTarget` forced teleportation while avoiding broad environment-movement interception.
- Corrected Crown Lorebook entries that requested missing tooltip translation keys.
- Moved the Iron Guardian's final-damage cap from a `LivingEntity` Mixin redirect to the loader's final-damage event, allowing RevelationFix to load alongside The Crowns.
- Archived the reproducible Forge 1.20.1 and NeoForge 1.21.1 JAR snapshots under `releases/1.0.4/`.

## 1.0.3

- Changed the Crown of Light recipe to use an amethyst shard instead of an amethyst bud.
- Added a dedicated Curios `crown` slot. It provides one slot by default and can be set from 1 to 16 with the server config's `curios.crownSlotCount` option.
- Crowns remain equippable in the existing Curios head/hat slots and the vanilla helmet slot.
- Added recipes for suspicious sand and suspicious gravel: surround sand or gravel with eight emeralds.
- Fixed the Angelic Crown's passive elytra-style flight so the client and server both accept flight without an equipped elytra.
- Creative-mode players now bypass the Glitched Crown advancement gate.
- Renamed the legacy `/gt` command root to `/crowns`.
- Added `/crown <target> cooldown reset [crown|all]`; omitting the crown resets all Crown cooldowns. `/crown cooldown reset [crown|all]` targets the executing player.
- Added `/crowns cdr` as a shortcut that resets all Crown cooldowns for the executing player.
- Added `/crown glitchadvancegate [true|false]` to view or change the Glitched Crown advancement gate at runtime. The default remains `true`.
- Fixed Crown Builder recipe rendering in REI and EMI.

## 1.0.2-beta.1 — NeoForge porting baseline

- Renamed the completed Forge 1.20.1 balance and tooltip revision from the provisional `0.0.2` baseline to `1.0.2-beta.1`, without changing Forge 1.20.1 gameplay behavior.
- Prepared independent NeoForge 1.21 and NeoForge 1.21.1 Gradle modules with Java 21, loader metadata, client/server/data run configurations, Gradle 8.14, and pinned official MDK coordinates.
- Recorded the migration work required before either NeoForge target can be called feature-complete.

## 1.0.0-beta.1

- Began the new `thecrowns` identifier line from the preserved Forge 1.20.1 1.5.3 baseline.
- Moved the Forge 1.20.1 project into `platforms/forge-1.20.1`.
- Reserved `platforms/neoforge-1.21` and `platforms/neoforge-1.21.1` as the next port targets.
- Reset the 1.20.1 network protocol to `1`; it is not compatible with the legacy `glitchedcrown` protocol `12`.

## Legacy beta history

The former Forge 1.20.1 `glitchedcrown` 1.0.7–1.5.3 patch notes and project documents are preserved under `docs/legacy/forge-1.20.1/` and by the Git tag `forge-1.20.1-1.5.3`.
