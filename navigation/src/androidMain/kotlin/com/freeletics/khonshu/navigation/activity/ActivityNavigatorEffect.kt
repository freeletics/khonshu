package com.freeletics.khonshu.navigation.activity

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.freeletics.khonshu.navigation.LocalHostNavigator
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent
import com.freeletics.khonshu.navigation.activity.internal.ActivityStarter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting

/**
 * Sets up the [ActivityNavigator] and [com.freeletics.khonshu.navigation.DestinationNavigator] inside the current
 * composition so that its events are handled while the composition is active.
 */
@Composable
public fun ActivityNavigatorEffect(navigator: ActivityNavigator) {
    val hostNavigator = LocalHostNavigator.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = requireNotNull(LocalActivity.current) {
        "ActivityNavigatorEffect can only be used in the context of an Activity"
    }

    val activityStarter = remember(activity, hostNavigator) {
        ActivityStarter(activity, hostNavigator)
    }

    val activityLaunchers = navigator.activityResultRequests.associateWith {
        rememberResultLaunchers(it, activity)
    }

    LaunchedEffect(lifecycleOwner, hostNavigator, activityStarter, navigator) {
        navigator.collectAndHandleActivityEvents(
            lifecycleOwner.lifecycle,
            activityStarter::start,
            activityLaunchers,
        )
    }
}

@Composable
private fun <I, O> rememberResultLaunchers(
    request: ActivityResultContractRequest<I, O, *>,
    activity: Activity,
): ActivityResultLauncher<*> {
    return rememberLauncherForActivityResult(request.contract) {
        request.deliverResult(activity, it)
    }
}

@VisibleForTesting
internal suspend fun ActivityNavigator.collectAndHandleActivityEvents(
    lifecycle: Lifecycle,
    activityStarter: (ActivityRoute, NavRoute?) -> Unit,
    activityLaunchers: Map<ActivityResultContractRequest<*, *, *>, ActivityResultLauncher<*>>,
) {
    // Following comment https://github.com/Kotlin/kotlinx.coroutines/issues/2886#issuecomment-901188295,
    // the events could be lost due to the prompt cancellation guarantee of Channel,
    // we must use `Dispatchers.Main.immediate` to receive events.
    //
    // Note, when calling this method from a Composable,
    // the dispatcher of the Compose Side-effect is [androidx.compose.ui.platform.AndroidUiDispatcher],
    // it does not execute coroutines immediately when the current thread is the main thread,
    // but performs the dispatch during a handler callback or choreographer animation frame stage,
    // whichever comes first. Basically, it has some certain delay compared to [Dispatchers.Main.immediate].
    // So we must switch to [Dispatchers.Main.immediate] before collecting events.
    withContext(Dispatchers.Main.immediate) {
        activityEvents.flowWithLifecycle(lifecycle, minActiveState = Lifecycle.State.RESUMED)
            .collect { event ->
                navigateTo(event, activityStarter, activityLaunchers)
            }
    }
}

private fun navigateTo(
    event: ActivityEvent,
    activityStarter: (ActivityRoute, NavRoute?) -> Unit,
    activityLaunchers: Map<ActivityResultContractRequest<*, *, *>, ActivityResultLauncher<*>>,
) {
    when (event) {
        is ActivityEvent.NavigateTo -> {
            activityStarter(event.route, event.fallbackRoute)
        }
        is ActivityEvent.NavigateForResult<*> -> {
            val request = event.request
            val launcher = activityLaunchers[request] ?: throw IllegalStateException(
                "No launcher registered for request with contract ${request.contract}!" +
                    "\nMake sure you called the appropriate ActivityNavigator.registerFor... method",
            )
            @Suppress("UNCHECKED_CAST")
            (launcher as ActivityResultLauncher<Any?>).launch(event.input)
        }
    }
}

@VisibleForTesting
internal fun <I, O, R> ActivityResultContractRequest<I, O, R>.deliverResult(activity: Activity, result: O) {
    deliverResult(result) {
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }
}

@VisibleForTesting
@Suppress("UNCHECKED_CAST")
internal inline fun <I, O, R> ActivityResultContractRequest<I, O, R>.deliverResult(
    result: O,
    shouldShowPermissionRationale: (String) -> Boolean,
) {
    when (this) {
        is ActivityResultRequest<*, *> -> onResult(result as R)
        is PermissionsResultRequest -> onResult(
            (result as Map<String, Boolean>).mapValues { (permission, granted) ->
                if (granted) {
                    PermissionsResultRequest.PermissionResult.Granted
                } else {
                    PermissionsResultRequest.PermissionResult.Denied(shouldShowPermissionRationale(permission))
                }
            },
        )
    }
}
