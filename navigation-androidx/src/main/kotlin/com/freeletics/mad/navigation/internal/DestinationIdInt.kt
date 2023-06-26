package com.freeletics.mad.navigation.internal

import com.freeletics.mad.navigation.ActivityRoute
import com.freeletics.mad.navigation.BaseRoute
import kotlin.reflect.KClass

@InternalNavigationApi
public fun BaseRoute.destinationId(): Int = this::class.destinationId()

@InternalNavigationApi
public fun KClass<out BaseRoute>.destinationId(): Int = internalDestinationId()

@InternalNavigationApi
public fun DestinationId<*>.destinationId(): Int = route.internalDestinationId()

@InternalNavigationApi
public fun ActivityRoute.destinationId(): Int = this::class.activityDestinationId()

@InternalNavigationApi
public fun ActivityDestinationId<*>.destinationId(): Int = route.internalDestinationId()

@InternalNavigationApi
public fun KClass<out ActivityRoute>.activityDestinationId(): Int = internalDestinationId()

private fun KClass<*>.internalDestinationId() = qualifiedName!!.hashCode()
