# AZYRON CLIENT V3 — modpacks

One folder per supported Minecraft version. Each folder contains
`azyron-modpack.txt`: the exact pack definition (Minecraft version, Fabric
Loader, Fabric API build, Java version, and the performance mods to fetch).

**No binaries are checked in here.** Mod jars are downloaded from the
official Fabric Maven and the Modrinth API at install time, so everything you
get is genuine and current, and `ModernFix` is deliberately never bundled
(no Fabric builds exist for any supported version — see each manifest).

## Fetch everything (PC / maintainer machine)

Linux / macOS:

```sh
cd azyron-client
./modpack/_shared/fetch-mods.sh 26.3 ./mods
```

Windows PowerShell:

```powershell
cd azyron-client
.\modpack\_shared\fetch-mods.ps1 -Minecraft 26.3 -OutputDir .\mods
```

Replace `26.3` with `1.21.11`, `26.1` or `26.2`.

The script resolves each performance mod from **live Modrinth metadata**
(filtered to `fabric` + the exact game version) and downloads the newest
compatible build. If a mod has no Fabric build for your version, it is
skipped with a warning — the pack still works.

Then copy the AZYRON CLIENT V3 jar produced by the CI workflow
(`azyron-client-<mc>-3.0.0.jar`) into the same `mods/` folder.

## Android / PojavLauncher

PojavLauncher cannot run shell scripts for you. See
`../release/<mc>/INSTALL.md` for the manual step-by-step (it takes the same
manifest as a checklist: identical Fabric API jar + the same mod list from
Modrinth + the AZYRON jar + the custom profile).

## What is in each pack

| Mod | Purpose | Required? |
|-----|---------|-----------|
| Fabric API | Fabric mod dependency layer | yes (exact build pinned per version) |
| Sodium | rendering engine — the big FPS gain | recommended |
| Lithium | game-logic optimization | recommended |
| FerriteCore | memory footprint reduction | recommended |
| ImmediatelyFast | immediate-mode batching for UI/HUD | recommended |
| Entity Culling | skips rendering hidden entities | recommended |
| More Culling | block/entity culling extensions | recommended |
| Dynamic FPS | throttles the game when unfocused/idle | recommended |
| ~~ModernFix~~ | — | **omitted: no Fabric builds (verified 2026-11-13)** |

AZYRON's own jar provides the client features (HUD suite, FPS BOOST
optimizer, auto sprint, custom UI). The performance mods provide the raw
frame-rate gains; the two are complementary and AZYRON does not duplicate
their functionality.
