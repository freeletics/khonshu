package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.Navigator
import kotlin.reflect.KClass

internal actual class MultiStackHostNavigator actual constructor(stack: MultiStack) : HostNavigator() {
    override fun navigate(block: Navigator.() -> Unit) {
        TODO("Not yet implemented")
    }

    @InternalNavigationTestingApi
    override val snapshot: State<StackSnapshot>
        get() = TODO("Not yet implemented")

    override fun navigateTo(route: NavRoute) {
        TODO("Not yet implemented")
    }

    override fun navigateUp() {
        TODO("Not yet implemented")
    }

    override fun navigateBack() {
        TODO("Not yet implemented")
    }

    override fun <T : BaseRoute> navigateBackTo(
        popUpTo: KClass<T>,
        inclusive: Boolean,
    ) {
        TODO("Not yet implemented")
    }

    override fun switchBackStack(root: NavRoot) {
        TODO("Not yet implemented")
    }

    override fun showRoot(root: NavRoot) {
        TODO("Not yet implemented")
    }

    override fun replaceAllBackStacks(root: NavRoot) {
        TODO("Not yet implemented")
    }

    @InternalNavigationTestingApi
    override val startRoot: NavRoot
        get() = TODO("Not yet implemented")

    @InternalNavigationApi
    override fun getTopEntryFor(destinationId: DestinationId<*>): StackEntry<*> {
        TODO("Not yet implemented")
    }

    @InternalNavigationApi
    override fun getEntryFor(id: StackEntry.Id): StackEntry<*> {
        TODO("Not yet implemented")
    }
}
