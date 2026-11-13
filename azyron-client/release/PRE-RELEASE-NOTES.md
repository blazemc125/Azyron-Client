# AZYRON CLIENT V3 — pre-release (untested build)

> **This is a pre-release.** The jars here were produced by CI from the
> sources in this repository, but **nobody has launched them in Minecraft
> yet**. They may fail to load. Treat them as a test build, not a finished
> release.

## Read this first

- These jars are **Fabric mods**, not a launcher and not a client install.
  You need Minecraft Java Edition **plus Fabric Loader 0.20.1** and
  **Fabric API** for the matching game version.
- Each jar only works on **one** Minecraft version. Download the one that
  matches your profile — `26.3` will not load on `26.1`.
- Java is not optional: **1.21.11 needs Java 21**, **26.x needs Java 25**.
  A mismatch fails at launch with `Unsupported class file major version`.
- Nothing is bundled. Sodium, Lithium, FerriteCore, ImmediatelyFast,
  Entity Culling, More Culling and Dynamic FPS are fetched separately by the
  scripts in `modpack/`; ModernFix is deliberately absent because no Fabric
  build of it exists for these versions.

## Files

| Jar | Minecraft | Java | Fabric API to install |
|-----|-----------|------|-----------------------|
| `azyron-client-1.21.11-3.0.0.jar` | 1.21.11 | 21 | `0.141.6+1.21.11` |
| `azyron-client-26.1-3.0.0.jar` | 26.1 | 25 | `0.145.1+26.1` |
| `azyron-client-26.2-3.0.0.jar` | 26.2 | 25 | `0.161.0+26.2` |
| `azyron-client-26.3-3.0.0.jar` | 26.3 | 25 | `0.165.2+26.3` |

Fabric Loader **0.20.1** for all four.

Take the plain `.jar`. Do not use a file ending in `-dev.jar` or
`-sources.jar` — those are development artifacts and will not load.

## Install

**PC**

1. Install Fabric Loader 0.20.1 for your game version, launch once, close.
2. Put `fabric-api-<pinned>.jar` and the matching `azyron-client-<mc>-3.0.0.jar`
   in `.minecraft/mods/`.
3. Optionally fetch the performance mods: `modpack/_shared/fetch-mods.sh <mc>`.
4. Launch. A compact `AZYRON V3` button appears top-left on the title screen.

**Android (PojavLauncher)**

1. Create a profile for the exact Minecraft version, Fabric 0.20.1, and the
   Java runtime the version needs (21 or 25).
2. Drop `fabric-api` and the matching AZYRON jar into that profile's
   `mods` folder.
3. Give the profile enough RAM (at least 1 GB; more for the 26.x versions).

Full per-version instructions, including a troubleshooting table, are in
`release/<mc>/INSTALL.md` in the repository.

## What it adds

- **HUD**: FPS, CPS, ping with a colour dot, coordinates, a keystroke grid
  (W/A/S/D, L/R click with CPS, space) and an armour bar. Every element can
  be toggled, scaled, recoloured and dragged.
- **FPS BOOST**: one-click POTATO profile (render distance 2, simulation
  distance 2, clouds off, particles minimal, smooth lighting off, entity
  distance 50%, mipmap 0, VSync off, max FPS 60, biome blend 0, entity
  shadows off) plus BALANCED and SMOOTH, with a report of exactly which
  settings were applied and which were skipped.
- **Auto sprint**, opt-in first-launch optimisation, and a hub screen with a
  VERSIONS page that detects the running build.

## Known risks in a pre-release

The settings applier uses version-tolerant reflection with candidate field
names. Mojang renamed several option fields between 1.21.11 and 26.x, so on
first launch open **FPS BOOST** and read the `LAST OPTIMIZATION` block: a
`FAIL` line names the setting that did not apply. The client reports those
instead of crashing, but it means that one setting stayed at its old value.

If the game crashes before the title screen, the likely causes are the Java
version (see above) or the title-screen mixin on a version where the target
method changed. Attach `logs/latest.log` to an issue — it names the exact
class and line.

## Reporting

Open an issue at
https://github.com/blazemc125/Azyron-Client/issues with your Minecraft
version, Java version, the jar filename, and `logs/latest.log`.

## License

MIT. AZYRON CLIENT V3 by JIGNESH GAMING. Minecraft is a trademark of Mojang
Studios; this project is not affiliated with or endorsed by Mojang or
FabricMC.
