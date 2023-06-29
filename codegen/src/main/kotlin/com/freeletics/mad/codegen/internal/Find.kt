package com.freeletics.mad.codegen.internal

import android.content.Context
import com.freeletics.mad.navigation.internal.NavigationExecutor
import kotlin.reflect.KClass

@InternalCodegenApi
public fun <T> Context.findComponentByScope(scope: KClass<*>): T {
    val component = find(scope)
    checkNotNull(component) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return component as T
}

/**
 * Looks up [T] by searching for a fitting [NavEntryComponentGetter] or finding the [scope] in
 * [Context.getSystemService].
 */
@InternalCodegenApi
public fun <T : Any> Context.findComponentByScope(
    scope: KClass<*>,
    destinationScope: KClass<*>,
    executor: NavigationExecutor,
): T {
    if (scope != destinationScope) {
        val destinationComponent = find(destinationScope) as? NavDestinationComponent
        val getter = destinationComponent?.navEntryComponentGetters?.get(scope.java)
        if (getter != null) {
            @Suppress("UNCHECKED_CAST")
            return getter.retrieve(executor, this) as T
        }
    }
    val dependency = find(scope)
    checkNotNull(dependency) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return dependency as T
}

@InternalCodegenApi
public fun Context.find(service: KClass<*>): Any? {
    val serviceName = service.qualifiedName!!
    return getSystemService(serviceName) ?: applicationContext.getSystemService(serviceName)
}
