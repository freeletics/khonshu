package com.freeletics.mad.navigator.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavEvent.BackEvent
import com.freeletics.mad.navigator.NavEvent.BackToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToEvent
import com.freeletics.mad.navigator.NavEvent.ActivityResultEvent
import com.freeletics.mad.navigator.NavEvent.UpEvent
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.GRANTED
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED
import kotlinx.coroutines.flow.collect

/**
 * A [NavigationHandler] that handles [NavEvent] emitted by a [NavEventNavigator].
 */
public open class NavEventNavigationHandler : NavigationHandler<NavEventNavigator> {

    @Composable
    @OptIn(InternalNavigatorApi::class)
    @CallSuper
    override fun Navigation(navigator: NavEventNavigator) {
        val controller = LocalNavController.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val activityLaunchers = navigator.activityResultRequests.associateWith {
            rememberResultLaunchers(it)
        }
        val permissionLaunchers = navigator.permissionsResultRequests.associateWith {
            rememberResultLaunchers(it)
        }

        val backDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
        DisposableEffect(lifecycleOwner, backDispatcher, navigator) {
            backDispatcher.addCallback(lifecycleOwner, navigator.onBackPressedCallback)

            onDispose {
                navigator.onBackPressedCallback.remove()
            }
        }

        LaunchedEffect(lifecycleOwner, controller, navigator) {
            navigator.navEvents
                .flowWithLifecycle(lifecycleOwner.lifecycle)
                .collect { navEvent ->
                    navigate(controller, activityLaunchers, permissionLaunchers, navEvent)
                }
        }
    }

    @Composable
    private fun <I, O> rememberResultLaunchers(
        request: ActivityResultRequest<I, O>,
    ): ActivityResultLauncher<*> {
        return rememberLauncherForActivityResult(request.contract, request::onResult)
    }

    @Composable
    @OptIn(InternalNavigatorApi::class)
    private fun rememberResultLaunchers(
        request: PermissionsResultRequest,
    ): ActivityResultLauncher<List<String>> {
        val context = LocalContext.current
        return rememberLauncherForActivityResult(RequestPermissionsContract()) { resultMap ->
            val permissionsResult = resultMap.mapValues { (permission, granted) ->
                when {
                    granted -> GRANTED
                    context.shouldShowRequestPermissionRationale(permission) -> DENIED
                    else -> DENIED_PERMANENTLY
                }
            }
            request.onResult(permissionsResult)
        }
    }

    private fun Context.shouldShowRequestPermissionRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(findActivity(), permission)
    }

    private fun Context.findActivity(): Activity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        throw IllegalStateException("Permissions should be called in the context of an Activity")
    }

    private fun navigate(
        controller: NavController,
        activityLaunchers: Map<ActivityResultRequest<*, *>, ActivityResultLauncher<*>>,
        permissionLaunchers: Map<PermissionsResultRequest, ActivityResultLauncher<List<String>>>,
        event: NavEvent
    ) {
        if (handleNavEvent(event)) {
            return
        }

        when (event) {
            is NavigateToEvent -> {
                controller.navigate(
                    event.route.destinationId,
                    event.route.getArguments(),
                    event.options,
                )
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
                controller.navigate(
                    event.root.destinationId,
                    event.root.getArguments(),
                    options
                )
            }
            is UpEvent -> {
                controller.navigateUp()
            }
            is BackEvent -> {
                controller.popBackStack()
            }
            is BackToEvent -> {
                controller.popBackStack(event.destinationId, event.inclusive)
            }
            is ActivityResultEvent<*> -> {
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
            else -> throw IllegalArgumentException("Unknown NavEvent $event")
        }
    }

    /**
     * This method can be overridden to handle custom [NavEvent] implementations or handle
     * the standard events in a different way.
     *
     * @return `true` if event was handled, `false` otherwise
     */
    protected open fun handleNavEvent(event: NavEvent): Boolean {
        return false
    }
}
