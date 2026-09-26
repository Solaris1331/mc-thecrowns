# The Crowns development guide

## Project line

- Mod name: The Crowns
- Mod ID: `thecrowns`
- Current line: `1.0.4`
- Supported targets: Forge 1.20.1, NeoForge 1.21, NeoForge 1.21.1
- Release JARs begin with 1.0.0. Do not publish legacy 1.5.3 binaries from this repository.

## Repository layout

- `platforms/forge-1.20.1`: Forge 1.20.1 implementation, Java 17.
- `platforms/neoforge-1.21`: reserved for the NeoForge 1.21 implementation, Java 21.
- `platforms/neoforge-1.21.1`: reserved for the NeoForge 1.21.1 implementation, Java 21.
- `docs/legacy/forge-1.20.1`: preserved `glitchedcrown` 1.5.3 beta history and documentation.

## Compatibility and safety

- `thecrowns` is a clean namespace break from `glitchedcrown`; never promise old-world item or config compatibility without an explicit migration implementation.
- Keep the three targets functionally aligned. Document intentional platform differences in the changelog.
- Keep client-only references out of shared/common server paths and verify dedicated-server startup for every platform change.
- Preserve the legacy feature contract while porting unless a newer approved requirement replaces it.
- Do not delete user worlds, build caches, or legacy source history.

## Verification

- Forge 1.20.1: `./gradlew.bat :platforms:forge-1.20.1:clean :platforms:forge-1.20.1:build`
- Parse resource JSON, compare Korean and English translation keys, and inspect the built JAR before publishing.
- Distinguish clean-build/static verification from real Minecraft integration testing.
