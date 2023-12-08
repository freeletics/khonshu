package com.freeletics.khonshu.navigation.test

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Turbine
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import java.io.Serializable

internal class TestNavigationExecutor : NavigationExecutor {

    val received = Turbine<NavEvent>()
    val savedStateHandle = SavedStateHandle()

    override fun navigateTo(route: NavRoute) {
        received.add(NavEvent.NavigateToEvent(route))
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        received.add(NavEvent.NavigateToRootEvent(root, restoreRootState))
    }

    override fun navigateTo(route: ActivityRoute) {
        received.add(NavEvent.NavigateToActivityEvent(route))
    }

    override fun navigateUp() {
        received.add(NavEvent.UpEvent)
    }

    override fun navigateBack() {
        received.add(NavEvent.BackEvent)
    }

    override fun <T : BaseRoute> navigateBackToInternal(
        popUpTo: DestinationId<T>,
        inclusive: Boolean,
    ) {
        received.add(NavEvent.BackToEvent(popUpTo, inclusive))
    }

    override fun resetToRoot(root: NavRoot) {
        received.add(NavEvent.ResetToRoot(root))
    }

    override fun replaceAll(root: NavRoot) {
        received.add(NavEvent.ReplaceAll(root))
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

    override fun <T : BaseRoute> extra(destinationId: DestinationId<T>): Serializable {
        throw UnsupportedOperationException()
    }
}
