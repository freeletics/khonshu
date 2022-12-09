package com.freeletics.mad.navigator.internal

import androidx.activity.result.ActivityResultLauncher
import com.freeletics.mad.navigator.ActivityResultRequest
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
