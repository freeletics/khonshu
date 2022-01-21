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
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavEvent.BackEvent
import com.freeletics.mad.navigator.NavEvent.BackToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToEvent
import com.freeletics.mad.navigator.NavEvent.ResultLauncherEvent
import com.freeletics.mad.navigator.NavEvent.UpEvent
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.GRANTED
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED
import com.freeletics.mad.navigator.ResultLauncher
import java.lang.IllegalArgumentException
import kotlinx.coroutines.flow.collect

/**
 * A [NavigationHandler] that handles [NavEvent] emitted by a [NavEventNavigator].
 */
public open class NavEventNavigationHandler : NavigationHandler<NavEventNavigator> {

    @Composable
    @OptIn(InternalNavigatorApi::class)
    @CallSuper
    override fun Navigation(navController: NavController, navigator: NavEventNavigator) {
        val lifecycleOwner = LocalLifecycleOwner.current

        val activityLaunchers = navigator.activityResultRequests.associateWith {
            rememberResultLaunchers(it)
        }
        val permissionLaunchers = navigator.permissionsResultRequests.associateWith {
            rememberResultLaunchers(it)
        }
        val launchers: Map<ResultLauncher<*>, ActivityResultLauncher<*>> = activityLaunchers + permissionLaunchers

        val backDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
        DisposableEffect(lifecycleOwner, backDispatcher, navigator) {
            backDispatcher.addCallback(lifecycleOwner, navigator.onBackPressedCallback)

            onDispose {
                navigator.onBackPressedCallback.remove()
            }
        }

        LaunchedEffect(lifecycleOwner, navController, navigator) {
            navigator.navEvents
                .flowWithLifecycle(lifecycleOwner.lifecycle)
                .collect { navEvent ->
                    navigate(navController, launchers, navEvent)
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
        resultLaunchers: Map<ResultLauncher<*>, ActivityResultLauncher<*>>,
        navEvent: NavEvent
    ) {
        when (navEvent) {
            is NavigateToEvent -> {
                controller.navigate(
                    navEvent.navRoute.destinationId,
                    navEvent.navRoute.getArguments(),
                    navEvent.navOptions,
                )
            }
            is UpEvent -> {
                controller.navigateUp()
            }
            is BackEvent -> {
                controller.popBackStack()
            }
            is BackToEvent -> {
                controller.popBackStack(navEvent.destinationId, navEvent.inclusive)
            }
            is ResultLauncherEvent<*> -> {
                val request = navEvent.resultLauncher
                val launcher = resultLaunchers[request] ?: throw IllegalStateException(
                    "No launcher registered for $request!\nMake sure you called the appropriate " +
                        "AbstractNavigator.registerFor... method"
                )
                @Suppress("UNCHECKED_CAST")
                (launcher as ActivityResultLauncher<Any?>).launch(navEvent.input)
            }
            else -> throw IllegalArgumentException("Unknown NavEvent $navEvent")
        }
    }
}
