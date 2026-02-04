package com.freeletics.khonshu.navigation.internal

import androidx.savedstate.SavedStateWriter

internal actual fun SavedStateWriter.putPlatformValue(key: String, value: Any): Boolean = false
