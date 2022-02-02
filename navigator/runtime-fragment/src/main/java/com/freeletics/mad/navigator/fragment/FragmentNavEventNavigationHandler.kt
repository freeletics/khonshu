package com.freeletics.mad.navigator.fragment

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultOwner
import androidx.lifecycle.LifecycleOwner
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.fragment.result.FragmentResultRequest
import com.freeletics.mad.navigator.internal.InternalNavigatorApi

/**
 * A [NavigationHandler] that handles [NavEvent] emitted by a [FragmentNavEventNavigator].
 */
public open class FragmentNavEventNavigationHandler<T : FragmentNavEventNavigator>
    : NavEventNavigationHandler<T>() {

    @OptIn(InternalNavigatorApi::class)
    @CallSuper
    public override fun handle(fragment: Fragment, navigator: T) {
        navigator.fragmentResultRequests.forEach {
            it.registerIn(fragment.parentFragmentManager, fragment)
        }

        super.handle(fragment, navigator)
    }

    private fun <O> FragmentResultRequest<O>.registerIn(
        fragmentResultOwner: FragmentResultOwner,
        lifecycleOwner: LifecycleOwner
    ) {
        fragmentResultOwner.setFragmentResultListener(requestKey, lifecycleOwner) { _, bundle ->
            onResult(bundle.getParcelable(KEY_FRAGMENT_RESULT)!!)
        }
    }
    
    /**
     * This method can be overridden to handle custom [NavEvent] implementations or handle
     * the standard events in a different way.
     *
     * @return `true` if event was handled, `false` otherwise
     */
    protected override fun handleNavEvent(fragment: Fragment, event: NavEvent): Boolean {
        if (event is FragmentResultEvent) {
            val result = Bundle(1).apply {
                putParcelable(KEY_FRAGMENT_RESULT, event.result)
            }
            fragment.parentFragmentManager.setFragmentResult(event.requestKey, result)
            return true
        }
       return false
    }
}

/**
 * Internal key used to store the result data in the result [Bundle].
 */
private const val KEY_FRAGMENT_RESULT = "fragment_result"
