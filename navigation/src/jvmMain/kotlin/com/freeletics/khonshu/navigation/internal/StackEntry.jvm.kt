package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute
import kotlinx.serialization.Serializable

public actual class StackEntry<T : BaseRoute> {
    public actual val route: T
        get() = TODO()

    @JvmInline
    @Serializable
    @InternalNavigationTestingApi
    public actual value class Id(internal val value: String)
}
