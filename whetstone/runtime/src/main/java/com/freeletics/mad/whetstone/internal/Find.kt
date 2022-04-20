package com.freeletics.mad.whetstone.internal

import android.content.Context
import kotlin.reflect.KClass

@InternalWhetstoneApi
public fun <T> Context.findDependencies(scope: KClass<*>): T {
    val dependency = find(scope)
    checkNotNull(dependency) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return dependency as T
}

@InternalWhetstoneApi
public fun Context.find(service: KClass<*>): Any? {
    val serviceName = service.qualifiedName!!
    return find(serviceName) ?: applicationContext.find(serviceName)
}

private fun Context.find(serviceName: String): Any? {
    return getSystemService(serviceName)
}
