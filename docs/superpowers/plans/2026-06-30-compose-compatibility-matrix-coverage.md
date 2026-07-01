# Compose Multi-CMP Support (Reflective Driver) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Make kinvoicing's PDF/HTML renderers work across the CMP compatibility matrix (incl. 1.12), and document + test which CMP/Kotlin versions are supported — mirroring compose2pdf 1.2.0's single-binary reflective-driver approach.

**Architecture:** Port compose2pdf 1.2.0's reflective `ComposeSceneRenderer` into `render-html` (the only kinvoicing code calling `CanvasLayersComposeScene` directly); `render-pdf` inherits multi-CMP via a compose2pdf 1.2.0 bump; a standalone `compat-consumer` build proves the *shipped* binaries against each CMP cell; a generated table documents supported versions.

**Tech Stack:** Kotlin Multiplatform / JVM, Gradle, compose2pdf 1.2.0, Compose Multiplatform, GitHub Actions, Python (docs generator).

## Global Constraints

- compose2pdf pinned to `1.2.0` (live on Maven Central; the single-binary reflective build).
- The matrix must NOT override the compose2pdf version — test what ships.
- Prerelease cells (compose-version contains `-`) follow the enforcement decision in Task 6; today they are `continue-on-error`. Do not claim "all green" while that holds.
- `render-html` reflective driver = near-verbatim port of `compose2pdf/src/main/kotlin/com/chrisjenx/compose2pdf/internal/ComposeSceneRenderer.kt` (clone at `/Users/christopherjenkins/.claude-work/jobs/2e852706/tmp/compose2pdf`), package `com.chrisjenx.kinvoicing.html.internal`, with a render-html-local exception and the `PlatformContext.Empty` fail-loud hardening. Do NOT copy compose2pdf's raw-exception fail paths verbatim.
- Base build versions align to compose2pdf's: CMP `1.11.1`, Kotlin `2.4.0`. The Kotlin bump is gated on `:build-logic:wasmjs-node-compose:test` + `:render-compose:wasmJsNodeTest` staying green (KGP-version-sensitive reflective plugin).
- `render-compose` stays source-recompiled per cell (KMP klib; keep the existing `perl`-override legs).
- Commit messages end with: `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`
- Do not push / open a PR until explicitly authorized.

---

### Task 1: render-html reflective scene driver  — IN PROGRESS (dispatched)

**Files:** Create `render-html/src/main/kotlin/com/chrisjenx/kinvoicing/html/internal/ComposeSceneRenderer.kt`; modify `.../internal/ComposeToSvg.kt`; create `render-html/src/test/kotlin/.../internal/ComposeSceneRendererTest.kt`.

**Deliverable:** render-html compiles+renders SVG on the current base AND under a CMP 1.12.0-alpha01 / Kotlin 2.4.0 + compose2pdf 1.2.0 override (RED→GREEN). No version-catalog change committed in this task.

Verification (must be evidenced in the task report): current-base `:render-html:test` green; override `:render-html:test :render-pdf:jvmTest` green (was RED before the driver); `git diff gradle/libs.versions.toml` empty at commit.

---

### Task 2: Align build base to compose2pdf 1.2.0 (version bump + wasmJs gate)

**Files:** Modify `gradle/libs.versions.toml`.

**Interfaces:** Consumes Task 1's committed driver (so render-html compiles on the new base). Produces a catalog with `compose2pdf=1.2.0`, `compose-multiplatform=1.11.1`, `kotlin=2.4.0`.

- [ ] **Step 1: Bump the three versions**

```bash
perl -i -pe '
  s/^compose2pdf = ".*"/compose2pdf = "1.2.0"/;
  s/^compose-multiplatform = ".*"/compose-multiplatform = "1.11.1"/;
  s/^kotlin = ".*"/kotlin = "2.4.0"/;
' gradle/libs.versions.toml
grep -E '^(kotlin|compose-multiplatform|compose2pdf) = ' gradle/libs.versions.toml
```

- [ ] **Step 2: Gate — the wasmJs Node plugin must survive Kotlin 2.4.0**

```bash
./gradlew :build-logic:wasmjs-node-compose:test :render-compose:wasmJsNodeTest --no-configuration-cache
```
Expected: PASS. If it FAILS, STOP and report BLOCKED — the plugin reaches `KotlinJsTest.nodeJsArgs` reflectively and may need a KGP-2.4.0 fix before the base bump can land.

