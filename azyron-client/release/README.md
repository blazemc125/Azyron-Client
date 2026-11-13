# AZYRON CLIENT V3 — release packages

Per-version release folders. Each `<mc>/INSTALL.md` is the exact install
procedure (PC + Android/PojavLauncher) and the troubleshooting table for
that version. **No binaries are committed here** — the jars are produced by
CI (`.github/workflows/azyron-build.yml`) and attached to its runs as
artifacts (`azyron-client-<mc>`).

| Folder | Minecraft | Java | Fabric API |
|--------|-----------|------|------------|
| `1.21.11/` | 1.21.11 | 21 | 0.141.6+1.21.11 |
| `26.1/` | 26.1 (26.1.x) | 25 | 0.145.1+26.1 |
| `26.2/` | 26.2 | 25 | 0.161.0+26.2 |
| `26.3/` | 26.3 | 25 | 0.165.2+26.3 |

## Quick download (pre-release)

The fastest path to a downloadable jar is the **pre-release** job:

1. Actions → **AZYRON CLIENT V3 Build** → **Run workflow**.
2. Set `publish` to **prerelease**, keep the branch you want to build.
3. Wait for the four build jobs, then the `Publish v3.0.0-beta.1 pre-release` job.
4. The jars appear at:

   ```
   https://github.com/blazemc125/Azyron-Client/releases/tag/v3.0.0-beta.1
   ```

That job refuses to publish unless all four matrix builds passed, refuses
anything that is not a shippable jar (`-sources` / `-dev` are skipped), runs
`docs/validate-jar.py` on each jar, requires exactly four jars, and will not
overwrite an existing `v3.0.0-beta.1` — delete the old one first.

It is a **pre-release**: untested in-game, so it is not announced as 3.0.0.

### Publishing by hand instead

If the Actions token is not allowed to write, create the same release
locally after downloading the four jars:

```sh
gh release create v3.0.0-beta.1 \
  --repo blazemc125/Azyron-Client \
  --target <commit-sha> \
  --prerelease \
  --title "AZYRON CLIENT V3 v3.0.0-beta.1 - Beta 1" \
  --notes-file azyron-client/release/RELEASE-BODY.md \
  azyron-client-1.21.11-3.0.0.jar azyron-client-26.1-3.0.0.jar \
  azyron-client-26.2-3.0.0.jar azyron-client-26.3-3.0.0.jar
```

Editing later: open the release page → pencil icon for the notes, or
*Edit release* for the title/tag; the *Pre-release* checkbox is on that same
form, and unticking it promotes the release to a full (Latest) release.
Assets are managed with *Edit release* → drag files in / the X to remove.

If `publish` stays `none` (the default), the workflow only builds and uploads
run artifacts, exactly as before.

## Producing a genuine release (owner)

1. Push the branch (agents in this repo may not push — this is a human step).
2. Watch **Actions → AZYRON CLIENT V3 Build**: 4 jobs, one per version.
   All four must be green; a red job means that version ships nothing.
3. Download the four `azyron-client-<mc>` artifacts + the four
   `azyron-build-log-<mc>` logs.
4. Drop the logs into `../build-logs/` (e.g. `2026-11-13-github-actions-26.3.log`).
5. Run `python3 ../docs/validate-jar.py <each-remapped-jar>` for the record.
6. Copy each jar into the matching `<mc>/` folder (or attach to a GitHub
   release) **only if step 2 was green**. Never rename a `-dev`/`-sources`
   jar into the release name.
7. Tag `v3.0.0` and attach the jars; list the exact MC/loader/API versions
   in the notes.

Do not tag or announce v3.0.0 until all four builds are green and the jars
validate.
