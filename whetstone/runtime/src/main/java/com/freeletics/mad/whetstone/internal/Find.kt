package com.freeletics.mad.whetstone.internal

import android.content.Context
import kotlin.reflect.KClass

@InternalWhetstoneApi
public fun <T> Context.findDependencies(scope: KClass<*>): T {
    return find(scope)!!
}

private fun <T : Any> Context.find(service: KClass<*>): T? {
    val serviceName = service.qualifiedName!!
    return find(serviceName) ?: applicationContext.find(serviceName)
}

@Suppress("UNCHECKED_CAST")
private fun <T> Context.find(serviceName: String): T? {
    return getSystemService(serviceName) as T?
}
