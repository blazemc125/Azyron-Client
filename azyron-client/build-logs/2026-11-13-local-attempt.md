# Build attempt & verification log — 2026-11-13

## Local build: still impossible in the authoring sandbox

No JDK or Gradle is installed and none can be installed:

- `apt-get` is denied (no root, no package manager network).
- `jdk4py` wheels are **JRE-only** — they contain no `javac`, so even with a
  JRE present the code cannot be compiled.
- All build-relevant hosts (`services.gradle.org`, `plugins.gradle.org`,
  `maven.fabricmc.net`, Mojang hosts) are unreachable from here (connection
  refused / `000`).

**No jar has been produced anywhere. Nothing in this repository is claimed
to be built or tested in-game.** The only path to a real jar is the
`AZYRON CLIENT V3 Build` GitHub Actions workflow.

## What *was* verified statically (no compilation)

All four Fabric API source tarballs (`0.141.6+1.21.11`, `0.145.1+26.1`,
`0.161.0+26.2`, `0.165.2+26.3`), the four `fabric-example-mod` templates and
Fabric Loom `v1.19.4` were read directly. Findings are recorded in
`docs/API-VERIFICATION.md` and `docs/COMPATIBILITY.md`; the important ones:

1. **Client classes do reach the jar.** Loom's
   `MinecraftSourceSets.Split.evaluate()` adds the `client` source set's
   class dirs and resources to the `main` jar task. The required-class list in
   `docs/validate-jar.py` (including
   `com/azyron/client/flavor/AzyronClientEntrypoint.class`) is therefore
   correct.
2. **`main` is split-safe.** `AzyronCommonEntrypoint` imports no
   `net.minecraft.*` symbols and no client classes.
3. **Empty `repositories { }` blocks are fine.** Loom declares the Fabric,
   Mojang and Maven Central repositories itself.
4. **API surface used by the code exists on the matching version**
   (`HudElement` SAM, `Identifier.fromNamespaceAndPath`,
   `Button.Builder.pos/size`, `GuiGraphicsExtractor.text/centeredText`,
   `GuiGraphics.drawString/drawCenteredString`, `options.key*`,
   `Player#getArmorValue`, `extractBackground` / `renderBackground`).

## Defects found and fixed in this pass

| Defect | Fix |
|--------|-----|
| Per-version settings lived in `versions/*/gradle.properties`; Gradle's best-practice guide says not to do this, and the `plugins { version "..." }` block resolves before those files are applied | Moved every pin into each `versions/<flavor>/build.gradle` `ext { }` block; deleted the subproject property files |
| CI `cache-dependency-path: azyron-client` pointed at a directory; `actions/setup-java` throws `No file in <cwd> matched to [...]` when the glob matches no file | Replaced with an explicit list of wrapper/build files |
| `AzyronHomeScreen` used a fixed 148/150px panel; on a 240px-tall GUI the back button ran off-screen and collided with the footer | Panel height, button height, nav origin and back-button Y now derive from the screen size |
| `VersionSelectScreen` had a fixed 42px row stride and could overrun the footer on short screens | Row stride, row height and instruction block are computed from available space |
| `AzyronClientState.get()` threw `IllegalStateException` if the state was not initialised yet (reachable from a title-screen click path) | Falls back to a lazily created default state |
| `GameOptionsBridge.apply(null)` would NPE | Explicit null check returns a `FAIL` report line instead |
| `FpsBoostProfile.SMOOTH` did not match `docs/FPS-BOOST.md` (clouds / smooth lighting) | Enum corrected to match the documented profile |
| `FpsBoostScreen` placed its profile cards, auto-apply button and report block past the bottom of the window on every GUI size | Card height/gap and report line count derived from available space |
| `HudSettingsScreen` bottom row buttons overlapped at 320–360px GUI widths | Row split proportionally |
| `ClientSettingsScreen` about panel and credits drew under the back button | Panel height bounded, credit lines drawn only while they fit |
| HUD element sizes were duplicated in renderer and editor; renderer clamped against a fixed 12px margin and clipped the 66px keystroke grid | Single `HudElementSizes` table; clamping uses real scaled size |
| Unused `java.util.Locale` import in `ClientSettingsScreen` | Removed |

Syntax-only check after the edits: 36 Java files parsed with tree-sitter,
0 errors. This is **not** compilation.

## Next steps (requires push + GitHub Actions)

1. Push the branch (a human must do this — this repo's policy forbids pushes
   by the agent).
2. Actions → **AZYRON CLIENT V3 Build** → Run workflow (or push a change
   under `azyron-client/`).
3. Four jobs must be green. Download `azyron-client-<mc>` and
   `azyron-build-log-<mc>` for each.
4. Put the logs here as `2026-11-13-github-actions-<mc>.log`, run
   `python3 docs/validate-jar.py <jar>` on each jar, then follow
   `release/README.md` and `build-logs/README.md`.
5. First launch: check `logs/latest.log` for the AZYRON banner, open the hub,
   read the `LAST OPTIMIZATION` report on the FPS BOOST page, and paste both
   into this directory.
