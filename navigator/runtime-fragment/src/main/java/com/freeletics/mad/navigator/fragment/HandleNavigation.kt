package com.freeletics.mad.navigator.fragment

import android.app.Activity
import android.os.Parcelable
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.AndroidXNavigationExecutor
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.internal.navigate
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

/**
 * Handles the [NavEventNavigator] events while the Fragment's lifecycle is at least
 * started.
 */
public fun handleNavigation(fragment: Fragment, navigator: NavEventNavigator) {
    val activityLaunchers = navigator.activityResultRequests.associateWith {
        it.registerIn(fragment)
    }
    val permissionLaunchers = navigator.permissionsResultRequests.associateWith {
        it.registerIn(fragment, fragment.requireActivity())
    }

    val lifecycle = fragment.lifecycle

    val controller = fragment.findNavController()
    navigator.navigationResultRequests.forEach {
        it.registerIn(controller, lifecycle)
    }

    val dispatcher = fragment.requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(fragment, navigator.onBackPressedCallback)

    val navigationExecutor = AndroidXNavigationExecutor(controller)

    lifecycle.coroutineScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            navigator.navEvents.collect { event ->
                navigate(event, navigationExecutor, activityLaunchers, permissionLaunchers)
            }
        }
    }
}

private fun <I, O> ActivityResultRequest<I, O>.registerIn(
    caller: ActivityResultCaller
): ActivityResultLauncher<*> {
    return caller.registerForActivityResult(contract, ::handleResult)
}

private fun PermissionsResultRequest.registerIn(
    caller: ActivityResultCaller,
    activity: Activity
): ActivityResultLauncher<List<String>> {
    return caller.registerForActivityResult(RequestPermissionsContract()) { resultMap ->
        handleResult(resultMap, activity)
    }
}

private fun <O : Parcelable> NavigationResultRequest<O>.registerIn(
    controller: NavController,
    lifecycle: Lifecycle,
) {
    val backStackEntry = controller.getBackStackEntry(key.destinationId)

    lifecycle.coroutineScope.launch {
        backStackEntry
            .savedStateHandle
            .getStateFlow<Parcelable>(key.requestKey, InitialValue)
            .collect { result ->
                if (result != InitialValue) {
                    @Suppress("UNCHECKED_CAST")
                    handleResult(result as O)
                    backStackEntry.savedStateHandle[key.requestKey] = InitialValue
                }
            }
    }
}

@Parcelize
private object InitialValue : Parcelable
