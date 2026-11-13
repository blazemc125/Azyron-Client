# AZYRON CLIENT V3 — release packaging: Minecraft 1.21.11

## What a release contains

Produced by the `AZYRON CLIENT V3 Build` GitHub Actions workflow
(`.github/workflows/azyron-build.yml`) from this repository:

| Artifact | Contents |
|----------|----------|
| `azyron-client-1.21.11-3.0.0.jar` | The mod (remapped). Put in `mods/` |
| `azyron-client-1.21.11-3.0.0-sources.jar` | Sources jar (debugging) |
| `azyron-build-log-1.21.11` | Full CI build log (evidence) |

Plus the modpack definition in `../../modpack/1.21.11/` (Fabric API
`0.141.6+1.21.11` + recommended performance mods from Modrinth).

## Install — PC (Minecraft Java Edition)

1. Install [Fabric Loader](https://fabricmc.net/use/) **0.20.1** for
   **Minecraft 1.21.11** and create a launcher profile for it.
2. Download `fabric-api-0.141.6+1.21.11.jar` from the Fabric Maven
   (`https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.141.6+1.21.11/fabric-api-0.141.6+1.21.11.jar`)
   into `.minecraft/mods/`.
3. Download the performance mods (recommended) — see
   `../../modpack/1.21.11/azyron-modpack.txt`, or run
   `../../modpack/_shared/fetch-mods.sh 1.21.11 ~/.minecraft/mods`.
4. Copy `azyron-client-1.21.11-3.0.0.jar` into `.minecraft/mods/`.
5. Launch. On the title screen, the small `AZYRON V3` button in the
   top-left corner opens the AZYRON hub.

Java requirement: **21** (matching Minecraft 1.21.11).

## Install — Android / PojavLauncher

1. In PojavLauncher, install **Minecraft 1.21.11**, then install **Fabric**
   (Custom version / mod installer in PojavLauncher) — select loader
   **0.20.1**.
2. Create the modpack/profile for it.
3. Download these files on your phone and copy them into the profile's
   `mods/` folder (PojavLauncher: `storage/games/PojavLauncher/…/mods`,
   or use a file manager pointing at the profile directory):
   - `azyron-client-1.21.11-3.0.0.jar`
   - `fabric-api-0.141.6+1.21.11.jar`
   - Sodium, Lithium, FerriteCore, ImmediatelyFast, Entity Culling,
     More Culling, Dynamic FPS — each for MC **1.21.11** + Fabric, newest
     compatible builds (see `../../modpack/1.21.11/` for exact slugs/pins).
4. Start the profile. The AZYRON HUD appears in-game; the AZYRON hub is on
   the title screen (`AZYRON V3` button, top-left).
5. Low-end devices: open **FPS BOOST → ONE-CLICK OPTIMIZE** once. This
   applies the POTATO profile (render distance 2, simulation distance 2,
   clouds off, particles minimal, smooth lighting off, entity distance 50%,
   mipmaps 0, vsync off, max FPS 60, biome blend 0, entity shadows off) and
   the same pass runs automatically on first launch (toggle in
   Client Settings).

## Known limitations

- ModernFix is **not** included (no Fabric builds for 1.21.11).
- Performance mod builds are downloaded from Modrinth by the user, so the
  exact version set depends on what upstream published at install time;
  pin the versions listed in `../../modpack/1.21.11/` for reproducibility.
- AZYRON never ships a "fake" version of another mod; if a dependency is
  unavailable for your game version, fetch-mods will say so and skip it.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Game crashes on load with `InjectionError` / mixin apply failed | You are running mismatched jars: AZYRON **1.21.11** jar requires Minecraft **1.21.11** + Fabric Loader **0.20.1**. Check `logs/latest.log`. |
| `Mod azyronclient requires fabric-api` | Fabric API missing or wrong version — install `0.141.6+1.21.11`. |
| HUD not visible | HUD is hidden in debug screen / with F1 (GUI hidden). Press F1. Check `azyronclient-v3.properties` in `<config>/` (delete it to reset defaults). |
| `AZYRON V3` button missing on title screen | Mod did not load — check the game log for `azyronclient`; ensure the jar is in the profile's `mods` folder, not `mods/1.21.11` subfolder (unless using version-specific folders). |
| Stuttering on Android | Apply POTATO profile; disable Dynamic FPS if you want the background throttling off (it saves battery but pauses the game loop when minimized). |
