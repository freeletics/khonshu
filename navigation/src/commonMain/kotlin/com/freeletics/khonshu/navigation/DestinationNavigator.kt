package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import kotlin.reflect.KClass

/**
 * A combination of [Navigator] and [com.freeletics.khonshu.navigation.activity.ActivityNavigator] that can
 * be used as base class for navigators of individual screens.
 */
public abstract class DestinationNavigator(
    @property:InternalNavigationTestingApi
    public val hostNavigator: HostNavigator,
    public val stackEntryId: StackEntryId,
) : Navigator by hostNavigator,
    PlatformNavigator() {
    /**
     * Triggers up navigation if the entry that created this navigator is still present.
     */
    @OptIn(InternalNavigationApi::class)
    override fun navigateUp() {
        ifEntryPresent {
            navigateUp()
        }
    }

    /**
     * Removes the top entry from the backstack if the entry that created this navigator is still present.
     */
    @OptIn(InternalNavigationApi::class)
    override fun navigateBack() {
        ifEntryPresent {
            navigateBack()
        }
    }

    /**
     * Removes all entries from the backstack until [popUpTo] if the entry that created this navigator is still present.
     */
    @OptIn(InternalNavigationApi::class)
    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        ifEntryPresent {
            navigateBackTo(popUpTo, inclusive)
        }
    }

    /**
     * See [HostNavigator.navigate]. The [block] is ignored if the entry that created this navigator is no longer
     * present.
     */
    @OptIn(InternalNavigationApi::class)
    public fun navigate(block: Navigator.() -> Unit) {
        ifEntryPresent {
            navigate(block)
        }
    }

    @InternalNavigationApi
    private inline fun ifEntryPresent(block: HostNavigator.() -> Unit) {
        if (hostNavigator.containsEntry(stackEntryId)) {
            hostNavigator.block()
        }
    }
}

public expect abstract class PlatformNavigator()

@Composable
public expect fun PlatformNavigatorEffect(navigator: PlatformNavigator)
