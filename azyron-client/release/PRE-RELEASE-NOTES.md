![Version 3.0.0 Beta 1](https://img.shields.io/badge/Version-3.0.0%20Beta%201-C6C6CE?style=for-the-badge) ![Type Pre-release](https://img.shields.io/badge/Type-Pre--release-E5534B?style=for-the-badge) ![Minecraft 1.21.11 / 26.x](https://img.shields.io/badge/Minecraft-1.21.11%20%7C%2026.x-62B47A?style=for-the-badge) ![Java 21 / 25](https://img.shields.io/badge/Java-21%20%7C%2025-ED8B00?style=for-the-badge) ![Fabric Loader 0.20.1](https://img.shields.io/badge/Fabric%20Loader-0.20.1-DBB5D4?style=for-the-badge)

## AZYRON CLIENT V3 — Beta 1

First public build of **AZYRON CLIENT V3**, a multi-version Fabric client by
**JIGNESH GAMING**: an FPS HUD suite, a one-click optimizer, FPS BOOST
profiles and a custom AZYRON interface, built with low-end Android and
PojavLauncher in mind.

One jar per Minecraft version, all built from the same sources.

> **This is a pre-release.** The jars are produced by CI from this
> repository, but they have not been played yet. Expect rough edges, and
> please report them.

## What's included

- **HUD** — FPS, CPS, ping with a colour-coded latency dot, coordinates, a
  keystroke grid (W/A/S/D, left/right click with CPS, space) and an armour
  bar. Every element toggles, scales, recolours and drags.
- **FPS BOOST** — one-click **POTATO** profile (render distance 2,
  simulation distance 2, clouds off, particles minimal, smooth lighting off,
  entity distance 50%, mipmap 0, VSync off, max FPS 60, biome blend 0,
  entity shadows off), plus **BALANCED** and **SMOOTH**, each applied with a
  per-setting report of what took effect.
- **Auto sprint** and opt-in first-launch optimisation.
- **AZYRON hub** — replaces nothing; a compact `AZYRON V3` button sits on the
  vanilla title screen, and the hub has FPS BOOST, HUD LAYOUT, HUD SETTINGS,
  VERSIONS and CLIENT SETTINGS pages.

### Added

- Four independent builds: **1.21.11**, **26.1**, **26.2**, **26.3**.
- Native 26.x screen support (`GuiGraphicsExtractor`) alongside the 1.21.11
  one (`GuiGraphics`), so no version runs on a compatibility shim.
- VERSIONS page that detects the running build and explains switching.
- Config lives in `config/azyronclient-v3.properties`.

### Known issues in Beta 1

- The options applier uses version-tolerant reflection. If Mojang renamed a
  setting on your version, that one setting is reported as `FAIL` in the
  **LAST OPTIMIZATION** block instead of being applied. Open FPS BOOST after
  launching and check the report.
- The jars are untested in-game. This release exists to get builds into
  people's hands for that testing.

### Note on Java

**1.21.11 requires Java 21. 26.x requires Java 25.** A mismatch fails at
launch with `Unsupported class file major version`. This is the most common
install mistake.

### Install

1. Install **Fabric Loader 0.20.1** for your Minecraft version and launch once.
2. Download the jar below that matches your version, and the pinned
   **Fabric API** from the table.
3. Put both into your `mods` folder:

   | Jar | Minecraft | Java | Fabric API |
   |-----|-----------|------|------------|
   | `azyron-client-1.21.11-3.0.0.jar` | 1.21.11 | 21 | `0.141.6+1.21.11` |
   | `azyron-client-26.1-3.0.0.jar` | 26.1 | 25 | `0.145.1+26.1` |
   | `azyron-client-26.2-3.0.0.jar` | 26.2 | 25 | `0.161.0+26.2` |
   | `azyron-client-26.3-3.0.0.jar` | 26.3 | 25 | `0.165.2+26.3` |

4. Launch and press the `AZYRON V3` button on the title screen.

**Android / PojavLauncher:** create the profile with the exact Minecraft
version, Fabric 0.20.1 and the matching Java runtime, then drop both jars in
that profile's `mods` folder. Give it at least 1 GB of RAM.

Do not use files ending in `-dev.jar` or `-sources.jar` — those will not load.

Optional performance mods: Sodium, Lithium, FerriteCore, ImmediatelyFast,
Entity Culling, More Culling and Dynamic FPS. The scripts in `modpack/` fetch
the right build for your version. **ModernFix is deliberately not included** —
no Fabric build of it exists for these versions.

### Reporting

Open an issue with your Minecraft version, Java version, jar filename and
`logs/latest.log`: https://github.com/blazemc125/Azyron-Client/issues

Full per-version instructions and a troubleshooting table live in
`release/<mc>/INSTALL.md` in the repository.

---

**License:** MIT · **Developer:** JIGNESH GAMING

Minecraft is a trademark of Mojang Studios. AZYRON CLIENT V3 is not
affiliated with or endorsed by Mojang or FabricMC.
