package com.chrisjenx.kinvoicing.compose

// Android uses BitmapFactory (not Skia). Running this test on
// androidUnitTest requires Robolectric, which the module doesn't ship
// today. Leaving as the 0L sentinel makes the test log the actual
// observed checksum and skip the equality assertion; once a baseline
// is recorded (instrumented test or Robolectric), update this value.
internal actual val expectedPixelChecksum: Long = 0L
