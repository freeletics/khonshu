package com.freeletics.khonshu.navigation.internal

import android.content.Context
import android.os.Bundle
import com.freeletics.khonshu.navigation.ActivityDestination
import com.freeletics.khonshu.navigation.ContentDestination
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.internal.MultiStackHostNavigator.Companion.SAVED_STATE_STACK
import kotlinx.collections.immutable.ImmutableSet

internal fun createHostNavigator(
    context: Context,
    viewModel: StackEntryStoreViewModel,
    startRoot: NavRoot,
    destinations: ImmutableSet<NavDestination>,
): MultiStackHostNavigator {
    val activityDestinations = destinations.filterIsInstance<ActivityDestination>()
    val starter = ActivityStarter(context.applicationContext, activityDestinations)

    val contentDestinations = destinations.filterIsInstance<ContentDestination<*>>()
    val factory = StackEntryFactory(contentDestinations, viewModel)

    val navState = viewModel.globalSavedStateHandle.get<Bundle>(SAVED_STATE_STACK)
    val stack = if (navState == null) {
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

    return MultiStackHostNavigator(
        stack = stack,
        viewModel = viewModel,
        activityStarter = starter::start,
    )
}
