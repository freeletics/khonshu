package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi

/**
 * A combination of [Navigator] and [ActivityNavigator] that can
 * be used as base class for navigators of individual screens.
 */
public abstract class DestinationNavigator(
    @property:InternalNavigationTestingApi
    public val hostNavigator: HostNavigator,
) : Navigator by hostNavigator,
    ResultNavigator by hostNavigator,
    BackInterceptor by hostNavigator,
    ActivityNavigator() {
    /**
     * See [HostNavigator.navigate].
     */
    public fun navigate(block: Navigator.() -> Unit) {
        hostNavigator.navigate(block)
    }
}
