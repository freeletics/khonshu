package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.Navigator

@InternalNavigationCodegenApi
public interface NavigationExecutor : Navigator {
    public val snapshot: State<StackSnapshot>
}
