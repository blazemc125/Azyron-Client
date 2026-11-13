# AZYRON CLIENT V3 — compatibility matrix (2026-11-13)

Status key: **CI PASS** = workflow green (none yet — no push has happened) ·
**CI PENDING** = awaits the `AZYRON CLIENT V3 Build` run · **VERIFIED-SRC** =
API surface cross-checked against upstream sources/tags (not compiled) ·
**UNVERIFIED** = must be validated in-game.

| Component | 1.21.11 | 26.1 | 26.2 | 26.3 |
|-----------|---------|------|------|------|
| Minecraft | 1.21.11 | 26.1 (26.1.x) | 26.2 | 26.3 |
| Java | 21 | 25 | 25 | 25 |
| Mappings | official Mojang (`loom.officialMojangMappings()` via `fabric-loom-remap`) | none needed (unobfuscated, Loom `fabric-loom`) | same as 26.1 | same as 26.1 |
| Fabric Loader | 0.20.1 | 0.20.1 | 0.20.1 | 0.20.1 |
| Fabric Loom | 1.19-SNAPSHOT (`fabric-loom-remap`) | 1.19-SNAPSHOT (`fabric-loom`) | 1.19-SNAPSHOT (`fabric-loom`) | 1.19-SNAPSHOT (`fabric-loom`) |
| Fabric API | 0.141.6+1.21.11 | 0.145.1+26.1 | 0.161.0+26.2 | 0.165.2+26.3 |
| Build | CI PENDING | CI PENDING | CI PENDING | CI PENDING |
| Sodium | 0.8.14 (stable) | 0.9.2 (tag mc26.1.2-0.9.2) | 0.9.2 (tag mc26.2-0.9.2) | 0.9.2 stable; 0.9.3-beta.5 also published |
| Lithium | fetch at install | fetch at install | fetch at install | fetch at install |
| FerriteCore | fetch at install | fetch at install | fetch at install | fetch at install |
| ImmediatelyFast | fetch at install | fetch at install | fetch at install | fetch at install |
| Entity Culling | fetch at install | fetch at install | fetch at install | fetch at install |
| More Culling | fetch at install | fetch at install | fetch at install | 1.9.2 (beta) fixes Sodium ≥0.9.3-beta.2 clash |
| Dynamic FPS | fetch at install | fetch at install | fetch at install | fetch at install |
| ModernFix | **omitted** — no Fabric builds (verified 2026-11-13) | omitted | omitted | omitted |

## Dependency resolution policy

- AZYRON's own jar has **no embedded dependencies** (no bundled jars, no
  shadowed libraries). It declares `fabric-api` as a hard dependency and
  everything else is optional.
- The modpack fetch scripts resolve performance mods from **live Modrinth
  metadata** (`loaders=[fabric]&game_versions=[<mc>]`, newest compatible
  build, skipped with a warning if none exists). They never invent a
  version and never fabricate hashes.
- Upstream tags spot-checked on 2026-11-13 via the GitHub API/releases:
  Sodium `mc1.21.11-0.8.14`, `mc26.1.2-0.9.2`, `mc26.2-0.9.2`, `mc26.3-0.9.2`,
  `mc26.3-0.9.3-beta.5` (all assets-less releases; jars are published on
  Modrinth/Fabric Maven, which is why the fetch scripts exist).

## What is verified vs. not

Build configuration (where pins live, why the plugin ids differ, why client
classes are in the jar) is documented in `BUILD-CONFIG.md`.

**Verified against upstream sources (this repo, 2026-11-13):**
- Fabric API HUD API signatures for all four versions
  (`HudElementRegistry.addLast`, `HudElement.render` / `extractRenderState`).
- Screen/`GuiGraphicsExtractor` render flow (`extractRenderState`,
  `extractBackground`/`renderBackground`) — all four versions' testmods.
- GUI pose stack API (`pose().pushMatrix()/popMatrix()/translate(float,float)/scale(float)`)
  — confirmed in 1.21.11 and 26.x testmods; the adapter code uses it uniformly.
- Input event classes (`net.minecraft.client.input.MouseButtonEvent`,
  signatures `mouseClicked(MouseButtonEvent, boolean)`,
  `mouseDragged(MouseButtonEvent, double, double)`,
  `mouseReleased(MouseButtonEvent)`) — all four versions.
- Lifecycle events (`ClientLifecycleEvents.CLIENT_STARTED`,
  `ClientTickEvents.END_CLIENT_TICK`) — all four versions.
- `HudElement` SAM bodies per version: 1.21.11 `render(GuiGraphics, DeltaTracker)`,
  26.x `extractRenderState(GuiGraphicsExtractor, DeltaTracker)` — read directly
  from `fabric-rendering-v1` in all four tarballs.
- `Button.Builder.pos/size/bounds`, `Screen#addRenderableWidget`,
  `Minecraft#setScreen` and 26.3's `setScreenAndShow` — the AZYRON code uses
  only `pos`/`size`/`setScreen`, which exist on all four versions.
- `Identifier.fromNamespaceAndPath` is the correct factory on all four
  versions (Mojang's `ResourceLocation` → `Identifier` rename); no
  `ResourceLocation` references exist in any of the four Fabric API tarballs.
- 26.x `BaseAzyronScreen` pattern (`extractBackground` → paint →
  `super.extractRenderState`) matches `ChannelScreen` in
  `fabric-networking-api-v1` verbatim, and 1.21.11's
  (`renderBackground` → paint → `super.render`) matches the same testmod.
- Sub-method surface used by the adapters: 26.x `GuiGraphicsExtractor`
  exposes `text(Font, String, int, int, int)` and
  `centeredText(Font, String, int, int, int)`; 1.21.11 `GuiGraphics` exposes
  `drawString` / `drawCenteredString`. `guiWidth()`, `guiHeight()`,
  `fill(...)` and the 2D pose matrix calls (`pushMatrix`, `popMatrix`,
  `translate(float, float)`, `scale(float)`) are used by Fabric's own code on
  every version.
- `options.keyUp/keyDown/keyLeft/keyRight/keyJump/keyAttack/keyUse` and
  `KeyMapping#isDown()` appear in Fabric API sources for 1.21.11 and 26.3
  (`options.keyAttack.isDown()` in `fabric-events-interaction-v0`), so the
  keystroke HUD compiles on both API eras.
- `Player#getArmorValue()` is used by Fabric's own HUD status bar code on
  26.3.
- Mouse handler targets in Fabric screen mixins — all four versions.

**Not verified (requires the CI build and an actual game):**
- Compilation of the 36 Java sources (no JDK available in the authoring
  environment; Maven/Mojang/Gradle hosts unreachable).
- Every direct reference into Minecraft internals
  (`minecraft.options.keyUp`, `getArmorValue`, `hideGui`, `framerateLimit`,
  enum class names `GraphicsStatus`/`ParticleStatus`/`CloudStatus`,
  `OptionInstance#set`). These are centralized in
  `GameOptionsBridge`/`AzyronReflect` and degrade to reported SKIP/FAIL
  instead of crashing — but their exact names per version must be confirmed
  on first launch.
- Title-screen mixin (`TitleScreenMixin`, `init()V` TAIL injection).
- 26.x `Screen#extractBackground` accessibility (testmod evidence shows a
  public override; AZYRON only calls it).
- In-game behavior of the HUD, screens, auto-sprint, POTATO application.
- Screen layout at unusual GUI scales: all six screens were checked
  arithmetically at 320x240 through 1024x512 (no control leaves the window,
  no buttons overlap, nothing draws over the footer), but not on a device.

See `API-VERIFICATION.md` for the exact call-site inventory.
