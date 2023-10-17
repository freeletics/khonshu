package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.NavEvent

public class TestNavEventCollector internal constructor() {

    private val _navEvents = mutableListOf<NavEvent>()
    internal val navEvents: List<NavEvent> = _navEvents

    public fun awaitNavigateTo(route: NavRoute) {
        val event = NavEvent.NavigateToEvent(route)
        _navEvents.add(event)
    }

    public fun awaitNavigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        _navEvents.add(event)
    }

    public fun awaitNavigateUp() {
        val event = NavEvent.UpEvent
        _navEvents.add(event)
    }

    public fun awaitNavigateBack() {
        val event = NavEvent.BackEvent
        _navEvents.add(event)
    }

    public inline fun <reified T : NavRoute> awaitNavigateBackTo(inclusive: Boolean) {
        awaitNavigateBackTo(DestinationId(T::class), inclusive)
    }

    @PublishedApi
    internal fun <T : NavRoute> awaitNavigateBackTo(destination: DestinationId<T>, inclusive: Boolean) {
        val event = NavEvent.BackToEvent(destination, inclusive)
        _navEvents.add(event)
    }

    public fun awaitResetToRoot(root: NavRoot) {
        val event = NavEvent.ResetToRoot(root)
        _navEvents.add(event)
    }
}
