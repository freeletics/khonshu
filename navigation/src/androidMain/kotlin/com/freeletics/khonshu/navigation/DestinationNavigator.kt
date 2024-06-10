package com.freeletics.khonshu.navigation

/**
 * A combination of [Navigator] and [ActivityResultNavigator] that can
 * be used as base class for navigators of individual screens.
 */
public abstract class DestinationNavigator(
    private val navigator: HostNavigator,
) : Navigator by navigator, ResultNavigator by navigator, BackInterceptor by navigator, ActivityResultNavigator() {

    /**
     * See [HostNavigator.navigate].
     */
    public fun navigate(block: Navigator.() -> Unit) {
        navigator.navigate(block)
    }
}
