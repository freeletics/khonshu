package com.freeletics.mad.navigator.internal

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.PermissionsResultRequest

@InternalNavigatorApi
public fun navigate(
    event: NavEvent,
    controller: NavigationExecutor,
    activityLaunchers: Map<ActivityResultRequest<*, *>, ActivityResultLauncher<*>>,
    permissionLaunchers: Map<PermissionsResultRequest, ActivityResultLauncher<List<String>>>
) {
    when (event) {
        is NavEvent.NavigateToEvent -> {
            controller.navigate(event.route)
        }
        is NavEvent.NavigateToRootEvent -> {
            controller.navigate(event.root, event.restoreRootState)
        }
        is NavEvent.NavigateToActivityEvent -> {
            controller.navigate(event.route)
        }
        is NavEvent.UpEvent -> {
            controller.navigateUp()
        }
        is NavEvent.BackEvent -> {
            controller.navigateBack()
        }
        is NavEvent.BackToEvent -> {
            controller.navigateBackTo(event.popUpTo, event.inclusive)
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
            controller.deliverResult(event.key, event.result)
        }
    }
}


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
