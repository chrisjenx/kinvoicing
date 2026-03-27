package com.chrisjenx.kinvoicing.compose

internal actual fun <T> runBlockingCompat(block: suspend () -> T): T =
    throw UnsupportedOperationException(
        "runBlocking is not available on wasmJs. " +
            "Use the Compose renderer path (painterResource) instead of accessing ImageSource.bytes directly."
    )
