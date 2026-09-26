# NeoForge 1.21 port plan

This module is the Java 21 NeoForge 1.21 build target for The Crowns **1.0.4** baseline.

## Pinned workspace

- Minecraft 1.21
- NeoForge 21.0.167
- NeoGradle 7.1.38
- Java 21

## Migration order

1. Port registries, creative-tab registration, networking, configuration, tags, recipes, advancements, models, sounds, and language assets.
2. Port common crown logic and all event subscriptions, replacing Forge-only APIs with their NeoForge equivalents.
3. Port mixins and validate every injection against 1.21 mappings; keep client-only mixins out of dedicated-server paths.
4. Port Curios integration only after selecting a NeoForge 1.21-compatible Curios release; keep JEI/EMI/REI integrations optional.
5. Recreate client screens, key mappings, model layers, and packet handling.
6. Build, test a dedicated server, and compare every 1.0.4 ability against Forge 1.20.1 before publishing.

Do not publish a JAR from this module until the checklist is complete.
