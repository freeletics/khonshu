package com.freeletics.mad.navigator.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.internal.navigate
import kotlinx.parcelize.Parcelize

/**
 * Sets up the [NavEventNavigator] inside the current composition so that it's events
 * are handled while the composition is active.
 */
@Composable
public fun NavigationSetup(navigator: NavEventNavigator) {
    val controller = LocalNavController.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activityLaunchers = navigator.activityResultRequests.associateWith {
        rememberResultLaunchers(it)
    }
    val permissionLaunchers = navigator.permissionsResultRequests.associateWith {
        rememberResultLaunchers(it)
    }

    navigator.navigationResultRequests.forEach {
        ResultEffect(it, controller)
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
            .flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState = RESUMED)
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

@Composable
private fun <O : Parcelable> ResultEffect(
    request: NavigationResultRequest<O>,
    controller: NavController,
) {
    LaunchedEffect(request, controller) {
        val backStackEntry = controller.getBackStackEntry(request.key.destinationId)

        backStackEntry
            .savedStateHandle
            .getStateFlow<Parcelable>(request.key.requestKey, InitialValue)
            .collect { result ->
                if (result != InitialValue) {
                    @Suppress("UNCHECKED_CAST")
                    request.handleResult(result as O)
                    backStackEntry.savedStateHandle[request.key.requestKey] = InitialValue
                }
            }
    }
}

@Parcelize
private object InitialValue : Parcelable
