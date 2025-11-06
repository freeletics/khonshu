package com.freeletics.khonshu.navigation.internal

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.internal.MultiStackHostNavigator.Companion.SAVED_STATE_STACK
import kotlinx.collections.immutable.ImmutableSet

internal fun createMultiStack(
    viewModel: StackEntryStoreViewModel,
    startRoot: NavRoot,
    destinations: ImmutableSet<NavDestination<*>>,
): MultiStack {
    val factory = StackEntryFactory(destinations.toList(), viewModel)

    val navState = viewModel.globalSavedStateHandle.get<Bundle>(SAVED_STATE_STACK)
    return if (navState == null) {
        MultiStack.createWith(
            root = startRoot,
            createEntry = factory::create,
        )
    } else {
        MultiStack.fromState(
            root = null,
            bundle = navState,
            createEntry = factory::create,
            createRestoredEntry = factory::create,
        )
    }
}

@Composable
internal fun rememberMultiStack(
    startRoot: NavRoot,
    viewModel: StackEntryStoreViewModel,
    destinations: ImmutableSet<NavDestination<*>>,
): MutableState<MultiStack> {
    return remember(startRoot, viewModel, destinations) {
        val factory = StackEntryFactory(destinations.toList(), viewModel)

        val navState = viewModel.globalSavedStateHandle.get<Bundle>(SAVED_STATE_STACK)
        val multiStack = if (navState == null) {
            MultiStack.createWith(
                root = startRoot,
                createEntry = factory::create,
            )
        } else {
            MultiStack.fromState(
                root = startRoot,
                bundle = navState,
                createEntry = factory::create,
                createRestoredEntry = factory::create,
            )
        }
        mutableStateOf(multiStack)
    }
}
