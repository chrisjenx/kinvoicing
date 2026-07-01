# Compose Compatibility Matrix — Coverage Fix

**Date:** 2026-06-30
**Status:** Approved design, ready for implementation plan
**Author:** Chris Jenkins (with Claude)

## Problem

The `Compose Compatibility` workflow (`.github/workflows/compatibility.yml`) is supposed to prove
that Kinvoicing builds and passes tests against each Compose Multiplatform / Kotlin version in
`.github/compose-versions.json` (currently `1.10.3`, `1.11.1`, `1.12.0-alpha02`).

It passed green on the matrix update PR (#11) even though `compose2pdf 1.1.2` was **incompatible
with Compose 1.12** — the exact class of failure the matrix exists to catch.

### Root cause (verified)

The `test` job only runs `:render-compose:*` tasks:

```
:render-compose:assemble :render-compose:jvmTest :render-compose:wasmJsNodeTest   # stable
:render-compose:jvmJar   :render-compose:jvmTest :render-compose:wasmJsNodeTest   # prerelease
:render-compose:iosSimulatorArm64Test                                             # macOS
```

`:render-compose` depends only on `:core` (`render-compose/build.gradle.kts:23`). The modules that
actually consume compose2pdf are:

- `:render-pdf` — `implementation(libs.compose2pdf)` (`render-pdf/build.gradle.kts:23`)
- `:render-html` — `api(libs.compose2pdf)` (`render-html/build.gradle.kts:19`)

Neither is ever compiled or tested by the matrix, so compose2pdf never lands on a Compose-1.12
classpath in CI. This is a **coverage gap**, not a silently-resolved version conflict — within any
single Gradle build there is exactly one Compose version (highest-wins conflict resolution), and the
matrix simply never puts compose2pdf into that build.

Secondary gap: the regular `build.yml` *does* exercise compose2pdf (`jvmTest`, fidelity) but always
at the default Compose `1.10.3`, never at the matrix versions. So no workflow runs
`compose2pdf × Compose 1.12`.

Note: the matrix `Override versions` step rewrites **both** `kotlin` and `compose-multiplatform` in
`libs.versions.toml`, so a matrix run exercises the Kotlin bump too — which affects every module,
including non-Compose ones like `:render-html-email`.

## Goal

Every **published** module is compiled and functionally tested against each Compose/Kotlin pair in
the matrix, so a compose2pdf-vs-Compose incompatibility (or a Kotlin-bump break) turns the matrix
**red** instead of passing green.

Published modules and their targets:

| Module | Plugin | Targets | Uses compose2pdf |
|--------|--------|---------|------------------|
| `:core` | KMP | jvm, android, ios, wasmJs, macos, linux, mingw | no |
| `:render-compose` | KMP | jvm, android, ios, wasmJs | no |
| `:render-html-email` | KMP | jvm, android, ios, wasmJs, macos, linux, mingw | no |
| `:render-pdf` | KMP | **jvm only** | **yes** |
| `:render-html` | kotlin.jvm | **jvm only** | **yes** |

## Design

### Change 1 — `.github/workflows/compatibility.yml`

Broaden the `test` job's task set from `:render-compose:*` to all published modules, **additively**:
keep the proven `:render-compose:assemble` / `:render-compose:jvmJar` compile line unchanged and
*add* the published modules' JVM / wasm / iOS **test** tasks.

Why additive rather than a top-level `assemble`: `:core` and `:render-html-email` declare
`macosX64`/`macosArm64`/`mingwX64` targets, which Kotlin/Native **cannot build on the Linux runner**.
A top-level or per-module `assemble` of those modules would fail on `ubuntu-latest`. The current
workflow only assembles `:render-compose` (jvm/android/ios/wasmJs — no macOS/mingw), so it never hits
this. Test tasks (`jvmTest`, `wasmJsNodeTest`, `iosSimulatorArm64Test`) only compile the target they
run on, all host-buildable on their respective runners — so extending the *test* set is safe.

**JVM leg (both OS):** run JVM tests for every published module (this is what exercises compose2pdf,
via `render-pdf`/`render-html`), keeping the existing single-module compile check:

```
# compile check (unchanged): :render-compose:assemble  (stable)  /  :render-compose:jvmJar  (prerelease)
:core:jvmTest :render-compose:jvmTest :render-html-email:jvmTest :render-pdf:jvmTest :render-html:test
```

**wasm leg (both OS):** extend to the wasm-capable published modules:

```
:core:wasmJsNodeTest :render-compose:wasmJsNodeTest :render-html-email:wasmJsNodeTest
```

**iOS leg (macOS only):** extend to the iOS-capable published modules:

```
:core:iosSimulatorArm64Test :render-compose:iosSimulatorArm64Test :render-html-email:iosSimulatorArm64Test
```

`render-pdf`/`render-html` are JVM-only, so they appear only in the JVM leg.

**Task-name rules** (verified against the build files):
- KMP-jvm modules (`core`, `render-compose`, `render-html-email`, `render-pdf`) → `:m:jvmTest`.
- `kotlin.jvm` module (`render-html`) → `:render-html:test`.

**Pinning:** the matrix does **not** override the `compose2pdf` version. It must test whatever
version actually ships, so a real incompatibility fails the build. (Highest-wins resolution upgrades
compose2pdf's transitive Compose deps to the matrix version — exactly the condition under test.)

**Path trigger:** extend the `pull_request.paths` list (currently `render-compose/**`, `build-logic/**`,
plus the version files) to also include `core/**`, `render-pdf/**`, `render-html/**`,
`render-html-email/**`, so edits to those modules re-run the matrix.

Update the step comment to reflect the broadened scope (still explains the Android-on-prerelease skip).

### Change 2 — `gradle/libs.versions.toml`

Bump `compose2pdf = "1.1.2"` → `"1.1.3"` (the release that fixes Compose 1.12). Confirmed live on
Maven Central (`<release>1.1.3`, POM 200), so it lands in the same PR — no propagation wait.

## Testing / verification (evidence, not assertion)

1. **Baseline (default Compose 1.10.3):** run the new JVM task set locally and confirm green:
   `./gradlew :render-compose:assemble :core:jvmTest :render-compose:jvmTest :render-html-email:jvmTest :render-pdf:jvmTest :render-html:test --no-configuration-cache`
2. **Failing-test proof the guard works:** with `compose-multiplatform` overridden to `1.12.0-alpha02`
   and `kotlin` to `2.4.0`, pin compose2pdf at the **old `1.1.2`** and run `:render-pdf:jvmTest`
   `:render-html:test` → expect FAILURE (proves detection). Then set compose2pdf `1.1.3` → expect green.
3. Confirm `compat-gate` still fails on any skipped/failed/cancelled leg (logic unchanged).

## Out of scope

- **Golden-image fidelity tests** (`:kinvoicing-fidelity-test:test`, `:fidelity-test:jvmTest`) stay in
  `build.yml` at the default Compose version. Running them across Compose versions would false-positive
  on pixel diffs from legitimate rendering changes, not real incompatibilities.
- Native leg (`macosArm64Test`, `linuxX64Test`, etc.) is not added to the matrix — those targets can't
  build on the Linux runner and only the iOS-sim leg runs on macOS. `build.yml` runs the native tests
  at default Compose.
- No change to `build.yml`, `release.yml`, `snapshot.yml`, or the versions JSON.

## Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Wrong task names (`jvmTest` vs `test`, `jvmJar` vs `jar`) | Rules pinned above from build files; baseline run in step 1 catches typos. |
| Prerelease pulls preview androidx needing newer compileSdk | Preserved: prereleases use per-module JVM tasks, never top-level `assemble`/Android. |
| Added iOS/wasm legs increase macOS runner time | Scoped to iOS/wasm-capable published modules only; `fail-fast: false` already isolates legs. |
| compose2pdf 1.1.3 has its own floor/ceiling on Compose | Baseline + matrix runs surface it; 1.1.3 was released specifically for 1.12. |
