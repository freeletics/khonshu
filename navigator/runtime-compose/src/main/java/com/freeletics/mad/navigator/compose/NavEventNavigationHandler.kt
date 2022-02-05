package com.freeletics.mad.navigator.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.navigate
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import kotlinx.coroutines.flow.collect

/**
 * A [NavigationHandler] that handles [NavEvent] emitted by a [NavEventNavigator].
 */
public class NavEventNavigationHandler : NavigationHandler<NavEventNavigator> {

    @Composable
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
                .collect { event ->
                    navigate(event, controller, activityLaunchers, permissionLaunchers)
                }
        }
    }

    @Composable
    private fun <I, O> rememberResultLaunchers(
        request: ActivityResultRequest<I, O>,
    ): ActivityResultLauncher<*> {
        return rememberLauncherForActivityResult(request.contract, request::handleResult)
    }

    @Composable
    private fun rememberResultLaunchers(
        request: PermissionsResultRequest,
    ): ActivityResultLauncher<List<String>> {
        val context = LocalContext.current
        return rememberLauncherForActivityResult(RequestPermissionsContract()) { resultMap ->
            request.handleResult(resultMap, context.findActivity())
        }
    }

    private fun Context.findActivity(): Activity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        throw IllegalStateException("Permissions should be called in the context of an Activity")
    }
}
