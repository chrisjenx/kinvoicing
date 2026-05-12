package com.chrisjenx.kinvoicing.compose

// Skia compiled to wasm. This same value is produced by *both* Node and
// Browser runtimes — same wasm binary, deterministic decode. That equality
// is the core safety net for the Karma→Node migration: an unintended
// runtime divergence would surface here as a checksum mismatch.
internal actual val expectedPixelChecksum: Long = -5336315741981768721L
