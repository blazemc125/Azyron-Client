# AZYRON CLIENT V3 — build logs

Genuine build evidence lives here.

- `2026-11-13-local-attempt.md` — why the authoring machine could not build
  (no JDK/Gradle; all build-relevant hosts unreachable). **No jar was
  produced locally and none is claimed.**
- When the `AZYRON CLIENT V3 Build` GitHub Actions workflow has run, download
  the `azyron-build-log-<minecraft>` artifacts and drop them here, e.g.
  `2026-11-13-github-actions-1.21.11.log`, one per supported version.

Verification checklist before any release (run per version):

1. `./gradlew :versions:<flavor>:build` finishes `BUILD SUCCESSFUL`.
2. `versions/<flavor>/build/libs/azyron-client-<mc>-3.0.0.jar` exists and is
   the **remapped** jar (not `-dev.jar`, not `-sources.jar`).
3. Jar validation (the CI workflow does this automatically):
   `python3 ../docs/validate-jar.py <jar>` — checks `fabric.mod.json`,
   `azyronclient.client.mixins.json`, `assets/azyronclient/icon.png`,
   entrypoint classes and zip integrity.
4. Launch the game with the jar in `mods/`; confirm:
   - game reaches the title screen without a mixin/crash error,
   - the `AZYRON V3` button is visible top-left on the title screen,
   - AZYRON home screen opens, HUD renders in-world,
   - `logs/latest.log` contains `AZYRON CLIENT V3 v3.0.0 by JIGNESH GAMING`.
5. Paste the per-version `latest.log` excerpt + CI log into this directory as
   `*.log` before tagging a release.
