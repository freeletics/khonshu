package com.freeletics.mad.navigator.fragment

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.CallSuper
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
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import com.freeletics.mad.navigator.internal.navigate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A [NavigationHandler] that handles [NavEvent] emitted by a [NavEventNavigator].
 */
@OptIn(InternalNavigatorApi::class)
public open class NavEventNavigationHandler : NavigationHandler<FragmentNavEventNavigator> {

    @CallSuper
    override fun handle(fragment: Fragment, navigator: FragmentNavEventNavigator) {
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
                    navigate(fragment, activityLaunchers, permissionLaunchers, event)
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

    private fun <O> FragmentResultRequest<O>.registerIn(
        fragmentResultOwner: FragmentResultOwner,
        lifecycleOwner: LifecycleOwner
    ) {
        fragmentResultOwner.setFragmentResultListener(requestKey, lifecycleOwner) { _, bundle ->
            onResult(bundle.getParcelable(KEY_FRAGMENT_RESULT)!!)
        }
    }

    private fun navigate(
        fragment: Fragment,
        activityLaunchers: Map<ActivityResultRequest<*, *>, ActivityResultLauncher<*>>,
        permissionLaunchers: Map<PermissionsResultRequest, ActivityResultLauncher<List<String>>>,
        event: NavEvent
    ) {
        if (handleNavEvent(event)) {
            return
        }

        navigate(event, fragment.findNavController(), activityLaunchers, permissionLaunchers)
    }

    private fun navigate(fragment: Fragment, event: FragmentResultEvent) {
        val result = Bundle(1).apply {
            putParcelable(KEY_FRAGMENT_RESULT, event.result)
        }
        fragment.parentFragmentManager.setFragmentResult(event.requestKey, result)
        fragment.findNavController().popBackStack()
    }

    /**
     * This method can be overridden to handle custom [NavEvent] implementations or handle
     * the standard events in a different way.
     *
     * @return `true` if event was handled, `false` otherwise
     */
    protected open fun handleNavEvent(event: NavEvent): Boolean {
       return false
    }
}

/**
 * Internal key used to store the result data in the result [Bundle].
 */
private const val KEY_FRAGMENT_RESULT = "fragment_result"
