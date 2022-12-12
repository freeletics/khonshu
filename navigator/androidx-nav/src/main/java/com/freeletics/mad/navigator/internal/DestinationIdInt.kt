package com.freeletics.mad.navigator.internal

import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import kotlin.reflect.KClass


@InternalNavigatorApi
public fun BaseRoute.destinationId(): Int = this::class.destinationId()

@InternalNavigatorApi
public fun KClass<out BaseRoute>.destinationId(): Int = internalDestinationId()

@InternalNavigatorApi
public fun DestinationId<*>.destinationId(): Int = route.internalDestinationId()

@InternalNavigatorApi
public fun ActivityRoute.destinationId(): Int = this::class.activityDestinationId()

@InternalNavigatorApi
public fun ActivityDestinationId<*>.destinationId(): Int = route.internalDestinationId()

@InternalNavigatorApi
public fun KClass<out ActivityRoute>.activityDestinationId(): Int = internalDestinationId()

private fun KClass<*>.internalDestinationId() = qualifiedName!!.hashCode()
