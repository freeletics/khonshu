package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.Navigator

@InternalNavigationCodegenApi
public interface NavigationExecutor : Navigator {
    public val snapshot: State<StackSnapshot>
}
