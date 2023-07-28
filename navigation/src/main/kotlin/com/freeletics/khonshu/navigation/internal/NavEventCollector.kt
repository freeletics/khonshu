package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import kotlin.reflect.KClass


public interface Navigator {
    public fun navigateTo(route: NavRoute)
    public fun navigateToRoot(root: NavRoot, restoreRootState: Boolean = false)
    public fun navigateUp()
    public fun navigateBack()
    public fun <T : NavRoute> navigateBackTo(navRoute: KClass<T> , inclusive: Boolean = false)
    public fun resetToRoot(root: NavRoot)
}

internal class NavEventCollector : Navigator {

    private val _navEvents = mutableListOf<NavEvent>()
    val navEvents : List<NavEvent> = _navEvents

    override fun navigateTo(route: NavRoute) {
        val event = NavEvent.NavigateToEvent(route)
        _navEvents.add(event)
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        _navEvents.add(event)
    }

    override fun navigateUp() {
        val event = NavEvent.UpEvent
        _navEvents.add(event)
    }

    override fun navigateBack() {
        val event = NavEvent.BackEvent
        _navEvents.add(event)
    }

    override fun <T : NavRoute> navigateBackTo(navRoute: KClass<T>, inclusive: Boolean) {
        val event = NavEvent.BackToEvent(DestinationId(navRoute), inclusive)
        _navEvents.add(event)
    }

    override fun resetToRoot(root: NavRoot) {
        val event = NavEvent.ResetToRoot(root)
        _navEvents.add(event)
    }
}
