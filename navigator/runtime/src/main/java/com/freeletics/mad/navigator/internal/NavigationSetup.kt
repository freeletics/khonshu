package com.freeletics.mad.navigator.internal

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import kotlinx.parcelize.Parcelize

@SuppressLint("VisibleForTests") // VisibleForTests(otherwise = INTERNAL) does not exist
@InternalNavigatorApi
public suspend fun NavEventNavigator.collectAndHandleNavEvents(
    lifecycle: Lifecycle,
    executor: NavigationExecutor,
    activityLaunchers: Map<ActivityResultRequest<*, *>, ActivityResultLauncher<*>>,
    permissionLaunchers: Map<PermissionsResultRequest, ActivityResultLauncher<List<String>>>
) {
    navEvents.flowWithLifecycle(lifecycle, minActiveState = Lifecycle.State.RESUMED)
        .collect { event ->
            executor.navigate(event, activityLaunchers, permissionLaunchers)
        }
}

private fun NavigationExecutor.navigate(
    event: NavEvent,
    activityLaunchers: Map<ActivityResultRequest<*, *>, ActivityResultLauncher<*>>,
    permissionLaunchers: Map<PermissionsResultRequest, ActivityResultLauncher<List<String>>>
) {
    when (event) {
        is NavEvent.NavigateToEvent -> {
            navigate(event.route)
        }
        is NavEvent.NavigateToRootEvent -> {
            navigate(event.root, event.restoreRootState)
        }
        is NavEvent.NavigateToActivityEvent -> {
            navigate(event.route)
        }
        is NavEvent.UpEvent -> {
            navigateUp()
        }
        is NavEvent.BackEvent -> {
            navigateBack()
        }
        is NavEvent.BackToEvent -> {
            navigateBackTo(event.popUpTo, event.inclusive)
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
            savedStateHandleFor(event.key.destinationId)[event.key.requestKey] = event.result
        }
    }
}

@SuppressLint("VisibleForTests") // VisibleForTests(otherwise = INTERNAL) does not exist
@InternalNavigatorApi
public suspend fun <R : Parcelable> NavigationExecutor.collectAndHandleNavigationResults(
    request: NavigationResultRequest<R>,
) {
    val savedStateHandle = savedStateHandleFor(request.key.destinationId)
    savedStateHandle.getStateFlow<Parcelable>(request.key.requestKey, InitialValue)
        .collect {
            if (it != InitialValue) {
                @Suppress("UNCHECKED_CAST")
                request.onResult(it as R)
                savedStateHandle[request.key.requestKey] = InitialValue
            }
        }
}

@Parcelize
private object InitialValue : Parcelable
