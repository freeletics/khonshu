package com.freeletics.mad.navigator.fragment

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigator.ContractResultOwner
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.internal.AndroidXNavigationExecutor
import com.freeletics.mad.navigator.internal.collectAndHandleNavEvents
import com.freeletics.mad.navigator.internal.collectAndHandleNavigationResults
import com.freeletics.mad.navigator.internal.deliverResult
import kotlinx.coroutines.launch

/**
 * Handles the [NavEventNavigator] events while the Fragment's lifecycle is at least
 * started.
 */
public fun handleNavigation(fragment: Fragment, navigator: NavEventNavigator) {
    val activityLaunchers = navigator.activityResultRequests.associateWith {
        it.registerIn(fragment, fragment.requireActivity())
    }

    val lifecycle = fragment.lifecycle

    val executor = AndroidXNavigationExecutor(fragment.findNavController())
    navigator.navigationResultRequests.forEach {
        lifecycle.coroutineScope.launch {
            executor.collectAndHandleNavigationResults(it)
        }
    }

    val dispatcher = fragment.requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(fragment, navigator.onBackPressedCallback)

    lifecycle.coroutineScope.launch {
        navigator.collectAndHandleNavEvents(lifecycle, executor, activityLaunchers)
    }
}

private fun <I, O, R> ContractResultOwner<I, O, R>.registerIn(
    caller: ActivityResultCaller,
    activity: Activity,
): ActivityResultLauncher<*> {
    return caller.registerForActivityResult(contract) {
        deliverResult(activity, it)
    }
}
