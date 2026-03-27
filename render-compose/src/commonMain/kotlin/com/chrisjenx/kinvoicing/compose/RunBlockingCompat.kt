package com.chrisjenx.kinvoicing.compose

internal expect fun <T> runBlockingCompat(block: suspend () -> T): T
