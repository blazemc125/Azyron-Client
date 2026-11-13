# API verification — what was checked, against what, and what to watch

Method: the Fabric API sources for all four target versions were fetched as
source tarballs from `codeload.github.com` (`FabricMC/fabric` at the tags
matching the pinned Fabric API builds) and extracted for inspection:

- `fabric-0.141.6+1.21.11`
- `fabric-0.145.1+26.1`
- `fabric-0.161.0+26.2`
- `fabric-0.165.2+26.3`

plus the per-version `FabricMC/fabric-example-mod` templates (branches
`1.21.11`, `26.1`, `26.2`, `26.3`). No Minecraft game jar was available in
the authoring environment (Mojang/Maven hosts unreachable), so all
game-class-facing calls remain **unverified until first CI build + launch**.

## Call-site inventory (file → upstream evidence)

| Call site | Evidence |
|-----------|----------|
| `HudElementRegistry.addLast(Identifier, HudElement)` (all flavors) | Present in `fabric-rendering-v1` sources, all 4 versions; KDoc confirms no render condition inherited. `HudElement` SAM: 1.21.11 → `render(GuiGraphics,DeltaTracker)`; 26.x → `extractRenderState(GuiGraphicsExtractor,DeltaTracker)`. Lambda arity matches both. |
| `AzyronClientEntrypoint` lambda `(graphics, tickCounter) -> ...` | Parameter names are free; types inferred from the `HudElement` SAM above. |
| `BaseAzyronScreen (26.x)`: `Screen#extractRenderState(GuiGraphicsExtractor,int,int,float)` override + `super.extractRenderState(...)` | `ChannelScreen` testmod (fabric-networking-api-v1, all 26.x) shows the exact same `@Override` + `this.extractBackground(...)` + `super.extractRenderState(...)` pattern. |
| `BaseAzyronScreen (1.21.11)`: `render(GuiGraphics,int,int,float)` + `renderBackground(graphics, mouseX, mouseY, delta)` | Screen render pattern from 1.21.11 testmods (`ChannelScreen`, `PositionedScreen`); `renderBackground(GuiGraphics,IIF)` target confirmed in `fabric-screen-api-v1` mixin (`ScreenMixin`, all 4). |
| `AzyronUi#pushScale` adapters: `pose().pushMatrix() / translate(float,float) / scale(float) / popMatrix()` | `HudTests` + `HudStatusBarHeightRegistryImpl` in all four versions use only these pose methods (`pushMatrix`, `popMatrix`, `translate(0.0F, height)`, `scale(0.25f)`). The 3-float `translate(x,y,z)`/`scale(x,y,z)` PoseStack API does **not** exist on these versions — an earlier revision of the adapter used it and was fixed. |
| `graphics.fill(x1,y1,x2,y2,argb)` | Used in all four testmods. |
| `graphics.text / centeredText(Font, String, int, int, int)` (26.x) | `HudTests` (26.x). 1.21.11 uses `drawString/drawCenteredString` — adapter matches each flavor. |
| `mouseClicked/mouseDragged/mouseReleased` overrides (`HudEditScreen`) | Screen mouse handler target signatures confirmed in `fabric-screen-api-v1` mixins (`MouseHandlerMixin`: `mouseClicked(MouseButtonEvent;Z)Z`, `mouseReleased(MouseButtonEvent;)Z`, `mouseDragged(MouseButtonEvent;DD)Z`) — imports `net.minecraft.client.input.MouseButtonEvent` confirmed for all four versions. Event accessors `event.x()`, `event.y()`, `event.button()` confirmed via public docs/sources (NeoForm/1.21.9 input primer: MouseButtonEvent wraps button + XY). |
| `Button.builder(Component, OnPress).pos().size().build()`, `addRenderableWidget`, `this.width/height/minecraft/font` | ChannelScreen testmod pattern (both APIs). |
| `TitleScreenMixin` `@Inject(method = "init()V", at = @At("TAIL"))` | `TitleScreen` overrides `init()` in current versions; the mixin is `defaultRequire = 1`, so a target rename would fail loudly at startup, not silently. **This is the most likely first runtime failure after CI runs.** The mixin config `compatibilityLevel` is `JAVA_21` / `JAVA_25`, matching the official example-mod templates byte for byte (verified against the `26.1`/`26.2`/`26.3` template `*.mixins.json`). Note: Sponge Mixin's own enum stops at `JAVA_15`; the levels above that are supplied by the Fabric Loader fork that ships with Loader 0.20.1 — the template values are the ones to trust. |
| `ClientLifecycleEvents.CLIENT_STARTED` / `ClientTickEvents.END_CLIENT_TICK` | `fabric-lifecycle-events-v1` sources, all 4. |
| `FabricLoader.getInstance().getConfigDir()/getAllMods()/getModContainer()` | `fabric-loader` API (stable since 0.12); loader 0.20.1 pinned. `getModContainer("minecraft")` — the built-in Minecraft mod id is `minecraft`. |
| `AzyronHudRenderer` input reads: `minecraft.options.keyUp/keyDown/keyLeft/keyRight/keyJump/keyAttack/keyUse .isDown()` | `KeyMapping.get/isDown` is stable public API; exact field names in `options` have been stable since 1.16. **Unverified on 26.x.** |
| `AzyronReflect`: `hideGui`, `getFps`, `getConnection/getPlayerInfo/getLatency`, `getArmorValue` | Wrapped in try/catch with -1 fallbacks — worst case the HUD shows `--` for that value. |
| `GameOptionsBridge`: `renderDistance`, `simulationDistance`, `graphicsMode` (`GraphicsStatus`), `particles` (`ParticleStatus`), `cloudStatus`/`cloudsType`/`clouds` (`CloudStatus`), `smoothLighting`, `entityDistanceScaling`, `mipmapLevels`, `enableVsync`, `framerateLimit`/`maxFramerate`, `biomeBlendRadius`, `entityShadows` | **Highest-risk area.** Mojang renamed options between 1.21.x and 26.x; the bridge tries candidate names and enum class name `net.minecraft.client.GraphicsStatus` / `net.minecraft.client.ParticleStatus` / `net.minecraft.client.CloudStatus` (exact package unknown — `resolveEnumConstant` returns null → `FAIL` line, never crash). First launch: read `LAST OPTIMIZATION` on the FPS BOOST page; any `FAIL` line tells you exactly which candidate names to add in `GameOptionsBridge.java`. |
| `OptionInstance<…>#set(value)` discovery via `getMethods()` name `set` + param count 1 | Mojang `OptionInstance` has `set(T)`. Reflection never hard-wires the signature. |

