package com.freeletics.mad.whetstone.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.compose.LocalNavigationExecutor
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.destinationId
import com.freeletics.mad.whetstone.internal.InternalWhetstoneApi
import com.freeletics.mad.whetstone.internal.findComponentByScope
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel]. The `ViewModel.Factory` will use [parentScope] to lookup
 * a parent component instance. That component will then be passed to the given [factory] together
 * with a [SavedStateHandle] and the passed in [route].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
@OptIn(InternalNavigatorApi::class)
@Composable
public inline fun <reified C : Any, P : Any, R : BaseRoute> rememberComponent(
    parentScope: KClass<*>,
    destinationScope: KClass<*>,
    route: R,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle, R) -> C
): C {
    val context = LocalContext.current
    val executor = LocalNavigationExecutor.current
    return remember(context, executor, route) {
        executor.storeFor(route.destinationId).getOrCreate(C::class) {
            val parentComponent = context.findComponentByScope<P>(parentScope, destinationScope, executor)
            val savedStateHandle = executor.savedStateHandleFor(route.destinationId)
            factory(parentComponent, savedStateHandle, route)
        }
    }
}
