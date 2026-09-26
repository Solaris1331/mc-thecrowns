# NeoForge 1.21.1 port plan

This module is the Java 21 NeoForge 1.21.1 build target for The Crowns **1.0.4** baseline.

## Pinned workspace

- Minecraft 1.21.1
- NeoForge 21.1.251
- ModDevGradle 2.0.147
- Java 21

## Migration order

1. Complete and verify the NeoForge 1.21 port first, then migrate its compatible implementation to 1.21.1.
2. Apply the official 1.21 to 1.21.1 migration changes to registries, networking, data components, entity attributes, and client rendering.
3. Revalidate all mixins against 1.21.1 mappings and run a dedicated-server startup before client testing.
4. Update optional Curios and recipe-viewer integrations to compatible 1.21.1 releases.
5. Run a full 1.0.4 parity matrix: attributes, each active ability, death handling, Glitched permanent-health loss, recipes, advancement gates, and bilingual tooltips.

Do not publish a JAR from this module until it passes the same server and client verification as Forge 1.20.1.

## Current implementation status

- The module is wired to the official 1.21.1 ModDevGradle toolchain and Java 21, including a
  1.21.1 Curios dependency.
- Core registration, configuration, payload registration, armor-material representation, block-entity
  persistence, modern menu-screen registration, recipe inputs, and Builder persistence have been migrated.
- Crown runtime/fusion/cursed/integrity data now use custom data components. The Builder and all special
  crafting/smithing recipes use 1.21.1 codecs and stream codecs while retaining their existing data fields.
- NeoForge 1.21.1 runs with official runtime names, so the legacy Forge SRG refmap annotation processor is
  deliberately not used. The mixin config is registered through `neoforge.mods.toml` without a refmap.
- Dedicated-server startup passed on 2026-09-25 with The Crowns 1.0.3 and Curios 9.5.1: Mixin bootstrap,
  recipe/advancement loading, Curios slot registration, world preparation, and `Done` were all reached.
- No release JAR has been produced. Remaining verification is feature-level parity, client startup, and
  optional JEI/EMI/REI runtime tests.
