package com.freeletics.mad.navigator.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.internal.navigate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Handles the [FragmentNavEventNavigator] events while the Fragment's lifecycle is at least
 * started.
 */
public fun handleNavigation(fragment: Fragment, navigator: FragmentNavEventNavigator) {
    val activityLaunchers = navigator.activityResultRequests.associateWith {
        it.registerIn(fragment)
    }
    val permissionLaunchers = navigator.permissionsResultRequests.associateWith {
        it.registerIn(fragment, fragment.requireActivity())
    }

    navigator.fragmentResultRequests.forEach {
        it.registerIn(fragment.parentFragmentManager, fragment)
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
    lifecycle.coroutineScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            navigator.resultEvents.collect { event ->
                navigate(fragment, event)
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

@SuppressLint("VisibleForTests") // it's ok to use onResult internally
private fun <O> FragmentResultRequest<O>.registerIn(
    fragmentResultOwner: FragmentResultOwner,
    lifecycleOwner: LifecycleOwner
) {
    fragmentResultOwner.setFragmentResultListener(requestKey, lifecycleOwner) { _, bundle ->
        onResult(bundle.getParcelable(KEY_FRAGMENT_RESULT)!!)
    }
}

private fun navigate(fragment: Fragment, event: FragmentResultEvent) {
    val result = Bundle(1).apply {
        putParcelable(KEY_FRAGMENT_RESULT, event.result)
    }
    fragment.parentFragmentManager.setFragmentResult(event.requestKey, result)
    fragment.findNavController().popBackStack()
}

/**
 * Internal key used to store the result data in the result [Bundle].
 */
private const val KEY_FRAGMENT_RESULT = "fragment_result"
