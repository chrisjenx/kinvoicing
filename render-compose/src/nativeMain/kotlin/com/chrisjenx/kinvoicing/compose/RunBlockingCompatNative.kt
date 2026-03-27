package com.chrisjenx.kinvoicing.compose

import kotlinx.coroutines.runBlocking

internal actual fun <T> runBlockingCompat(block: suspend () -> T): T = runBlocking { block() }
