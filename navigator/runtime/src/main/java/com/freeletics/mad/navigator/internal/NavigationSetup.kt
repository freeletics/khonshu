package com.freeletics.mad.navigator.internal

import android.app.Activity
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.ContractResultOwner
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult
import kotlinx.parcelize.Parcelize

@InternalNavigatorApi
public suspend fun NavEventNavigator.collectAndHandleNavEvents(
    lifecycle: Lifecycle,
    executor: NavigationExecutor,
    activityLaunchers: Map<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>,
) {
    navEvents.flowWithLifecycle(lifecycle, minActiveState = Lifecycle.State.RESUMED)
        .collect { event ->
            executor.navigate(event, activityLaunchers)
        }
}

private fun NavigationExecutor.navigate(
    event: NavEvent,
    activityLaunchers: Map<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>,
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
        is NavEvent.ResetToRoot -> {
            resetToRoot(event.root)
        }
        is NavEvent.ActivityResultEvent<*> -> {
            val request = event.request
            val launcher = activityLaunchers[request] ?: throw IllegalStateException(
                "No launcher registered for request with contract ${request.contract}!" +
                    "\nMake sure you called the appropriate NavEventNavigator.registerFor... method",
            )
            @Suppress("UNCHECKED_CAST")
            (launcher as ActivityResultLauncher<Any?>).launch(event.input)
        }
        is NavEvent.DestinationResultEvent<*> -> {
            savedStateHandleFor(event.key.destinationId)[event.key.requestKey] = event.result
        }
    }
}

@InternalNavigatorApi
public fun <I, O, R> ContractResultOwner<I, O, R>.deliverResult(activity: Activity, result: O) {
    deliverResult(result) {
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }
}

@Suppress("UNCHECKED_CAST")
internal inline fun <I, O, R> ContractResultOwner<I, O, R>.deliverResult(
    result: O,
    shouldShowPermissionRationale: (String) -> Boolean,
) {
    when (this) {
        is ActivityResultRequest<*, *> -> onResult(result as R)
        is PermissionsResultRequest -> onResult(
            (result as Map<String, Boolean>).mapValues { (permission, granted) ->
                if (granted) {
                    PermissionResult.Granted
                } else {
                    PermissionResult.Denied(shouldShowPermissionRationale(permission))
                }
            },
        )
    }
}

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
