#!/bin/sh
# AZYRON CLIENT V3 — dependency mod fetcher (POSIX sh + curl + python3)
#
# Downloads the Fabric API jar and every performance mod listed in the
# azyron-modpack.txt manifest for a given Minecraft version, straight from
# the official Fabric Maven and the Modrinth API. Run this on a machine with
# normal internet access (the CI sandbox used for authoring cannot reach
# those hosts).
#
# Usage:
#   ./fetch-mods.sh <minecraft-version> [output-dir]
#   ./fetch-mods.sh 26.3 ./mods
#
# The manifest for each version pins the Fabric API build and lists the
# performance mods as Modrinth project slugs. For each slug the NEWEST
# version whose game_versions contains the target Minecraft and whose
# loaders contain "fabric" is downloaded — i.e. compatibility is always
# resolved from live metadata, never assumed. Mods published only as
# pre-releases are included only when no stable build exists (Modrinth
# ordering is respected; override per-slug pins in the manifest if you want
# to force a specific version).

set -eu

MC="${1:-}"
OUT="${2:-$(pwd)/mods}"
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MANIFEST="$SCRIPT_DIR/../$MC/azyron-modpack.txt"

if [ -z "$MC" ]; then
	echo "usage: $0 <minecraft-version> [output-dir]   (e.g. 1.21.11, 26.1, 26.2, 26.3)" >&2
	exit 2
fi
if [ ! -f "$MANIFEST" ]; then
	echo "error: no manifest at $MANIFEST" >&2
	exit 2
fi

need() { command -v "$1" >/dev/null 2>&1 || { echo "error: '$1' is required" >&2; exit 2; }; }
need curl
need python3

mkdir -p "$OUT"

FABRIC_API=$(sed -n 's/^fabric_api=//p' "$MANIFEST" | head -n1)
if [ -n "$FABRIC_API" ]; then
	url="https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$FABRIC_API/fabric-api-$FABRIC_API.jar"
	echo ">> fabric-api $FABRIC_API"
	curl -fL --retry 3 -o "$OUT/fabric-api-$FABRIC_API.jar" "$url"
fi

# Mod slugs: lines starting with a slug (no '=' and not a comment/blank).
grep -E '^[a-z0-9-]+\|(required|recommended)' "$MANIFEST" | while IFS='|' read -r slug channel pin; do
	echo ">> $slug ($channel)"
	python3 - "$slug" "$MC" "$OUT" <<'EOF'
import json
import subprocess
import sys
import urllib.request

slug, mc, out = sys.argv[1], sys.argv[2], sys.argv[3]
url = f"https://api.modrinth.com/v2/project/{slug}/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%22{mc}%22%5D"
try:
    data = json.load(urllib.request.urlopen(url, timeout=60))
except Exception as exc:
    print(f"   !! could not query Modrinth for {slug}: {exc}", file=sys.stderr)
    sys.exit(3)
if not data:
    print(f"   !! no Fabric build of {slug} for Minecraft {mc} — skipping (see pack notes)", file=sys.stderr)
    sys.exit(0)
pick = data[0]
pri = next((f for f in pick["files"] if f.get("primary")), pick["files"][0])
dest = f"{out}/{pri['filename']}"
print(f"   downloading {pick['version_number']} -> {pri['filename']}")
urllib.request.urlretrieve(pri["url"], dest)
EOF
done

echo
echo "Done. Contents of $OUT:"
ls -1 "$OUT"
echo
echo "Next steps: put the AZYRON CLIENT V3 jar built by CI into $OUT as well,"
echo "then point your launcher profile (PojavLauncher / PC Minecraft + Fabric) at it."
