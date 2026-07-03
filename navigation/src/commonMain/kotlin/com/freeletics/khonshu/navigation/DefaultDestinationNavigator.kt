package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import kotlin.reflect.KClass

@InternalNavigationCodegenApi
public class DefaultDestinationNavigator(
    private val hostNavigator: HostNavigator,
    override val stackEntry: StackEntry<*>,
) : PlatformNavigator(),
    DestinationNavigator2,
    Navigator by hostNavigator {
    override val platformNavigator: PlatformNavigator
        get() = this

    /**
     * Triggers up navigation if the entry that created this navigator is still present.
     */
    @OptIn(InternalNavigationTestingApi::class)
    override fun navigateUp() {
        ifEntryPresent {
            navigateUp()
        }
    }

    /**
     * Removes the top entry from the backstack if the entry that created this navigator is still present.
     */
    @OptIn(InternalNavigationTestingApi::class)
    override fun navigateBack() {
        ifEntryPresent {
            navigateBack()
        }
    }

    /**
     * Removes all entries from the backstack until [popUpTo] if the entry that created this navigator is still present.
     */
    @OptIn(InternalNavigationTestingApi::class)
    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        ifEntryPresent {
            navigateBackTo(popUpTo, inclusive)
        }
    }

    /**
     * See [HostNavigator.navigate]. The [block] is ignored if the entry that created this navigator is no longer
     * present.
     */
    @OptIn(InternalNavigationTestingApi::class)
    override fun navigate(block: Navigator.() -> Unit) {
        ifEntryPresent {
            navigate(block)
        }
    }

    @InternalNavigationApi
    override fun getTopEntryFor(destinationId: DestinationId<*>): StackEntry<*> {
        return if (stackEntry.destinationId == destinationId) {
            stackEntry
        } else {
            hostNavigator.getTopEntryFor(destinationId)
        }
    }

    @InternalNavigationApi
    override fun getEntryFor(id: StackEntry.Id): StackEntry<*> {
        return if (stackEntry.id == id) {
            stackEntry
        } else {
            hostNavigator.getEntryFor(id)
        }
    }

    @OptIn(InternalNavigationTestingApi::class)
    private inline fun ifEntryPresent(block: HostNavigator.() -> Unit) {
        if (hostNavigator.snapshot.value.entryForOrNull(stackEntry.id) != null) {
            hostNavigator.block()
        }
    }
}
