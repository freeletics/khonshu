package com.freeletics.khonshu.navigation.internal

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavRoot
import kotlinx.collections.immutable.ImmutableSet

internal fun createMultiStack(
    viewModel: StackEntryStoreViewModel,
    startRoot: NavRoot,
    destinations: ImmutableSet<NavDestination<*>>,
): MultiStack {
    val factory = StackEntryFactory(destinations.toList(), viewModel)
    val saver = MultiStack.Saver(factory::create, factory::create)
    return viewModel.globalSavedStateHandle.saveable(SAVED_STATE_STACK, saver) {
        MultiStack.createWith(startRoot, factory::create)
    }
}

@Composable
internal fun rememberMultiStack(
    startRoot: NavRoot,
    viewModel: StackEntryStoreViewModel,
    destinations: ImmutableSet<NavDestination<*>>,
): MutableState<MultiStack> {
    return key(startRoot, viewModel, destinations) {
        val factory = StackEntryFactory(destinations.toList(), viewModel)
        val saver = MultiStack.Saver(factory::create, factory::create)
        rememberSaveable(stateSaver = saver) {
            mutableStateOf(MultiStack.createWith(startRoot, factory::create))
        }
    }
}

@Suppress("DEPRECATION", "UNCHECKED_CAST")
private fun <T : Any, S : Any> SavedStateHandle.saveable(key: String, saver: Saver<T, S>, initial: () -> T): T {
    val value = get<Bundle>(key)?.getParcelableArrayList<Parcelable>(key)?.let { saver.restore(it as S) } ?: initial()
    setSavedStateProvider(key) {
        with(saver) {
            val state = SaverScope { true }.save(value)
            bundleOf(key to state)
        }
    }
    return value
}

private const val SAVED_STATE_STACK = "com.freeletics.khonshu.navigation.stack"
