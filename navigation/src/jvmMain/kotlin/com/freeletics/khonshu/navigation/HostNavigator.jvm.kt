package com.freeletics.khonshu.navigation

public actual abstract class HostNavigator : Navigator {
    public actual abstract val startRoot: NavRoot

    public actual abstract fun navigate(block: Navigator.() -> Unit)
}
