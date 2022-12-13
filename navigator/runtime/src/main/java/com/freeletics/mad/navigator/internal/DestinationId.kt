package com.freeletics.mad.navigator.internal

import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import kotlin.reflect.KClass

@InternalNavigatorApi
@JvmInline
public value class DestinationId<T : BaseRoute>(public val route: KClass<T>)

@InternalNavigatorApi
public val <T : BaseRoute> T.destinationId: DestinationId<out T>
    get() = DestinationId(this::class)

@InternalNavigatorApi
@JvmInline
public value class ActivityDestinationId<T : ActivityRoute>(public val route: KClass<T>)
