# Compose Compatibility Matrix Coverage — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the `Compose Compatibility` CI matrix actually exercise compose2pdf (and every other published module) against each Compose/Kotlin version, and bump compose2pdf to the Compose-1.12-compatible `1.1.3`.

**Architecture:** Two-file change. (1) `gradle/libs.versions.toml`: bump `compose2pdf` `1.1.2 → 1.1.3`. (2) `.github/workflows/compatibility.yml`: additively extend the `test` job so the JVM leg runs every published module's tests (including `render-pdf`/`render-html`, which consume compose2pdf), the wasm/iOS legs cover the multiplatform published modules, and the path trigger fires on all of them. The compose2pdf version is deliberately **not** overridden by the matrix, so it tests the version that ships.

**Tech Stack:** Kotlin Multiplatform, Gradle, GitHub Actions, compose2pdf, Compose Multiplatform.

## Global Constraints

Every task's requirements implicitly include these:

- `compose2pdf` must be pinned to a Maven-Central-published version. `1.1.3` confirmed live (`<release>1.1.3`, POM HTTP 200).
- The matrix must **NOT** override the `compose2pdf` version — it tests whatever ships.
- Prerelease Compose versions (compose-version string contains `-`) must **not** build the Android target (preview androidx metadata demands compileSdk 37, which the repo doesn't ship). Use `:render-compose:jvmJar`, never `:render-compose:assemble`, for prereleases.
- The Linux runner cannot build `macosX64`/`macosArm64`/`mingwX64`/`ios*` native targets. Only add **test** tasks (`jvmTest`/`wasmJsNodeTest`/`iosSimulatorArm64Test`), which compile just the host-buildable target they run on. Do **not** introduce `:core:assemble` / `:render-html-email:assemble` / top-level `assemble`.
- Exact Gradle task names: KMP-jvm modules (`core`, `render-compose`, `render-html-email`, `render-pdf`) use `:m:jvmTest`; the `kotlin.jvm` module `render-html` uses `:render-html:test`.
- Golden-image fidelity modules (`kinvoicing-fidelity-test`, `fidelity-test`) are **out of scope** for the matrix (pixel diffs false-positive across Compose versions).
- Commit messages end with:
  `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`
- Work happens on branch `worktree-compat-matrix-coverage` (already checked out in the worktree). Do not push or open a PR until Task 3, which is gated on the user's go-ahead.

---

### Task 1: Prove the guard catches the 1.12 break, then bump compose2pdf

This is the TDD core: demonstrate that `render-pdf`/`render-html` JVM tests **fail** at Compose 1.12 with the old compose2pdf `1.1.2` (proving the tests we're about to add to CI actually detect the incompatibility), then make them pass by bumping to `1.1.3`.

**Files:**
- Modify: `gradle/libs.versions.toml` (line `compose2pdf = "1.1.2"`)

**Interfaces:**
- Consumes: nothing (first task).
- Produces: `gradle/libs.versions.toml` with `compose2pdf = "1.1.3"` and `kotlin`/`compose-multiplatform` unchanged at their committed defaults (`2.3.20` / `1.10.3`). Task 2 relies only on this file being back at default Compose/Kotlin.

> ⚠️ Steps 1–3 download Kotlin `2.4.0` and Compose `1.12.0-alpha02` artifacts — first run is network- and time-heavy (several minutes). This is local evidence; the authoritative proof is the CI matrix in Task 3.

- [ ] **Step 1: Reproduce the failure — Compose 1.12 with the OLD compose2pdf 1.1.2**

Temporarily override Kotlin + Compose to the 1.12 matrix entry, leaving `compose2pdf` at `1.1.2`, then run the two compose2pdf-consuming test tasks:

```bash
perl -i -pe '
  s/^kotlin = ".*"/kotlin = "2.4.0"/;
  s/^compose-multiplatform = ".*"/compose-multiplatform = "1.12.0-alpha02"/;
' gradle/libs.versions.toml
grep -E '^(kotlin|compose-multiplatform|compose2pdf) = ' gradle/libs.versions.toml
./gradlew :render-pdf:jvmTest :render-html:test --no-configuration-cache
```

- [ ] **Step 2: Confirm it fails (this is the point of the task)**

Expected: BUILD FAILED. The failure is a compile error or a runtime `NoSuchMethodError`/`AbstractMethodError`/`NoClassDefFoundError` (or Skia/`renderToPdf` error) from compose2pdf `1.1.2` running against Compose `1.12.0-alpha02`.

> If the build unexpectedly **PASSES**, STOP. The proposed guard (`render-pdf`/`render-html` JVM tests) does not exercise the broken code path, so it would not have caught the reported bug. Escalate before continuing: the matrix would then also need a real render exercise (e.g. `:kinvoicing-fidelity-test:test`), and the spec's "out of scope" decision must be revisited with the user.

- [ ] **Step 3: Apply the fix — bump compose2pdf to 1.1.3, still under the 1.12 override**

```bash
perl -i -pe 's/^compose2pdf = ".*"/compose2pdf = "1.1.3"/;' gradle/libs.versions.toml
./gradlew :render-pdf:jvmTest :render-html:test --no-configuration-cache
```

Expected: BUILD SUCCESSFUL. Proves `compose2pdf 1.1.3` is compatible with Compose `1.12.0-alpha02`.

- [ ] **Step 4: Restore Kotlin/Compose to defaults, keeping only the compose2pdf bump**

```bash
perl -i -pe '
  s/^kotlin = ".*"/kotlin = "2.3.20"/;
  s/^compose-multiplatform = ".*"/compose-multiplatform = "1.10.3"/;
  s/^compose2pdf = ".*"/compose2pdf = "1.1.3"/;
' gradle/libs.versions.toml
git diff gradle/libs.versions.toml
```

Expected `git diff`: exactly ONE changed line — `compose2pdf = "1.1.2"` → `compose2pdf = "1.1.3"`. Kotlin and compose-multiplatform must be back at `2.3.20` / `1.10.3`.

- [ ] **Step 5: Baseline at default Compose — validates the JVM task set Task 2 will wire into CI**

```bash
./gradlew :render-compose:assemble :core:jvmTest :render-compose:jvmTest :render-html-email:jvmTest :render-pdf:jvmTest :render-html:test --no-configuration-cache
```

Expected: BUILD SUCCESSFUL, and no "Task ... not found" — confirms every JVM task name is correct at the default (1.10.3 / 2.3.20) toolchain with compose2pdf `1.1.3`.

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "$(cat <<'EOF'
build: bump compose2pdf 1.1.2 -> 1.1.3 for Compose 1.12 compatibility

Verified locally: render-pdf/render-html JVM tests fail with 1.1.2 against
Compose 1.12.0-alpha02 and pass with 1.1.3.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: Broaden the compatibility matrix to all published modules

**Files:**
- Modify: `.github/workflows/compatibility.yml` (paths block lines 5–10; `Assemble + JVM/wasmJs tests` step lines 72–82; `iOS simulator tests` step lines 84–86)

**Interfaces:**
- Consumes: `gradle/libs.versions.toml` at default Compose/Kotlin with compose2pdf `1.1.3` (Task 1).
- Produces: a `test` job that runs, per matrix version — JVM tests for all 5 published modules, wasm tests for the 3 wasm-capable modules, and (on macOS) iOS-sim tests for the 3 iOS-capable modules.

- [ ] **Step 1: Extend the `pull_request.paths` trigger**

Replace the `paths:` block (lines 5–10) with:

```yaml
    paths:
      - '.github/compose-versions.json'
      - '.github/workflows/compatibility.yml'
      - 'gradle/libs.versions.toml'
      - 'core/**'
      - 'render-compose/**'
      - 'render-html-email/**'
      - 'render-pdf/**'
      - 'render-html/**'
      - 'build-logic/**'
```

- [ ] **Step 2: Replace the `Assemble + JVM/wasmJs tests` step with the broadened JVM + wasm legs**

Replace the whole step (lines 72–82) with:

```yaml
      - name: Build + test published modules (JVM/wasmJs)
        # Avoid Android testDebugUnitTest (BitmapFactory needs Robolectric).
        # Prerelease CMP pulls preview androidx artifacts whose AAR metadata demands
        # a compileSdk we don't ship yet (1.12 alphas need 37) — so the compile check
        # assembles render-compose on stable but only jvmJar on prereleases (no Android).
        # JVM tests cover every published module: render-pdf/render-html pull in
        # compose2pdf, so this is what actually exercises compose2pdf at the matrix version.
        run: |
          # Compile check for the Compose UI module (its Android target too, on stable).
          COMPILE=(:render-compose:assemble)
          if [[ "${{ matrix.version.compose-version }}" == *-* ]]; then
            COMPILE=(:render-compose:jvmJar)
          fi
          # JVM tests: every published module (render-pdf/render-html use compose2pdf).
          JVM=(:core:jvmTest :render-compose:jvmTest :render-html-email:jvmTest :render-pdf:jvmTest :render-html:test)
          # wasmJs tests: the wasm-capable published modules.
          WASM=(:core:wasmJsNodeTest :render-compose:wasmJsNodeTest :render-html-email:wasmJsNodeTest)
          ./gradlew "${COMPILE[@]}" "${JVM[@]}" "${WASM[@]}" --no-configuration-cache
```

- [ ] **Step 3: Replace the `iOS simulator tests` step to cover the iOS-capable published modules**

Replace the step (lines 84–86) with:

```yaml
      - name: iOS simulator tests
        if: matrix.os == 'macos-latest'
        run: ./gradlew :core:iosSimulatorArm64Test :render-compose:iosSimulatorArm64Test :render-html-email:iosSimulatorArm64Test --no-configuration-cache
```

- [ ] **Step 4: Validate every task name resolves (no execution)**

```bash
./gradlew :render-compose:assemble :core:jvmTest :render-compose:jvmTest :render-html-email:jvmTest :render-pdf:jvmTest :render-html:test :core:wasmJsNodeTest :render-compose:wasmJsNodeTest :render-html-email:wasmJsNodeTest --dry-run
./gradlew :core:iosSimulatorArm64Test :render-compose:iosSimulatorArm64Test :render-html-email:iosSimulatorArm64Test --dry-run
```

Expected: both print a task execution plan and exit 0. Any "Task ... not found in project" means a task-name typo — fix it before committing.

- [ ] **Step 5: Validate the workflow YAML parses**

```bash
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/compatibility.yml')); print('YAML ok')"
```

Expected: `YAML ok`.

- [ ] **Step 6: Commit**

```bash
git add .github/workflows/compatibility.yml
git commit -m "$(cat <<'EOF'
ci: run the compat matrix against all published modules

The matrix only ran :render-compose:*, which never loads compose2pdf, so
the compose2pdf-vs-Compose-1.12 incompatibility passed green. Extend the
JVM leg to every published module (render-pdf/render-html exercise
compose2pdf), the wasm/iOS legs to the multiplatform published modules,
and the path trigger to match. compose2pdf is left un-overridden so the
matrix tests the shipped version.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: Push, open PR, and confirm the matrix goes green end-to-end (gated on user go-ahead)

This is the authoritative verification: only CI runs the full multi-version × 2-OS matrix with the real iOS/wasm legs. **Do not run this task until the user confirms they want the branch pushed and a PR opened.**

**Files:** none (delivery only).

- [ ] **Step 1: Push the branch**

```bash
git push -u origin worktree-compat-matrix-coverage
```

- [ ] **Step 2: Open the PR**

```bash
gh pr create --fill --title "ci: close compat-matrix coverage gap + bump compose2pdf to 1.1.3" --body "$(cat <<'EOF'
## Why

The Compose Compatibility matrix only ran `:render-compose:*` tasks, which never load
compose2pdf. So compose2pdf's incompatibility with Compose 1.12 passed the matrix green
(see the discussion on #11). This broadens the matrix to every published module and bumps
compose2pdf to `1.1.3` (the release that fixes Compose 1.12).

## Changes

- `gradle/libs.versions.toml`: compose2pdf `1.1.2` → `1.1.3`.
- `.github/workflows/compatibility.yml`: JVM leg now tests all 5 published modules
  (`render-pdf`/`render-html` exercise compose2pdf); wasm + iOS legs cover the multiplatform
  published modules; path trigger extended. compose2pdf is intentionally not overridden.

## Verification

Locally: `render-pdf`/`render-html` JVM tests FAIL with compose2pdf 1.1.2 against Compose
1.12.0-alpha02 and PASS with 1.1.3.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

- [ ] **Step 3: Watch the Compose Compatibility matrix**

```bash
gh pr checks --watch
```

Expected: all `CMP <version> / <os>` legs pass, **including `CMP 1.12.0-alpha02`** on both OSes, and `Compose Compatibility CI Gate` is green.

- [ ] **Step 4: Confirm the new tasks actually ran (not silently skipped)**

Open the `CMP 1.12.0-alpha02 / ubuntu-latest` job log and confirm the `Build + test published modules (JVM/wasmJs)` step shows `:render-pdf:jvmTest` and `:render-html:test` executing:

```bash
gh run view --log --job "$(gh pr checks --json name,link -q '.[] | select(.name=="CMP 1.12.0-alpha02 / ubuntu-latest") | .link' | grep -oE '[0-9]+$')" 2>/dev/null | grep -E ':render-pdf:jvmTest|:render-html:test' | head
```

Expected: lines showing both tasks ran (`> Task :render-pdf:jvmTest` / `> Task :render-html:test`).

---

## Self-Review

**1. Spec coverage:**
- Change 1 (broaden matrix: JVM/wasm/iOS legs + paths, no compose2pdf override) → Task 2 (all steps). ✅
- Change 2 (bump compose2pdf 1.1.2 → 1.1.3) → Task 1 Steps 3–6. ✅
- Testing step 1 (baseline default Compose) → Task 1 Step 5. ✅
- Testing step 2 (failing-then-passing at 1.12) → Task 1 Steps 1–3. ✅
- Testing step 3 (compat-gate unchanged) → gate logic is untouched; verified green in Task 3 Step 3. ✅
- Out-of-scope (golden fidelity, native leg) → honored: neither appears in any task; stated in Global Constraints. ✅

**2. Placeholder scan:** No TBD/TODO/"handle edge cases"/"similar to Task N". Every code step shows the exact command or YAML. ✅

**3. Type/name consistency:** Task names identical across Task 1 Step 5, Task 2 Steps 2/4, and Global Constraints (`:render-pdf:jvmTest`, `:render-html:test`, `:core:jvmTest`, `:render-compose:jvmTest`, `:render-html-email:jvmTest`, `:*:wasmJsNodeTest`, `:*:iosSimulatorArm64Test`, `:render-compose:assemble`/`jvmJar`). `compose2pdf` version string `1.1.3` consistent throughout. ✅