## Verified in the 2026-11-13 verification pass

These were checked directly against the four Fabric API source trees and
Fabric Loom 1.19.4:

| Item | Evidence |
|------|----------|
| Client classes are packaged into the remapped jar | Loom `MinecraftSourceSets.Split.evaluate()` does `jar.from(clientOnlySourceSet.getOutput().getClassesDirs())` (and the resources dir) on the `main` jar task. This is what makes the required-class list in `validate-jar.py` valid, including `flavor/AzyronClientEntrypoint.class`. |
| `main` source set is split-safe | `AzyronCommonEntrypoint` imports only `net.fabricmc.api.ModInitializer`, slf4j and `AzyronConstants` — no `net.minecraft.*` and no client classes. Gradle's split build compiles `main` against the common-only Minecraft jar. |
| Empty `repositories { }` is fine | Loom's `LoomRepositoryPlugin` declares the Fabric maven, the Mojang libraries repo and Maven Central itself. |
| `HudElement` SAM per version | 1.21.11: `render(GuiGraphics, DeltaTracker)`; 26.x: `extractRenderState(GuiGraphicsExtractor, DeltaTracker)`. Both entrypoints match (the lambda's parameters are inferred). |
| `Identifier.fromNamespaceAndPath` | 374/317/377 call sites across the 1.21.11 / 26.1 / 26.3 tarballs, and zero `ResourceLocation` factory calls. |
| `Button.Builder.pos(...).size(...)` | Used by Fabric's own `ChannelScreen` on all four versions, so the call is valid on both API eras (26.3 also has `bounds(...)`, which AZYRON does not need). |
| Keystroke HUD inputs | `options.keyAttack.isDown()` appears in `fabric-events-interaction-v0` (`MinecraftMixin`) on both 1.21.11 and 26.3, and `options.keyUse` in the 26.3 gametest API. |
| Armor HUD input | `player.getArmorValue()` is used by `HudStatusBarHeightRegistryImpl` and the HUD gametest on 26.3. |
| Build properties location | Per-version values live in each `build.gradle`'s `ext { }` block; no subproject `gradle.properties` (Gradle's best-practice guide explicitly warns against them, and the `plugins { version "..." }` block resolves before they apply). See `BUILD-CONFIG.md`. |
| CI Java cache config | `actions/setup-java`'s `computeCacheKey` throws `No file in <cwd> matched to [...]` when `cache-dependency-path` matches no file, so the workflow lists files, not the `azyron-client` directory. |

