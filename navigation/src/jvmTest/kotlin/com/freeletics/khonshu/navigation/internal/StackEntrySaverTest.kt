package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.saveable.SaverScope
import androidx.savedstate.serialization.SavedStateConfiguration
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.ScreenDestination
import com.freeletics.khonshu.navigation.StackEntryState
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

@OptIn(InternalNavigationTestingApi::class, InternalNavigationCodegenApi::class)
class StackEntrySaverTest {
    private val savedStateConfiguration = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(BaseRoute::class) {
                subclass(TestRoute::class)
            }
        }
    }

    private val saver = StackEntry.Saver(
        createRestoredEntry = { route, id, state ->
            StackEntry(
                id = id,
                route = route,
                destination = ScreenDestination<BaseRoute> {},
                state = state,
                store = StackEntryStore {},
            )
        },
        savedStateConfiguration = savedStateConfiguration,
    )

    @Test
    fun saveAndRestore() {
        val state = StackEntryState()
        state["key"] = "value"
        val entry = StackEntry(
            id = StackEntry.Id("test-id"),
            route = TestRoute(42),
            destination = ScreenDestination<BaseRoute> {},
            state = state,
            store = StackEntryStore {},
        )

        val saved = with(saver) { SaverScope { true }.save(entry) }
        val restored = saver.restore(saved)

        assertThat(restored.id).isEqualTo(StackEntry.Id("test-id"))
        assertThat(restored.route).isEqualTo(TestRoute(42))
        assertThat(restored.state.get<String>("key")).isEqualTo("value")
    }
}

@Serializable
private data class TestRoute(val number: Int) : NavRoute
