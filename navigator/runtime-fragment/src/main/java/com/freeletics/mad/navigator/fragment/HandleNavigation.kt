package com.freeletics.mad.navigator.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.internal.navigate
import kotlinx.coroutines.launch

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

    val controller = fragment.findNavController()
    navigator.navigationResultRequest.forEach {
        it.registerIn(controller, fragment)
    }

    val dispatcher = fragment.requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(fragment, navigator.onBackPressedCallback)

    val lifecycle = fragment.lifecycle
    lifecycle.coroutineScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            navigator.navEvents.collect { event ->
                navigate(event, fragment.findNavController(), activityLaunchers, permissionLaunchers)
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
    lifecycleOwner: LifecycleOwner,
) {
    val liveData = controller.getBackStackEntry(key.destinationId).savedStateHandle
        .getLiveData<Parcelable>(key.requestKey)

    val observer = Observer<Parcelable> { result ->
        @Suppress("UNCHECKED_CAST")
        handleResult(result as O)
    }

    liveData.observe(lifecycleOwner, observer)
}

/**
 * Internal key used to store the result data in the result [Bundle].
 */
private const val KEY_FRAGMENT_RESULT = "fragment_result"