- [ ] **Step 3: Full base build + tests green on the new base**

```bash
./gradlew build --no-configuration-cache
```
Expected: PASS (all modules, including `:render-html:test` and `:render-pdf:jvmTest`, now on CMP 1.11.1 / Kotlin 2.4.0 with compose2pdf 1.2.0).

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "$(cat <<'EOF'
build: bump compose2pdf 1.2.0 + align base to CMP 1.11.1 / Kotlin 2.4.0

compose2pdf 1.2.0 is the single-binary reflective build; render-pdf inherits
CMP 1.12 compatibility through it. Base aligned to compose2pdf's build base.
Verified wasmjs-node-compose plugin + wasmJsNodeTest survive Kotlin 2.4.0.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: `compat-consumer` publish-then-consume harness

**Files:** Create `compat-consumer/settings.gradle.kts`, `compat-consumer/build.gradle.kts`, `compat-consumer/gradle.properties`, `compat-consumer/src/main/kotlin/com/chrisjenx/compat/Smoke.kt`. (Standalone build — NOT added to root `settings.gradle.kts`.)

**Interfaces:** Consumes published `render-html`/`render-pdf` jars (fixed version `0.0.0-compat`). Produces a `run`-able consumer forcing `org.jetbrains.compose*` to `-PcomposeVersion`.

- [ ] **Step 1: Decide + wire the publish version.** Use `-Pversion=0.0.0-compat` on the publish tasks and hardcode `0.0.0-compat` as the consumed version in `compat-consumer/build.gradle.kts` (kinvoicing has no `gradle.properties version=` SSOT). Read compose2pdf's `compat-consumer/build.gradle.kts` (clone) as the template for `resolutionStrategy.eachDependency { if (requested.group.startsWith("org.jetbrains.compose")) useVersion(composeVersion) }` and the `-PcomposeVersion`/`-PkotlinVersion`/`mavenLocal()` wiring.

- [ ] **Step 2: `Smoke.kt`** — build an inline `InvoiceDocument` (render-html/pdf don't depend on core's fixtures), then:
  - `com.chrisjenx.kinvoicing.pdf.PdfRenderer().render(doc)` → assert first 5 bytes `== "%PDF-"` and `size > 100`;
  - the render-html entry (`renderToHtml { … }`) → assert the result contains `"<svg"`.
  Both drive the reflective paths; a wrong dispatch throws → non-zero exit.

- [ ] **Step 3: Local smoke against the current cell**

```bash
./gradlew :render-html:publishToMavenLocal :render-pdf:publishToMavenLocal -Pversion=0.0.0-compat
./gradlew -p compat-consumer run -PcomposeVersion=1.11.1 -PkotlinVersion=2.4.0 -PrenderHtmlVersion=0.0.0-compat -PrenderPdfVersion=0.0.0-compat
```
Expected: PASS. Then repeat with `-PcomposeVersion=1.12.0-alpha01` → PASS (proves the shipped binaries, not a recompile).

- [ ] **Step 4: Commit** (`ci: add compat-consumer publish-then-consume harness`).

---

### Task 4: Rewrite the compatibility matrix CI

**Files:** Modify `.github/workflows/compatibility.yml`.

