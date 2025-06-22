package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute
import kotlinx.serialization.Serializable

public expect class StackEntry<T : BaseRoute> {
    public val route: T

    @Serializable
    @InternalNavigationTestingApi
    public value class Id(internal val value: String)
}
