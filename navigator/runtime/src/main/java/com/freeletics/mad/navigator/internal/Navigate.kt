package com.freeletics.mad.navigator.internal

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.PermissionsResultRequest
import kotlin.reflect.KClass

@InternalNavigatorApi
public fun navigate(
    event: NavEvent,
    controller: NavController,
    activityLaunchers: Map<ActivityResultRequest<*, *>, ActivityResultLauncher<*>>,
    permissionLaunchers: Map<PermissionsResultRequest, ActivityResultLauncher<List<String>>>
) {
    when (event) {
        is NavEvent.NavigateToEvent -> {
            controller.navigate(event.route.destinationId(), event.route.getArguments())
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
            controller.navigate(event.root.destinationId(), event.root.getArguments(), options)
        }
        is NavEvent.NavigateToActivityEvent -> {
            controller.navigate(event.route.destinationId(), event.route.getArguments())
        }
        is NavEvent.UpEvent -> {
            controller.navigateUp()
        }
        is NavEvent.BackEvent -> {
            controller.popBackStack()
        }
        is NavEvent.BackToEvent -> {
            controller.popBackStack(event.popUpTo.destinationId(), event.inclusive)
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
        is NavEvent.DestinationResultEvent<*> -> {
            val entry = controller.getBackStackEntry(event.key.destinationId)
            entry.savedStateHandle.set(event.key.requestKey, event.result)
        }
    }
}

@InternalNavigatorApi
public fun BaseRoute.destinationId(): Int = this::class.destinationId()

@InternalNavigatorApi
public fun KClass<out BaseRoute>.destinationId(): Int = internalDestinationId()

@InternalNavigatorApi
public fun ActivityRoute.destinationId(): Int = this::class.activityDestinationId()

@InternalNavigatorApi
public fun KClass<out ActivityRoute>.activityDestinationId(): Int = internalDestinationId()

private fun KClass<*>.internalDestinationId() = qualifiedName!!.hashCode()

@InternalNavigatorApi
public fun <T : BaseRoute> Bundle?.requireRoute(): T = requireNotNull(this) {
        "Bundle is null. Can't extract Route data."
    }
    .run {
        requireNotNull(getParcelable(EXTRA_ROUTE)) {
            "Bundle doesn't contain Route data in \"$EXTRA_ROUTE\""
        }
    }

@InternalNavigatorApi
public fun <T : ActivityRoute> Bundle.toActivityRoute(): T? = getParcelable(EXTRA_ROUTE)

@InternalNavigatorApi
public fun BaseRoute.getArguments(): Bundle = Bundle().also {
    it.putParcelable(EXTRA_ROUTE, this)
}

@InternalNavigatorApi
public fun ActivityRoute.getArguments(): Bundle = Bundle().also {
    it.putParcelable(EXTRA_FILL_IN_INTENT, fillInIntent())
    if (this is Parcelable) {
        it.putParcelable(EXTRA_ROUTE, this)
    }
}

@InternalNavigatorApi
public const val EXTRA_ROUTE: String = "com.freeletics.mad.navigation.ROUTE"

@InternalNavigatorApi
public const val EXTRA_FILL_IN_INTENT: String = "com.freeletics.mad.navigation.FILL_IN_INTENT"
