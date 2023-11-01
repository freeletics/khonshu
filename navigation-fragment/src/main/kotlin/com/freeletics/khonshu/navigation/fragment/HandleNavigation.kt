package com.freeletics.khonshu.navigation.fragment

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.coroutineScope
import com.freeletics.khonshu.navigation.ContractResultOwner
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.internal.collectAndHandleNavEvents
import com.freeletics.khonshu.navigation.internal.collectAndHandleNavigationResults
import com.freeletics.khonshu.navigation.internal.deliverResult
import kotlinx.coroutines.launch

internal class HandleNavigationViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // This state is used to save the start route, so that we can update the start destination of the graph.
    // It is updated when NavEventNavigation#replaceAll is called or when the `startRoute` parameter changes.
    internal val savedStartRouteState = savedStateHandle.getStateFlow<NavRoot?>(
        key = "SAVED_START_ROUTE",
        initialValue = null,
    )

    init {
        println("$this init")
    }

    fun onSaveStartRoute(navRoot: NavRoot) {
        println("$this onSaveStartRoute $navRoot")
        savedStateHandle["SAVED_START_ROUTE"] = navRoot
    }
}

/**
 * Handles the [NavEventNavigator] events while the Fragment's lifecycle is at least
 * started.
 */
public fun handleNavigation(fragment: Fragment, navigator: NavEventNavigator) {
    val activityLaunchers = navigator.activityResultRequests.associateWith {
        it.registerIn(fragment, fragment.requireActivity())
    }

    val lifecycle = fragment.lifecycle

    val executor = fragment.findNavigationExecutor()
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
