# AZYRON CLIENT V3 — FPS BOOST profiles

One-click optimization (`ONE-CLICK OPTIMIZE` on the FPS BOOST page) applies
the **POTATO** profile and marks the first-launch flag, so the same pass
also happens automatically on first launch after install (disable in
CLIENT SETTINGS → OPTIMIZE ON FIRST LAUNCH).

## Profile specification

| Setting | POTATO | BALANCED | SMOOTH |
|---------|--------|----------|--------|
| Render distance | 2 | 6 | 8 |
| Simulation distance | 2 | 4 | 6 |
| Graphics mode | Fast | Fast | Fast |
| Max FPS | 60 | 144 | 120 |
| Mipmap levels | 0 | 2 | 4 |
| Biome blend radius | 0 | 0 | 2 |
| Clouds | OFF | OFF | Fast |
| Particles | Minimal | Decreased | Decreased |
| Smooth lighting | OFF | OFF | ON |
| Entity distance scaling | 0.5 (50%) | 0.75 | 1.0 |
| Entity shadows | OFF | OFF | ON |
| VSync | OFF | OFF | OFF |
| Auto sprint | ON (mod default) | ON | ON |

## What the optimizer actually does

`GameOptionsBridge` walks the live `Minecraft.options` object with
version-tolerant reflection (candidate field names, enum class name lookup,
attempt-save). For every setting it either applies it, reports `OK`,
reports `SKIP` (option does not exist on this game version) or `FAIL` with
the root cause — and continues with the rest. The last report is shown on
the FPS BOOST page (`LAST OPTIMIZATION`) and includes an `Options ->
saved` line when the game accepted the save.

What it does **not** do: change server-side settings, touch GPU drivers,
or guarantee frame rates. FPS results depend on device, world, view-bob,
etc. On low-end Android (PojavLauncher), expect the largest gain from the
Sodium + POTATO combination; do not expect 100+ FPS on very weak GPUs.

## Background tasks & Dynamic FPS

AZYRON does not try to out-throttle Dynamic FPS. If you also install Dynamic
FPS (recommended in the modpack), the game reduces FPS/throttles when the
window is unfocused or idle — that is the background-task reduction path,
kept outside AZYRON to avoid duplicated/conflicting functionality.

## Reducing entity/chunk pressure beyond the profile

- Entity Culling + More Culling (modpack) skip rendering of hidden entities
  and blocks — usually the biggest gain after render distance.
- FerriteCore reduces memory footprint (helps long sessions and 2–4 GB
  Android devices).
- Lithium rewrites expensive game logic (entity AI, collisions, spawning).
- ImmediatelyFast batches immediate-mode GUI/entity-model rendering.
- Sodium is the renderer rewrite; AZYRON ships compatible with it and with
  its absence.

## If a setting says SKIP

That game version simply does not expose the option under any known field
name (e.g. `biomeBlendRadius` moved/renamed). Everything else still applies.
If you find a renamed option on a new version, add its candidate name to the
`candidates` list in `common/src/client/java/com/azyron/client/client/GameOptionsBridge.java`.
