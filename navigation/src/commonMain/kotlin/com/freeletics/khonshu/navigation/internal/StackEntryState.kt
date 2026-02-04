package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateWriter
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlin.collections.getOrPut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer

@InternalNavigationApi
public class StackEntryState(initialState: Map<String, Any?>) {
    public constructor() : this(emptyMap())
    public constructor(savedState: SavedState) : this(savedState.read { toMap() })

    private val values: MutableMap<String, Any?> = initialState.toMutableMap()
    private val serializers: MutableMap<String, SerializationStrategy<*>?> = mutableMapOf()
    private val flows: MutableMap<String, MutableStateFlow<Any?>> = mutableMapOf()

    public operator fun contains(key: String): Boolean = key in values

    public inline fun <reified T : Any> getStateFlow(key: String, initialValue: T): StateFlow<T> {
        return getStateFlow(key, initialValue, serializer())
    }

    public fun <T : Any> getStateFlow(
        key: String,
        initialValue: T,
        strategy: KSerializer<T>?,
    ): StateFlow<T> {
        // If a flow exists we should just return it, and since it is a StateFlow and a value must
        // always be set, we know a value must already be available
        val flow = flows.getOrPut(key) {
            // If there is not a value associated with the key, add the initial value,
            // otherwise, use the one we already have.
            val initial = if (key !in values) {
                set(key, initialValue, strategy)
                initialValue
            } else {
                get(key, strategy)
            }
            MutableStateFlow(initial)
        }
        @Suppress("UNCHECKED_CAST") return flow.asStateFlow() as StateFlow<T>
    }

    public inline operator fun <reified T : Any> get(key: String): T? {
        return get(key, serializer())
    }

    public fun <T : Any> get(key: String, serializer: DeserializationStrategy<T>?): T? {
        val savedState = values[key]
        return if (savedState != null) {
            if (savedState is SavedState) {
                decodeFromSavedState(requireNotNull(serializer), savedState)
            } else {
                @Suppress("UNCHECKED_CAST")
                savedState as T?
            }
        } else {
            null
        }
    }

    public inline operator fun <reified T : Any> set(key: String, value: T?) {
        set(key, value, serializer())
    }

    public fun <T : Any> set(key: String, value: T?, serializationStrategy: SerializationStrategy<T>?) {
        values[key] = value
        serializers[key] = serializationStrategy
        flows[key]?.value = value
    }

    public fun <T : Any> remove(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        val latestValue = values.remove(key) as T?
        flows.remove(key)
        return latestValue
    }

    public fun savedStateHandle(): SavedStateHandle {
        return when (val current = values.get("khonshu-internal-saved-state-handle")) {
            is SavedStateHandle -> current
            is SavedState -> {
                val savedValues = current.read { toMap() }
                // noinspection VisibleForTests
                SavedStateHandle(savedValues).also {
                    values[KEY_SAVED_STATE_HANDLE] = it
                }
            }
            null -> {
                // noinspection VisibleForTests
                SavedStateHandle().also {
                    values[KEY_SAVED_STATE_HANDLE] = it
                }
            }
            else -> error("Unknown value $current for SavedStateHandle")
        }
    }

    internal fun saveState(): SavedState {
        return savedState {
            values.forEach { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                put(key, value, serializers[key] as SerializationStrategy<Any>?)
            }
        }
    }
}

private const val KEY_SAVED_STATE_HANDLE = "khonshu-internal-saved-state-handle"

private fun <T : Any> SavedStateWriter.put(key: String, value: T?, serializer: SerializationStrategy<T>?) {
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
        is SavedStateHandle -> {
            // noinspection RestrictedApi
            putSavedState(key, value.savedStateProvider().saveState())
        }
        else -> {
            if (!putPlatformValue(key, value)) {
                val serializer = requireNotNull(serializer) { "Did not find serializer for $value" }
                val savedState = encodeToSavedState(serializer, value)
                putSavedState(key, savedState)
            }
        }
    }
}

internal expect fun SavedStateWriter.putPlatformValue(key: String, value: Any): Boolean
