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
