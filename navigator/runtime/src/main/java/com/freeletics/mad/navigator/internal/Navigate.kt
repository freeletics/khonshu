package com.freeletics.mad.navigator.internal

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.PermissionsResultRequest

@InternalNavigatorApi
public fun navigate(
    event: NavEvent,
    controller: NavController,
    activityLaunchers: Map<ActivityResultRequest<*, *>, ActivityResultLauncher<*>>,
    permissionLaunchers: Map<PermissionsResultRequest, ActivityResultLauncher<List<String>>>
) {
    when (event) {
        is NavEvent.NavigateToEvent -> {
            controller.navigate(event.route.destinationId, event.route.getArguments())
        }
        is NavEvent.NavigateBackAndThenToEvent -> {
            val options = NavOptions.Builder()
                .setPopUpTo(event.popUpToDestinationId, inclusive = event.inclusive)
                .build()
            controller.navigate(event.route.destinationId, event.route.getArguments(), options)
        }
        is NavEvent.NavigateToRootEvent -> {
            val options = NavOptions.Builder()
                // save the state of the current root before leaving it
                .setPopUpTo(controller.graph.startDestinationId, inclusive = false, saveState = true)
                // restoring the state of the target root
                .setRestoreState(event.restoreRootState)
                // makes sure that if the destination is already on the backstack, it and
                // everything above it gets removed
                .setLaunchSingleTop(true)
                .build()
            controller.navigate(event.root.destinationId, event.root.getArguments(), options)
        }
        is NavEvent.UpEvent -> {
            controller.navigateUp()
        }
        is NavEvent.BackEvent -> {
            controller.popBackStack()
        }
        is NavEvent.BackToEvent -> {
            controller.popBackStack(event.destinationId, event.inclusive)
        }
        is NavEvent.ActivityResultEvent<*> -> {
            val request = event.request
            val launcher = activityLaunchers[request] ?: throw IllegalStateException(
                "No launcher registered for $request!\nMake sure you called the appropriate " +
                    "AbstractNavigator.registerFor... method"
            )
            @Suppress("UNCHECKED_CAST")
            (launcher as ActivityResultLauncher<Any?>).launch(event.input)
        }
        is NavEvent.PermissionsResultEvent -> {
            val request = event.request
            val launcher = permissionLaunchers[request] ?: throw IllegalStateException(
                "No launcher registered for $request!\nMake sure you called the appropriate " +
                    "AbstractNavigator.registerFor... method"
            )
            @Suppress("UNCHECKED_CAST")
            (launcher as ActivityResultLauncher<Any?>).launch(event.permissions)
        }
    }
}

@ObsoleteNavigatorApi
public fun <T : NavRoute> Bundle.toNavRoute(): T = getParcelable(EXTRA_ROUTE)!!

@ObsoleteNavigatorApi
public fun <T : NavRoot> Bundle.toNavRoot(): T = getParcelable(EXTRA_ROUTE)!!

private fun NavRoute.getArguments(): Bundle = Bundle().also {
    if (this is Parcelable) {
        it.putParcelable(EXTRA_ROUTE, this)
    }
}

private fun NavRoot.getArguments(): Bundle = Bundle().also {
    if (this is Parcelable) {
        it.putParcelable(EXTRA_ROUTE, this)
    }
}

private const val EXTRA_ROUTE = "com.freeletics.mad.navigation.ROUTE"