- [ ] **Step 1: JVM renderers via publish-then-consume.** Per matrix cell: `./gradlew :render-html:publishToMavenLocal :render-pdf:publishToMavenLocal -Pversion=0.0.0-compat` (built at pinned base), then `xvfb-run ./gradlew -p compat-consumer run -PcomposeVersion=<cell> -PkotlinVersion=<cell> -PrenderHtmlVersion=0.0.0-compat -PrenderPdfVersion=0.0.0-compat` (no `xvfb-run` on macOS).
- [ ] **Step 2: Keep render-compose recompile legs** — retain the existing `perl`-override + `:render-compose:jvmTest`/`wasmJsNodeTest`/`iosSimulatorArm64Test` (KMP klib can't be reflectively bridged). Keep `continue-on-error: ${{ contains(matrix.version.compose-version, '-') }}` on the smoke step only. Keep `compat-gate`.
- [ ] **Step 3: Extend `paths:`** — add `render-html/**`, `render-pdf/**`, `compat-consumer/**` (keep `render-compose/**`, `build-logic/**`, version files).
- [ ] **Step 4: Validate** — `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/compatibility.yml')); print('ok')"`.
- [ ] **Step 5: Commit** (`ci: matrix tests the shipped JVM-renderer binaries per CMP cell`).

---

### Task 5: Generated version-support docs

**Files:** Create `.github/scripts/render-compat-tables.py`; create `docs/compatibility.md`; modify `README.md`; add a `docs-sync` job to `.github/workflows/build.yml`; extend `.github/workflows/update-compose-versions.yml`.

- [ ] **Step 1: Verify the scope claim first** — `grep -rE "org.jetbrains.compose|skiko" core/build.gradle.kts render-html-email/build.gradle.kts`. If empty, the "Compose-independent" renderer-scope note is true; if not, revisit before documenting.
- [ ] **Step 2: Port the generator** from compose2pdf's `.github/scripts/render-compat-tables.py` (clone): read `.github/compose-versions.json` + pinned `compose-multiplatform`/`kotlin` from `libs.versions.toml`, sort descending (prerelease-aware), emit `| Compose Multiplatform | Kotlin | Status |` (pinned row bolded `(current)`), DOTALL-replace between `<!-- BEGIN cmp-matrix -->`/`<!-- END cmp-matrix -->`; `--check` exits non-zero on drift.
- [ ] **Step 3: `docs/compatibility.md`** — Jekyll page (front-matter `nav_order`), CI badge, the marker block, an "edit the JSON not this table" note, and the **renderer-scope note** (render-html direct, render-pdf via compose2pdf, render-compose recompiled-per-cell, core/render-html-email Compose-free). `README.md` gets a `## Compatibility` section with the same marker block.
- [ ] **Step 4: `docs-sync` gate** — add `python3 .github/scripts/render-compat-tables.py --check` to `build.yml`.
- [ ] **Step 5: Extend `update-compose-versions.yml`** — after writing the JSON, `perl -i` bump `compose-multiplatform`/`kotlin` to the highest **stable** detected, run the generator (non-`--check`), and widen the change-gate to fire on JSON/catalog/docs diffs.
- [ ] **Step 6: Run the generator, verify `--check` clean, commit** (`docs: generated CMP/Kotlin support table + docs-sync gate`).

---

### Task 6: Prerelease enforcement, JSON resync, CLAUDE.md

> **Status: COMPLETE** — all steps shipped (commit `4bc8672`; review fixes `bcb545b`). Prerelease kept non-blocking (matches compose2pdf); JSON resynced `alpha01`→`beta01`; CLAUDE.md rewritten. Steps 1–4 below are done.

**Files:** Modify `.github/compose-versions.json`, `.github/workflows/compatibility.yml`, `CLAUDE.md`.

- [ ] **Step 1: Prerelease decision** — with the driver + compose2pdf 1.2.0 in place, either drop `continue-on-error` for the 1.12 cell (enforce green) or keep it and label the docs "newest prerelease = best-effort". Do not leave the docs claiming "all green" with `continue-on-error` still on.
- [ ] **Step 2: Resync the prerelease id** — kinvoicing's JSON has `1.12.0-alpha01`; compose2pdf tracks `1.12.0-beta01`. Update to the current beta (or confirm the weekly updater will), keeping Kotlin `2.4.0`.
- [ ] **Step 3: Update `CLAUDE.md`** — rewrite the "Compose compatibility matrix" bullet to describe render-html's reflective driver, the publish-then-consume `compat-consumer`, the docs generator + markers, and render-pdf's inherited compatibility via compose2pdf.
- [ ] **Step 4: Commit** (`ci+docs: enforce/label 1.12 cell, resync prerelease id, update CLAUDE.md`).

---

## Self-Review

**Spec coverage:** driver → Task 1; render-pdf inheritance + base bump → Task 2; publish-then-consume testing → Task 3; matrix rewrite → Task 4; generated docs + scope-verify → Task 5; prerelease enforcement + resync + memory → Task 6. Every spec section maps to a task. ✅

**Placeholder scan:** no TBD/TODO; each step has an exact command, file, or explicit reference to the compose2pdf source to port. ✅

**Consistency:** task names (`:render-html:test`, `:render-pdf:jvmTest`, `:build-logic:wasmjs-node-compose:test`, `:render-compose:wasmJsNodeTest`, publish `-Pversion=0.0.0-compat`) and versions (`compose2pdf 1.2.0`, CMP `1.11.1`, Kotlin `2.4.0`) are consistent across tasks and the Global Constraints. ✅
