package com.freeletics.khonshu.navigation

import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.StackSnapshot

/**
 * An implementation of [Navigator] that is meant to be used at the [NavHost] level.
 */
public abstract class HostNavigator internal constructor() : Navigator {
    internal abstract val snapshot: State<StackSnapshot>
}
