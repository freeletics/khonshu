package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.Navigator

@InternalNavigationCodegenApi
public interface NavigationExecutor : Navigator {
    public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
    public fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle
    public fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): StackEntryStore
    public fun <T : BaseRoute> extra(destinationId: DestinationId<T>): Any
}
