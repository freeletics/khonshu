package com.freeletics.khonshu.navigation

public expect abstract class HostNavigator : Navigator {
    public abstract val startRoot: NavRoot

    public abstract fun navigate(block: Navigator.() -> Unit)
}
