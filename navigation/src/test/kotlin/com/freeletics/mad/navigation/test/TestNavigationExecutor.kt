package com.freeletics.mad.navigation.test

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Turbine
import com.freeletics.mad.navigation.ActivityRoute
import com.freeletics.mad.navigation.BaseRoute
import com.freeletics.mad.navigation.NavRoot
import com.freeletics.mad.navigation.NavRoute
import com.freeletics.mad.navigation.internal.DestinationId
import com.freeletics.mad.navigation.internal.NavEvent
import com.freeletics.mad.navigation.internal.NavigationExecutor

internal class TestNavigationExecutor : NavigationExecutor {

    val received = Turbine<NavEvent>()
    val savedStateHandle = SavedStateHandle()

    override fun navigate(route: NavRoute) {
        received.add(NavEvent.NavigateToEvent(route))
    }

    override fun navigate(root: NavRoot, restoreRootState: Boolean) {
        received.add(NavEvent.NavigateToRootEvent(root, restoreRootState))
    }

    override fun navigate(route: ActivityRoute) {
        received.add(NavEvent.NavigateToActivityEvent(route))
    }

    override fun navigateUp() {
        received.add(NavEvent.UpEvent)
    }

    override fun navigateBack() {
        received.add(NavEvent.BackEvent)
    }

    override fun <T : BaseRoute> navigateBackTo(
        destinationId: DestinationId<T>,
        isInclusive: Boolean,
    ) {
        received.add(NavEvent.BackToEvent(destinationId, isInclusive))
    }

    override fun resetToRoot(root: NavRoot) {
        received.add(NavEvent.ResetToRoot(root))
    }

    override fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T {
        throw UnsupportedOperationException()
    }

    override fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle {
        return savedStateHandle
    }

    override fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): NavigationExecutor.Store {
        throw UnsupportedOperationException()
    }
}
