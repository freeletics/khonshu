package com.freeletics.khonshu.navigation.internal

import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import com.freeletics.khonshu.navigation.internal.putPlatformValue
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import org.junit.Test

class StackEntryStateTest {
    @Test
    fun saveState() {
        val state = StackEntryState()
        state["null"] = null as String?
        state["string"] = "test"
        state["long"] = 0L
        state["int"] = 1
        state["double"] = 0.2
        state["float"] = 0.1f
        state["boolean"] = false
        state["kotlin serialization"] = TestClass(2)
        state.savedStateHandle()["c"] = "d"

        val savedState = state.saveState().read { toMap() }
        assertThat(savedState).containsKey("null")
        assertThat(savedState["null"]).isNull()
        assertThat(savedState["string"]).isEqualTo("test")
        assertThat(savedState["long"]).isEqualTo(0L)
        assertThat(savedState["int"]).isEqualTo(1)
        assertThat(savedState["double"]).isEqualTo(0.2)
        assertThat(savedState["float"]).isEqualTo(0.1f)
        assertThat(savedState["boolean"]).isEqualTo(false)
        assertThat((savedState["kotlin serialization"] as SavedState).read { toMap() }).isEqualTo(mapOf("value" to 2))
        assertThat(
            (savedState["khonshu-internal-saved-state-handle"] as SavedState).read {
                toMap()
            },
        ).isEqualTo(mapOf("c" to "d"))
    }

    @Test
    fun restoreState() {
        val original = mutableMapOf<String, Any?>()
        original["null"] = null as String?
        original["string"] = "test"
        original["long"] = 0L
        original["int"] = 1
        original["double"] = 0.2
        original["float"] = 0.1f
        original["boolean"] = false
        original["kotlin serialization"] = savedState(mapOf("value" to 2))
        original["khonshu-internal-saved-state-handle"] = savedState(mapOf("c" to "d"))

        val state = StackEntryState(original)
        assertThat(state.contains("null")).isTrue()
        assertThat(state.get<String?>("null")).isNull()
        assertThat(state.get<String>("string")).isEqualTo("test")
        assertThat(state.get<Long>("long")).isEqualTo(0L)
        assertThat(state.get<Int>("int")).isEqualTo(1)
        assertThat(state.get<Double>("double")).isEqualTo(0.2)
        assertThat(state.get<Float>("float")).isEqualTo(0.1f)
        assertThat(state.get<Boolean>("boolean")).isEqualTo(false)
        assertThat(state.get<TestClass>("kotlin serialization")).isEqualTo(TestClass(2))
        assertThat(state.savedStateHandle().get<String>("c")).isEqualTo("d")
    }
}

@Serializable
private data class TestClass(val value: Int)
