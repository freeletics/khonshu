package com.freeletics.mad.navigator.compose

import android.os.Parcelable
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.NavigationExecutor
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.internal.collectAndHandleNavEvents
import kotlinx.parcelize.Parcelize

/**
 * Sets up the [NavEventNavigator] inside the current composition so that it's events
 * are handled while the composition is active.
 */
@Composable
public fun NavigationSetup(navigator: NavEventNavigator) {
    val executor = LocalNavigationExecutor.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activityLaunchers = navigator.activityResultRequests.associateWith {
        rememberResultLaunchers(it)
    }
    val permissionLaunchers = navigator.permissionsResultRequests.associateWith {
        rememberResultLaunchers(it)
    }

    navigator.navigationResultRequests.forEach {
        ResultEffect(it, executor)
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
    DisposableEffect(lifecycleOwner, backDispatcher, navigator) {
        backDispatcher.addCallback(lifecycleOwner, navigator.onBackPressedCallback)

        onDispose {
            navigator.onBackPressedCallback.remove()
        }
    }

    LaunchedEffect(lifecycleOwner, executor, navigator) {
        navigator.collectAndHandleNavEvents(
            lifecycleOwner.lifecycle, executor, activityLaunchers, permissionLaunchers)
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
        request.handleResult(resultMap, context)
    }
}

@Composable
private fun <O : Parcelable> ResultEffect(
    request: NavigationResultRequest<O>,
    executor: NavigationExecutor,
) {
    LaunchedEffect(request, executor) {
        val savedStateHandle = executor.savedStateHandleFor(request.key.route)
        savedStateHandle.getStateFlow<Parcelable>(request.key.requestKey, InitialValue)
            .collect { result ->
                if (result != InitialValue) {
                    @Suppress("UNCHECKED_CAST")
                    request.handleResult(result as O)
                    savedStateHandle[request.key.requestKey] = InitialValue
                }
            }
    }
}

@Parcelize
private object InitialValue : Parcelable
