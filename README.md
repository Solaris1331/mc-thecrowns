# The Crowns

The Crowns is a Minecraft equipment and progression mod under active 1.0.0 beta development.

## Supported targets

- Forge 1.20.1
- NeoForge 1.21
- NeoForge 1.21.1

Each target is an independent platform project under `platforms/`. Shared behavior must remain functionally aligned, while loader- and version-specific code stays local to its platform.

## Current status

`platforms/forge-1.20.1` contains the `thecrowns` 1.0.0-beta.1 baseline. NeoForge targets will be added after this Forge baseline is verified.

The former `glitchedcrown` Forge 1.20.1 1.5.3 source and beta history are preserved in `docs/legacy/forge-1.20.1/` and in Git tag `forge-1.20.1-1.5.3`.

## Build

Run the Forge 1.20.1 baseline from the repository root:

```powershell
.\gradlew.bat :platforms:forge-1.20.1:clean :platforms:forge-1.20.1:build
```

Release JARs begin with The Crowns 1.0.0; legacy 1.5.3 binaries are intentionally not published in this repository.
