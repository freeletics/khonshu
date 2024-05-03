package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.Navigator
import kotlin.reflect.KClass

@InternalNavigationCodegenApi
public interface NavigationExecutor : Navigator {
    public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
    public fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle
    public fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): Store
    public fun <T : BaseRoute> extra(destinationId: DestinationId<T>): Any

    @InternalNavigationCodegenApi
    public interface Store {
        public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T
    }
}
