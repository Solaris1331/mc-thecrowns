# The Crowns

The Crowns is a Minecraft equipment and progression mod under active 1.0.0 beta development.

## Supported targets

- Forge 1.20.1
- NeoForge 1.21
- NeoForge 1.21.1

Each target is an independent platform project under `platforms/`. Shared behavior must remain functionally aligned, while loader- and version-specific code stays local to its platform.

## Current status

`platforms/forge-1.20.1` is the **1.0.4** baseline. Java 21 NeoForge ports are developed in `platforms/neoforge-1.21` and `platforms/neoforge-1.21.1` from that baseline.

The former `glitchedcrown` Forge 1.20.1 1.5.3 source and beta history are preserved in `docs/legacy/forge-1.20.1/` and in Git tag `forge-1.20.1-1.5.3`.

The reproducible 1.0.4 artifacts and a Korean current-state brief for reviews and general-chat context are kept in [`releases/1.0.4/`](releases/1.0.4/). The source remains under `platforms/`.

## Build

Run the Forge 1.20.1 baseline from the repository root:

```powershell
.\gradlew.bat :platforms:forge-1.20.1:clean :platforms:forge-1.20.1:build
```

Release JARs begin with The Crowns 1.0.0; legacy 1.5.3 binaries are intentionally not published in this repository.
