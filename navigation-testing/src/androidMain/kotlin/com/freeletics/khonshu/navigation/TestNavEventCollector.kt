package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.internal.NavEvent
import kotlin.reflect.KClass

public class TestNavEventCollector internal constructor() {
    private val navEventList = mutableListOf<NavEvent>()
    internal val navEvents: List<NavEvent> = navEventList

    public fun awaitNavigateTo(route: NavRoute) {
        val event = NavEvent.NavigateToEvent(route)
        navEventList.add(event)
    }

    public fun awaitNavigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        navEventList.add(event)
    }

    public fun awaitNavigateUp() {
        val event = NavEvent.UpEvent
        navEventList.add(event)
    }

    public fun awaitNavigateBack() {
        val event = NavEvent.BackEvent
        navEventList.add(event)
    }

    public inline fun <reified T : NavRoute> awaitNavigateBackTo(inclusive: Boolean) {
        awaitNavigateBackTo(T::class, inclusive)
    }

    @PublishedApi
    internal fun <T : NavRoute> awaitNavigateBackTo(destination: KClass<T>, inclusive: Boolean) {
        val event = NavEvent.BackToEvent(destination, inclusive)
        navEventList.add(event)
    }

    public fun awaitResetToRoot(root: NavRoot) {
        val event = NavEvent.ResetToRoot(root)
        navEventList.add(event)
    }
}