## Screen layout constraints

The GUI coordinate space shrinks as the GUI scale grows: a 1080p phone at
scale 4 gets a 480×270 screen, and PojavLauncher users sometimes run 240px
tall. Both `AzyronHomeScreen` and `VersionSelectScreen` therefore compute
their panel height, button height, row stride and back-button position from
`this.width` / `this.height` instead of hard-coding them, and the back
button is clamped above the footer line drawn at `height - 12`. All six screens were checked across 320×240, 426×240, 480×270, 640×360,
854×480 and 1024×512 GUI sizes: every control stays inside the window, no
two buttons overlap, and nothing is drawn over the footer line.

Fixes made during that check:

- `FpsBoostScreen`: the three profile cards ran to y=216 and the auto-apply
  button and report block were placed below them on *every* screen size.
  Card height, gap, card text rows, APPLY-button size/position and the
  report line count are now all derived from the space above the back
  button (`height - 32`), including a hard clamp so `bottomY` / `reportY`
  can never pass it.
- `HudSettingsScreen`: the DRAG LAYOUT / RESET / BACK row overlapped at
  320–360px widths; the three buttons now share the row proportionally.
- `ClientSettingsScreen`: the about panel and credit lines spilled under the
  back button; the panel now ends above it and credit lines are drawn only
  while they fit.
- `HudEditScreen` / `AzyronHudRenderer`: element sizes were duplicated in two
  places and the renderer clamped every element against a fixed 12px bottom
  margin, clipping the 66px-tall keystroke grid. Both now use
  `HudElementSizes`, and clamping uses each element's real scaled size.

## Deliberate omissions (do not "fix" these)

- **ModernFix** — no Fabric builds exist for 1.21.11/26.1/26.2/26.3
  (verified on Modrinth 2026-11-13). Not bundled, not referenced.
- **Cloth Config / Mod Menu integration** — AZYRON draws its own screens;
  adding Mod Menu as a hard dep would burden PojavLauncher users. A Mod
  Menu `modmenu.api.ModMenuApi` entry is intentionally left out rather than
  half-integrated.
- **Dynamic FPS as an embedded feature** — background throttling is left to
  the Dynamic FPS mod (it ships in the modpack); duplicating it inside
  AZYRON would conflict.
- **Version hot-switching** — impossible for a mod; the VERSIONS screen
  detects and displays, switching happens at the launcher/profile level.

## Test plan for the first CI + launch cycle

1. CI green for all four flavors (`.github/workflows/azyron-build.yml`).
2. `python3 docs/validate-jar.py <jar>` per version.
3. Launch each version → title screen shows `AZYRON V3` top-left.
4. Open hub → VERSIONS page shows the running version with `RUNNING` badge.
5. FPS BOOST → ONE-CLICK OPTIMIZE → report shows `OK`/`SKIP` lines, zero
   `FAIL` ideally; paste report into `build-logs/`.
6. In-world: FPS/CPS/PING/COORDS/KEYSTROKES/ARMOR HUD visible; layout
   editor drags and persists across relaunch.
7. Record `logs/latest.log` lines containing `AZYRON` into `build-logs/`.
8. Fix-forward: for any `FAIL`, extend candidate lists in
   `GameOptionsBridge.java`; for any mixin failure, retarget
   `TitleScreenMixin` (keep `defaultRequire = 1`).
