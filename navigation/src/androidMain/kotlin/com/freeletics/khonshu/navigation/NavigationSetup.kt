package com.freeletics.khonshu.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Parcelable
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.freeletics.khonshu.navigation.internal.ActivityStarter
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.NavEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.VisibleForTesting

/**
 * Sets up the [ActivityNavigator] and [NavEventNavigator] inside the current composition so that it's events
 * are handled while the composition is active.
 */
@Composable
public fun NavigationSetup(navigator: ActivityNavigator) {
    val hostNavigator = LocalHostNavigator.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val activityStarter = remember(context, hostNavigator) {
        ActivityStarter(context, hostNavigator)
    }

    val activityLaunchers = navigator.activityResultRequests.associateWith {
        rememberResultLaunchers(it, context)
    }

    if (navigator is NavEventNavigator) {
        navigator.navigationResultRequests.forEach {
            LaunchedEffect(hostNavigator, it) {
                hostNavigator.collectAndHandleNavigationResults(it)
            }
        }

        val backDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
        DisposableEffect(backDispatcher, navigator) {
            backDispatcher.addCallback(navigator.onBackPressedCallback)

            onDispose {
                navigator.onBackPressedCallback.remove()
            }
        }
    }

    LaunchedEffect(lifecycleOwner, hostNavigator, activityStarter, navigator) {
        navigator.collectAndHandleNavEvents(
            lifecycleOwner.lifecycle,
            hostNavigator,
            activityStarter::start,
            activityLaunchers,
        )
    }
}

@Composable
private fun <I, O> rememberResultLaunchers(
    request: ContractResultOwner<I, O, *>,
    context: Context,
): ActivityResultLauncher<*> {
    return rememberLauncherForActivityResult(request.contract) {
        request.deliverResult(context.findActivity(), it)
    }
}

@VisibleForTesting
internal suspend fun ActivityNavigator.collectAndHandleNavEvents(
    lifecycle: Lifecycle,
    hostNavigator: HostNavigator,
    activityStarter: (ActivityRoute, NavRoute?) -> Unit,
    activityLaunchers: Map<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>,
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
        navEvents.flowWithLifecycle(lifecycle, minActiveState = Lifecycle.State.RESUMED)
            .collect { event ->
                navigateTo(hostNavigator, activityStarter, hostNavigator, event, activityLaunchers)
            }
    }
}

private fun navigateTo(
    hostNavigator: HostNavigator,
    activityStarter: (ActivityRoute, NavRoute?) -> Unit,
    navigator: Navigator,
    event: NavEvent,
    activityLaunchers: Map<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>,
) {
    when (event) {
        is NavEvent.NavigateToEvent -> {
            navigator.navigateTo(event.route)
        }
        is NavEvent.NavigateToRootEvent -> {
            navigator.navigateToRoot(event.root, event.restoreRootState)
        }
        is NavEvent.NavigateToActivityEvent -> {
            activityStarter(event.route, event.fallbackRoute)
        }
        is NavEvent.UpEvent -> {
            navigator.navigateUp()
        }
        is NavEvent.BackEvent -> {
            navigator.navigateBack()
        }
        is NavEvent.BackToEvent -> {
            navigator.navigateBackTo(event.popUpTo, event.inclusive)
        }
        is NavEvent.ResetToRoot -> {
            navigator.resetToRoot(event.root)
        }
        is NavEvent.ReplaceAll -> {
            navigator.replaceAll(event.root)
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
            val entry = hostNavigator.snapshot.value.entryFor(event.key.destinationId)
            entry.savedStateHandle[event.key.requestKey] = event.result
        }
        is NavEvent.MultiNavEvent -> {
            hostNavigator.navigate {
                event.navEvents.forEach {
                    navigateTo(hostNavigator, activityStarter, this@navigate, it, activityLaunchers)
                }
            }
        }
    }
}

@VisibleForTesting
internal fun <I, O, R> ContractResultOwner<I, O, R>.deliverResult(activity: Activity, result: O) {
    deliverResult(result) {
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }
}

@VisibleForTesting
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
                    PermissionsResultRequest.PermissionResult.Granted
                } else {
                    PermissionsResultRequest.PermissionResult.Denied(shouldShowPermissionRationale(permission))
                }
            },
        )
    }
}

@VisibleForTesting
internal suspend fun <R : Parcelable> HostNavigator.collectAndHandleNavigationResults(
    request: EventNavigationResultRequest<R>,
) {
    val savedStateHandle = snapshot.value.entryFor(request.key.destinationId).savedStateHandle
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
internal object InitialValue : Parcelable

@InternalNavigationCodegenApi
public val LocalHostNavigator: ProvidableCompositionLocal<HostNavigator> = staticCompositionLocalOf {
    throw IllegalStateException("Can't access HostNavigator outside of a NavHost")
}

internal tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("Could not find activity in Context chain.")
}
