package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.Navigator
import kotlin.reflect.KClass

internal class NavEventCollector : Navigator {
    private val navEventList = mutableListOf<NavEvent>()
    internal val navEvents: List<NavEvent> = navEventList

    override fun navigateTo(route: NavRoute) {
        val event = NavEvent.NavigateToEvent(route)
        navEventList.add(event)
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        navEventList.add(event)
    }

    override fun navigateUp() {
        val event = NavEvent.UpEvent
        navEventList.add(event)
    }

    override fun navigateBack() {
        val event = NavEvent.BackEvent
        navEventList.add(event)
    }

    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        val event = NavEvent.BackToEvent(popUpTo, inclusive)
        navEventList.add(event)
    }

    override fun resetToRoot(root: NavRoot) {
        val event = NavEvent.ResetToRoot(root)
        navEventList.add(event)
    }

    override fun replaceAll(root: NavRoot) {
        val event = NavEvent.ReplaceAll(root)
        navEventList.add(event)
    }
}
