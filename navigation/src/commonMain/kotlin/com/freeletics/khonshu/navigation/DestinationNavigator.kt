package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi

/**
 * A combination of [Navigator] and [com.freeletics.khonshu.navigation.activity.ActivityNavigator] that can
 * be used as base class for navigators of individual screens.
 */
public abstract class DestinationNavigator(
    @property:InternalNavigationTestingApi
    public val hostNavigator: HostNavigator,
) : Navigator by hostNavigator,
    PlatformNavigator() {
    /**
     * See [HostNavigator.navigate].
     */
    public fun navigate(block: Navigator.() -> Unit) {
        hostNavigator.navigate(block)
    }
}

/**
 * Entry-aware navigator for individual destinations.
 */
public interface DestinationNavigator2 : Navigator {
    /**
     * Whether this navigator's destination is currently the current destination.
     *
     * While this is `false` all back navigation ([navigateBack], [navigateUp], [navigateBackTo] and
     * [navigate]) is ignored.
     *
     * When this is read from a `@Composable` function it is observed and the composable will be
     * recomposed whenever the value changes. It is meant to be used for enabling or disabling UI
     * and in tests. Navigation logic should generally not branch on it.
     */
    public val isCurrentDestination: Boolean

    /**
     * See [HostNavigator.navigate]. The block only executes while this navigator's destination is current.
     */
    public fun navigate(block: Navigator.() -> Unit)
}

public expect abstract class PlatformNavigator()

@Composable
public expect fun PlatformNavigatorEffect(navigator: PlatformNavigator)
