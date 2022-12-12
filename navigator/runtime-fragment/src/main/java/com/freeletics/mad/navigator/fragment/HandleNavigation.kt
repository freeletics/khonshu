package com.freeletics.mad.navigator.fragment

import android.content.Context
import android.os.Parcelable
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.internal.AndroidXNavigationExecutor
import com.freeletics.mad.navigator.internal.NavigationExecutor
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.internal.collectAndHandleNavEvents
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
        it.registerIn(fragment, fragment.requireContext())
    }

    val lifecycle = fragment.lifecycle

    val executor = AndroidXNavigationExecutor(fragment.findNavController())
    navigator.navigationResultRequests.forEach {
        it.registerIn(executor, lifecycle)
    }

    val dispatcher = fragment.requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(fragment, navigator.onBackPressedCallback)

    lifecycle.coroutineScope.launch {
        navigator.collectAndHandleNavEvents(
            lifecycle, executor, activityLaunchers, permissionLaunchers)
    }
}

private fun <I, O> ActivityResultRequest<I, O>.registerIn(
    caller: ActivityResultCaller
): ActivityResultLauncher<*> {
    return caller.registerForActivityResult(contract, ::handleResult)
}

private fun PermissionsResultRequest.registerIn(
    caller: ActivityResultCaller,
    context: Context
): ActivityResultLauncher<List<String>> {
    return caller.registerForActivityResult(RequestPermissionsContract()) { resultMap ->
        handleResult(resultMap, context)
    }
}

private fun <O : Parcelable> NavigationResultRequest<O>.registerIn(
    executor: NavigationExecutor,
    lifecycle: Lifecycle,
) {
    lifecycle.coroutineScope.launch {
        val savedStateHandle = executor.savedStateHandleFor(key.route)
        savedStateHandle.getStateFlow<Parcelable>(key.requestKey, InitialValue)
            .collect { result ->
                if (result != InitialValue) {
                    @Suppress("UNCHECKED_CAST")
                    handleResult(result as O)
                    savedStateHandle[key.requestKey] = InitialValue
                }
            }
    }
}

@Parcelize
private object InitialValue : Parcelable
