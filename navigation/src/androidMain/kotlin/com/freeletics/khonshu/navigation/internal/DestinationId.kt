package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute
import kotlin.reflect.KClass

@InternalNavigationCodegenApi
@JvmInline
public value class DestinationId<T : BaseRoute>(public val route: KClass<T>)

@InternalNavigationCodegenApi
public val <T : BaseRoute> T.destinationId: DestinationId<out T>
    get() = DestinationId(this::class)
