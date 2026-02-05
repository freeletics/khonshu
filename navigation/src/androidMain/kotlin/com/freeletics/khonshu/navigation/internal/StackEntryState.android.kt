package com.freeletics.khonshu.navigation.internal

import android.os.Parcelable
import androidx.savedstate.SavedStateWriter
import java.io.Serializable

internal actual fun SavedStateWriter.putPlatformValue(key: String, value: Any): Boolean {
    return when (value) {
        is Parcelable -> {
            putParcelable(key, value)
            true
        }
        is Serializable -> {
            putJavaSerializable(key, value)
            true
        }
        else -> false
    }
}
