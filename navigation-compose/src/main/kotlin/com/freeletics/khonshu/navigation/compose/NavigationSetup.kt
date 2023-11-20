package com.freeletics.khonshu.navigation.compose

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
import com.freeletics.khonshu.navigation.ContractResultOwner
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import com.freeletics.khonshu.navigation.internal.collectAndHandleNavEvents
import com.freeletics.khonshu.navigation.internal.collectAndHandleNavigationResults
import com.freeletics.khonshu.navigation.internal.deliverResult

/**
 * Sets up the [NavEventNavigator] inside the current composition so that it's events
 * are handled while the composition is active.
 */
@Composable
public fun NavigationSetup(navigator: NavEventNavigator, executor: NavigationExecutor = LocalNavigationExecutor.current) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val activityLaunchers = navigator.activityResultRequests.associateWith {
        rememberResultLaunchers(it, context)
    }

    navigator.navigationResultRequests.forEach {
        LaunchedEffect(executor, it) {
            executor.collectAndHandleNavigationResults(it)
        }
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
    DisposableEffect(backDispatcher, navigator) {
        backDispatcher.addCallback(navigator.onBackPressedCallback)

        onDispose {
            navigator.onBackPressedCallback.remove()
        }
    }

    LaunchedEffect(lifecycleOwner, executor, navigator) {
        navigator.collectAndHandleNavEvents(lifecycleOwner.lifecycle, executor, activityLaunchers)
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

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be requested in the context of an Activity")
}
