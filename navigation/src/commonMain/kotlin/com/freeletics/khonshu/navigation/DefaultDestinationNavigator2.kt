package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.activity.ActivityNavigatorApi
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import kotlin.reflect.KClass

@InternalNavigationCodegenApi
public class DefaultDestinationNavigator2(
    private val destinationNavigator: DestinationNavigator,
    override val stackEntry: StackEntry<*>,
) : DestinationNavigator2,
    Navigator by destinationNavigator,
    ActivityNavigatorApi by destinationNavigator {
    @OptIn(InternalNavigationTestingApi::class)
    override fun navigateUp() {
        ifTopEntry {
            navigateUp()
        }
    }

    @OptIn(InternalNavigationTestingApi::class)
    override fun navigateBack() {
        ifTopEntry {
            navigateBack()
        }
    }

    @OptIn(InternalNavigationTestingApi::class)
    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        ifTopEntry {
            navigateBackTo(popUpTo, inclusive)
        }
    }

    override fun navigate(block: Navigator.() -> Unit) {
        destinationNavigator.navigate(block)
    }

    @InternalNavigationApi
    override fun getTopEntryFor(destinationId: DestinationId<*>): StackEntry<*> {
        return if (stackEntry.destinationId == destinationId) {
            stackEntry
        } else {
            destinationNavigator.getTopEntryFor(destinationId)
        }
    }

    @InternalNavigationApi
    override fun getEntryFor(id: StackEntry.Id): StackEntry<*> {
        return if (stackEntry.id == id) {
            stackEntry
        } else {
            destinationNavigator.getEntryFor(id)
        }
    }

    @OptIn(InternalNavigationTestingApi::class)
    private inline fun ifTopEntry(block: DestinationNavigator.() -> Unit) {
        if (destinationNavigator.hostNavigator.snapshot.value.current.id == stackEntry.id) {
            destinationNavigator.block()
        }
    }
}
