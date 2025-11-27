package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateWriter
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.SerializationStrategy

internal actual fun <T : Any> SavedStateWriter.put(
    key: String,
    value: T?,
    serializer: SerializationStrategy<T>?
) {
    when (value) {
        null -> putNull(key)
        is SavedState -> putSavedState(key, value)
        is String -> putString(key, value)
        is CharSequence -> putCharSequence(key, value)
        is Long -> putLong(key, value)
        is Int -> putInt(key, value)
        is Double -> putDouble(key, value)
        is Float -> putFloat(key, value)
        is Boolean -> putBoolean(key, value)
        is SavedStateHandle -> value.savedStateProvider().saveState()
        else -> {
            val serializer = requireNotNull(serializer) { "Did not find serializer for $value" }
            val savedState = encodeToSavedState(serializer, value)
            putSavedState(key, savedState)
        }
    }
}
