package com.freeletics.khonshu.navigation

import android.os.Parcelable
import androidx.savedstate.SavedStateWriter
import java.io.Serializable

internal actual fun SavedStateWriter.putPlatformValue(key: String, value: Any): Boolean {
    return when (value) {
        is Parcelable -> {
            putParcelable(key, value)
            true
        }
        is Serializable if canWriteToParcel(value) -> {
            putJavaSerializable(key, value)
            true
        }
        else -> false
    }
}

/**
 * A collection is [Serializable] while its elements might not be, so it can only be written as a
 * platform value when the platform supports all elements.
 */
internal fun canWriteToParcel(value: Any?): Boolean = when (value) {
    null -> true
    is Collection<*> -> value.all { canWriteToParcel(it) }
    is Array<*> -> value.all { canWriteToParcel(it) }
    is Map<*, *> -> value.all { canWriteToParcel(it.key) && canWriteToParcel(it.value) }
    else -> value is Parcelable || value is Serializable
}
