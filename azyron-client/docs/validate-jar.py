#!/usr/bin/env python3
"""AZYRON CLIENT V3 — jar validation (shared by CI and humans).

Usage: python3 validate-jar.py <path-to-remapped-jar>

Exits non-zero and prints a reason if the jar is not a plausible, complete
AZYRON CLIENT V3 mod jar. This checks structure and metadata only; it does
NOT prove the mod works in-game (see build-logs/README.md, step 4).
"""
import json
import sys
import zipfile

REQUIRED = [
    "fabric.mod.json",
    "azyronclient.client.mixins.json",
    "assets/azyronclient/icon.png",
    "com/azyron/client/client/AzyronCommonEntrypoint.class",
    "com/azyron/client/client/AzyronSharedInit.class",
    "com/azyron/client/client/AutoSprintHandler.class",
    "com/azyron/client/client/GameOptionsBridge.class",
    "com/azyron/client/client/hud/AzyronHudRenderer.class",
    "com/azyron/client/client/mixin/TitleScreenMixin.class",
    "com/azyron/client/client/screens/AzyronHomeScreen.class",
    "com/azyron/client/flavor/AzyronClientEntrypoint.class",
]


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    jar = sys.argv[1]
    print(f"Validating {jar}")
    with zipfile.ZipFile(jar) as zf:
        names = set(zf.namelist())
        for entry in REQUIRED:
            if entry not in names:
                print(f"FAIL: missing {entry}")
                return 1
        meta = json.loads(zf.read("fabric.mod.json"))
        if meta.get("id") != "azyronclient":
            print(f"FAIL: mod id {meta.get('id')!r}")
            return 1
        mixins = json.loads(zf.read("azyronclient.client.mixins.json"))
        if "TitleScreenMixin" not in mixins.get("client", []):
            print("FAIL: TitleScreenMixin not registered")
            return 1
        bad = zf.testzip()
        if bad is not None:
            print(f"FAIL: corrupt zip entry {bad}")
            return 1
    print(f"OK: {jar}")
    print(f"    mod id={meta['id']} version={meta['version']}")
    print(f"    depends={meta.get('depends')}")
    print(f"    environment={meta.get('environment')}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
