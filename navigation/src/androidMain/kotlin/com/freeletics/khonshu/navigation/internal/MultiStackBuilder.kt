package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.SavedStateConfiguration
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavRoot
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder

internal fun createMultiStack(
    startRoot: NavRoot,
    destinations: ImmutableSet<NavDestination<*>>,
    storeHolder: StackEntryStoreHolder,
    savedStateHandle: SavedStateHandle,
): MultiStack {
    val factory = StackEntryFactory(destinations.toList(), storeHolder)
    val savedStateConfiguration = SavedStateConfiguration(destinations)
    val saver = MultiStack.Saver(factory::create, factory::create, savedStateConfiguration)
    return savedStateHandle.saveable(SAVED_STATE_STACK, saver) {
        MultiStack.createWith(startRoot, factory::create)
    }
}

@Composable
internal fun rememberMultiStack(
    startRoot: NavRoot,
    destinations: ImmutableSet<NavDestination<*>>,
): MutableState<MultiStack> {
    return key(startRoot, destinations) {
        val storeHolder = retain { StackEntryStoreHolder() }
        val factory = StackEntryFactory(destinations, storeHolder)
        val savedStateConfiguration = SavedStateConfiguration(destinations)
        val saver = MultiStack.Saver(factory::create, factory::create, savedStateConfiguration)
        rememberSaveable(stateSaver = saver) {
            mutableStateOf(MultiStack.createWith(startRoot, factory::create))
        }
    }
}

internal fun SavedStateConfiguration(destinations: ImmutableSet<NavDestination<*>>): SavedStateConfiguration {
    return SavedStateConfiguration {
        serializersModule = SerializersModule {
            destinations.forEach {
                addRoute(it)
            }
        }
    }
}

private fun <T : BaseRoute> SerializersModuleBuilder.addRoute(destination: NavDestination<T>) {
    polymorphic(BaseRoute::class, destination.id.route, destination.serializer)
}

@Suppress("DEPRECATION", "UNCHECKED_CAST")
private fun <T : Any> SavedStateHandle.saveable(key: String, saver: Saver<T, SavedState>, initial: () -> T): T {
    val savedState = get<SavedState>(key)?.read {
        getSavedState(key)
    }
    val value = if (savedState != null) {
        saver.restore(savedState) ?: initial()
    } else {
        initial()
    }

    setSavedStateProvider(key) {
        val scope = SaverScope { true }
        val state = with(saver) {
            scope.save(value)
        }
        savedState {
            if (state != null) {
                putSavedState(key, state)
            }
        }
    }

    return value
}

private const val SAVED_STATE_STACK = "com.freeletics.khonshu.navigation.stack"
