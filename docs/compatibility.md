---
title: Compatibility
layout: default
nav_order: 3
---

# Compatibility

[![Compose Compatibility](https://github.com/chrisjenx/kinvoicing/actions/workflows/compatibility.yml/badge.svg)](https://github.com/chrisjenx/kinvoicing/actions/workflows/compatibility.yml)

Kinvoicing is tested weekly against the 3 most recent Compose Multiplatform releases on macOS and Linux.

---

## Compose Multiplatform versions

<!-- BEGIN cmp-matrix -->
| Compose Multiplatform | Kotlin | Status |
|:----------------------|:-------|:-------|
| 1.12.0-alpha01 | 2.4.0 | CI tested |
| **1.11.1** | 2.4.0 | CI tested (current) |
| 1.10.3 | 2.4.0 | CI tested |
<!-- END cmp-matrix -->

{: .note }
This table is generated from [`.github/compose-versions.json`](https://github.com/chrisjenx/kinvoicing/blob/main/.github/compose-versions.json) and updated weekly by the [update-compose-versions workflow](https://github.com/chrisjenx/kinvoicing/actions/workflows/update-compose-versions.yml), which discovers the 3 most recent CMP releases and runs the compatibility suite against each. The **current** row is the version the library itself pins. Edit `compose-versions.json`, not this table.

---

## What is tested per cell

Kinvoicing publishes several artifacts, so "Compose compatibility" means different things per module:

| Module | How it's exercised against each cell |
|:-------|:-------------------------------------|
| `:render-html` | **Tested directly** — its own reflective Compose scene driver renders through the cell's Compose/Skiko runtime. |
| `:render-pdf` | **Tested via [compose2pdf](https://github.com/chrisjenx/compose2pdf)** — compose2pdf's reflective scene driver drives the render, so it inherits compose2pdf's own compatibility surface. |
| `:render-compose` | **Source-recompiled per cell** — the shipped binary is republished, then a standalone consumer forces the Compose group to the cell version, exercising the normal KMP consumer contract (Compose is an `api` dependency, resolved by the consumer). |
| `:core`, `:render-html-email` | **Compose-independent** — neither declares any `org.jetbrains.compose` or `skiko` dependency, so no Compose version can affect them. They are not part of the Compose matrix. |

---

## How compatibility is tested

The [compatibility workflow](https://github.com/chrisjenx/kinvoicing/actions/workflows/compatibility.yml) runs on pull requests touching the Compose renderers, weekly on Monday at 9am UTC, and on demand.

It loads the Compose Multiplatform versions from [`.github/compose-versions.json`](https://github.com/chrisjenx/kinvoicing/blob/main/.github/compose-versions.json), publishes the shipped renderer binaries at the pinned base version, then overrides the library's pinned Compose/Kotlin to each cell and runs `:render-compose:assemble`, `:render-compose:jvmTest`, `:render-compose:wasmJsNodeTest` (plus `iosSimulatorArm64Test` on macOS) and consumes the shipped renderers on the cell's runtime.

The [update-compose-versions workflow](https://github.com/chrisjenx/kinvoicing/actions/workflows/update-compose-versions.yml) automatically discovers new CMP releases weekly, bumps the shipped build base to the latest stable, regenerates this table, and opens a PR.

---

## See also

- [Home]({{ site.baseurl }}/) -- Overview and installation
