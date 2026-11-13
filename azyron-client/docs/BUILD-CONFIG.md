# AZYRON CLIENT V3 — build configuration notes (2026-11-13)

Everything below was cross-checked against upstream sources rather than
assumed. Read this before changing `build.gradle` or the CI workflow.

## Where the per-version settings live

Each `versions/<flavor>/build.gradle` carries its own `ext { }` block:
`mc_version`, `loader_version`, `fabric_api_version`, `java_release`,
`archive_name`, plus `version = '3.0.0'` and `group = 'com.azyron'`.

There are **no** `versions/*/gradle.properties` files, on purpose:

- Gradle's own best-practice guide says *"Do not place a `gradle.properties`
  file inside subprojects to configure your build"* — support is
  inconsistent, and the `plugins { id ... version "..." }` block resolves
  before a subproject property file is guaranteed to be in scope.
- Gradle 9.8.2 does load project-level property files
  (`ProjectPropertySettingBuildLoader` → `loadGradleProperties(ProjectIdentity, File)`),
  but only `version`, `group`, `status`, `buildDir` and `description` are
  mapped straight onto the `Project`; everything else lands in extra
  properties, which is the fragile path the docs warn about.

## Why the plugin ids differ

| Flavor | Plugin id | Mappings |
|--------|-----------|----------|
| `v1_21_11` | `net.fabricmc.fabric-loom-remap` | `mappings loom.officialMojangMappings()` |
| `v26_1/2/3` | `net.fabricmc.fabric-loom` | none (26.1+ is unobfuscated) |

This mirrors the official `fabric-example-mod` templates exactly (branch
`1.21.11` uses `fabric-loom-remap` + `modImplementation`, branches `26.1`,
`26.2`, `26.3` use `fabric-loom` + `implementation`). Loom 1.19-SNAPSHOT.

## Why client classes end up in the jar

All four projects call `loom { splitEnvironmentSourceSets() }` and add
`../../common/src/main/java` to `main` and `../../common/src/client/java`
to `client`. Loom's `MinecraftSourceSets.Split.evaluate()` wires the rest:

```java
project.getTasks().named(mainSourceSet.getJarTaskName(), Jar.class).configure(jar -> {
    jar.from(clientOnlySourceSet.getOutput().getClassesDirs());
    jar.from(clientOnlySourceSet.getOutput().getResourcesDir());
    ...
});
```

So `build/libs/*.jar` (the remapped jar) contains **both** the `main` and
`client` outputs. That is why the required-class list in
`docs/validate-jar.py` and in the CI workflow is valid, including
`com/azyron/client/flavor/AzyronClientEntrypoint.class` (a `client`
source-set class) and `com/azyron/client/client/hud/AzyronHudRenderer.class`.

The `main` source set compiles against the **common-only** Minecraft jar, so
`AzyronCommonEntrypoint` must not touch client classes. It only imports
`net.fabricmc.api.ModInitializer`, slf4j and `AzyronConstants` — keep it
that way.

## Dependency repositories

The `repositories { }` blocks are empty. Loom's `LoomRepositoryPlugin`
declares the Fabric maven, the Mojang libraries repo and Maven Central
itself, so `com.mojang:minecraft`, `net.fabricmc:fabric-loader` and
`net.fabricmc.fabric-api` all resolve without extra configuration.

## CI workflow notes

`.github/workflows/azyron-build.yml` runs a 4-way matrix
(`1.21.11`/Java 21, `26.1`/`26.2`/`26.3`/Java 25).

- `cache-dependency-path` lists **files**, not directories. `setup-java`
  hashes them via `glob.hashFiles()` and throws
  `No file in <cwd> matched to [...]` if nothing matches. (`actions/setup-java`
  `src/cache.ts`.)
- `set -o pipefail` is used with `tee` so a failed Gradle build fails the
  step instead of being masked by `tee`'s exit code.
- The jar validation step globs `versions/**/build/libs/*.jar` and skips
  `-sources` / `-dev` jars.
- A build failure means the sources or a pinned version is wrong — **do not**
  "fix" it by loosening the validator. Read the uploaded
  `azyron-build-log-<mc>` artifact.

## Version pins

| Flavor | Minecraft | Java | Fabric Loader | Fabric API | Archive |
|--------|-----------|------|---------------|------------|---------|
| `v1_21_11` | 1.21.11 | 21 | 0.20.1 | 0.141.6+1.21.11 | `azyron-client-1.21.11` |
| `v26_1` | 26.1 | 25 | 0.20.1 | 0.145.1+26.1 | `azyron-client-26.1` |
| `v26_2` | 26.2 | 25 | 0.20.1 | 0.161.0+26.2 | `azyron-client-26.2` |
| `v26_3` | 26.3 | 25 | 0.20.1 | 0.165.2+26.3 | `azyron-client-26.3` |

`build/libs` therefore contains `azyron-client-<mc>-3.0.0.jar`,
`-3.0.0-sources.jar` and, when Loom produces it, `-3.0.0-dev.jar`.
Ship only the plain jar.
