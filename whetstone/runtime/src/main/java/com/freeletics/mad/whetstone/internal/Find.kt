package com.freeletics.mad.whetstone.internal

import android.content.Context
import kotlin.reflect.KClass

@InternalWhetstoneApi
public fun <T> Context.findComponentByScope(scope: KClass<*>): T {
    val component = find(scope)
    checkNotNull(component) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return component as T
}

@InternalWhetstoneApi
public fun Context.find(service: KClass<*>): Any? {
    val serviceName = service.qualifiedName!!
    return getSystemService(serviceName) ?: applicationContext.getSystemService(serviceName)
}
