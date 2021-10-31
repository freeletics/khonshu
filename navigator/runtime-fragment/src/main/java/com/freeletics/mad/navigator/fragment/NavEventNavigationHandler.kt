package com.freeletics.mad.navigator.fragment

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.CallSuper
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavEvent.BackEvent
import com.freeletics.mad.navigator.NavEvent.BackToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToEvent
import com.freeletics.mad.navigator.NavEvent.ResultLauncherEvent
import com.freeletics.mad.navigator.NavEvent.UpEvent
import com.freeletics.mad.navigator.fragment.result.FragmentResultRequest
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.GRANTED
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED
import com.freeletics.mad.navigator.ResultLauncher
import com.freeletics.mad.navigator.internal.RequestPermissionsContract
import java.lang.IllegalArgumentException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A [NavigationHandler] that handles [NavEvent] emitted by a [NavEventNavigator].
 */
public open class NavEventNavigationHandler : NavigationHandler<FragmentNavEventNavigator> {

    @OptIn(InternalNavigatorApi::class)
    @CallSuper
    override fun handle(fragment: Fragment, navigator: FragmentNavEventNavigator) {
        val activityLaunchers = navigator.activityResultRequests.associateWith {
            it.registerIn(fragment)
        }
        val permissionLaunchers = navigator.permissionsResultRequests.associateWith {
            it.registerIn(fragment, fragment.requireActivity())
        }
        val launchers: Map<ResultLauncher<*>, ActivityResultLauncher<*>> = activityLaunchers + permissionLaunchers

        navigator.fragmentResultRequests.forEach {
            it.registerIn(fragment.parentFragmentManager, fragment)
        }

        val dispatcher = fragment.requireActivity().onBackPressedDispatcher
        dispatcher.addCallback(fragment, navigator.onBackPressedCallback)

        val lifecycle = fragment.lifecycle
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigator.navEvents.collect { navEvent ->
                    navigate(fragment, launchers, navEvent)
                }
            }
        }
    }

    private fun <I, O> ActivityResultRequest<I, O>.registerIn(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<*> {
        return caller.registerForActivityResult(contract, ::onResult)
    }

    @OptIn(InternalNavigatorApi::class)
    private fun PermissionsResultRequest.registerIn(
        caller: ActivityResultCaller,
        activity: Activity
    ): ActivityResultLauncher<List<String>> {
        return caller.registerForActivityResult(RequestPermissionsContract()) { resultMap ->
            val permissionsResult = resultMap.mapValues { (permission, granted) ->
                when {
                    granted -> GRANTED
                    shouldShowRequestPermissionRationale(activity, permission) -> DENIED
                    else -> DENIED_PERMANENTLY
                }
            }
            onResult(permissionsResult)
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
        resultLaunchers: Map<ResultLauncher<*>, ActivityResultLauncher<*>>,
        navEvent: NavEvent
    ) {
        when (navEvent) {
            is NavigateToEvent -> {
                val controller = fragment.findNavController()
                controller.navigate(
                    navEvent.navRoute.destinationId,
                    navEvent.navRoute.getArguments(),
                    navEvent.navOptions,
                )
            }
            is UpEvent -> {
                val controller = fragment.findNavController()
                controller.navigateUp()
            }
            is BackEvent -> {
                val controller = fragment.findNavController()
                controller.popBackStack()
            }
            is BackToEvent -> {
                val controller = fragment.findNavController()
                controller.popBackStack(navEvent.destinationId, navEvent.inclusive)
            }
            is ResultLauncherEvent<*> -> {
                val request = navEvent.resultLauncher
                val launcher = resultLaunchers[request] ?: throw IllegalStateException(
                    "No launcher registered for $request!\nMake sure you called the appropriate " +
                        "AbstractNavigator.registerFor... method"
                )
                @Suppress("UNCHECKED_CAST")
                (launcher as ActivityResultLauncher<Any?>).launch(navEvent.input)
            }
            is FragmentResultEvent -> {
                val result = Bundle(1).apply {
                    putParcelable(KEY_FRAGMENT_RESULT, navEvent.result)
                }
                fragment.parentFragmentManager.setFragmentResult(navEvent.requestKey, result)
            }
            else -> throw IllegalArgumentException("Unknown NavEvent $navEvent")
        }
    }
}

/**
 * Internal key used to store the result data in the result [Bundle].
 */
private const val KEY_FRAGMENT_RESULT = "fragment_result"
