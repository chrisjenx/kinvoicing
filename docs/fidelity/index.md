---
title: Fidelity Testing
layout: default
nav_order: 6
---

# Cross-Renderer Fidelity Testing

Kinvoicing renders the same invoice IR through four different renderers: Compose UI, PDF, print HTML, and email HTML. Automated fidelity tests verify that all renderers produce visually consistent output.

## How It Works

1. Each test fixture is rendered through all four renderers
2. Compose output is used as the **reference image**
3. PDF and HTML outputs are rasterized to images at matching dimensions
4. Images are compared pixel-by-pixel using two metrics:
   - **RMSE** (Root Mean Square Error) — measures pixel-level color differences, normalized 0.0–1.0
   - **SSIM** (Structural Similarity Index) — measures perceptual similarity, 0.0–1.0 (1.0 = identical)
5. Visual diff images are generated with 10x amplification of pixel differences

## Pass/Warn/Fail Thresholds

| Renderer | Pass | Warn | Fail |
|----------|------|------|------|
| Vector PDF | RMSE ≤ 0.05 | RMSE ≤ threshold | RMSE > threshold |
| Print HTML | RMSE ≤ 0.02 | RMSE ≤ threshold | RMSE > threshold |
| Email HTML | RMSE ≤ 0.10 | RMSE ≤ threshold | RMSE > threshold |

Email HTML has a wider tolerance because it uses table-based layout with inline styles, which produces different spacing and rendering compared to the Compose reference.

## Test Fixtures

The fidelity suite tests a wide range of invoice configurations across categories:

- **basic** — minimal and standard invoices
- **line-items** — sub-items, custom columns, many rows
- **adjustments** — tax, discount, fee, credit combinations
- **branding** — logos, dual branding, stacked layouts
- **sections** — all 9 section types in various configurations
- **style** — themes, accent borders, grid lines, custom colors
- **composite** — full invoices combining multiple features
- **stress** — large invoices with many items and sections

## Running Locally

```bash
# Run the full fidelity test suite (JVM only)
./gradlew :kinvoicing-fidelity-test:test

# Open the generated HTML report
open kinvoicing-fidelity-test/build/reports/fidelity/index.html
```

The report includes side-by-side thumbnails of each renderer's output, diff images, and per-fixture metrics with filtering and sorting.

{: .note }
HTML fidelity tests require Playwright. Install with `npx playwright install chromium`. Tests skip gracefully if Playwright is unavailable — PDF fidelity tests always run.

## Report Contents

The generated report at `kinvoicing-fidelity-test/build/reports/fidelity/index.html` includes:

- **Summary badges** — pass/warn/fail counts and mean metrics across all fixtures
- **Per-fixture rows** — Compose reference, PDF, HTML, and email HTML thumbnails
- **Diff images** — visual diffs with amplified pixel differences
- **Metrics** — RMSE, SSIM, exact match percentage, and max error per fixture
- **Filtering** — filter by category or status, sort by name or worst metric
