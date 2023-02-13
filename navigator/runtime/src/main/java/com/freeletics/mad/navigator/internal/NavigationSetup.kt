package com.freeletics.mad.navigator.internal

import android.app.Activity
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.ContractResultOwner
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.RequestPermissionsContract.Companion.enrichResult
import kotlinx.parcelize.Parcelize

@InternalNavigatorApi
public suspend fun NavEventNavigator.collectAndHandleNavEvents(
    executor: NavigationExecutor,
    activityLaunchers: Map<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>,
) {
    navEvents.collect { event ->
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
            navigate(event.root, event.restoreRootState, event.saveCurrentRootState)
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
                    "NavEventNavigator.registerFor... method"
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
@Suppress("UNCHECKED_CAST")
public fun <I, O, R> ContractResultOwner<I, O, R>.deliverResult(activity: Activity, result: O) {
    when(this) {
        is ActivityResultRequest<*, *> -> onResult(result as R)
        is PermissionsResultRequest -> onResult(enrichResult(activity, result as Map<String, Boolean>))
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
