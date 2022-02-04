package com.freeletics.mad.navigator.fragment

import android.os.Parcelable
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.internal.InternalNavigatorApi

/**
 * An extension to [NavEventNavigator] that adds support for `Fragment` result APIs. See
 * [registerForFragmentResult] and [navigateBackWithResult].
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
public abstract class FragmentNavEventNavigator : NavEventNavigator() {

    private val _fragmentResultRequests = mutableListOf<FragmentResultRequest<*>>()

    /**
     * Register for receiving fragment results for the given [requestKey].
     *
     * The returned [FragmentResultRequest] can be used to collect incoming permission results (via
     * [FragmentResultRequest.results]).
     *
     * Note: This has to be called *before* this [FragmentNavEventNavigator] gets attached to a fragment.
     *   In practice, this means it should usually be called during initialisation of your subclass.
     */
    public fun <O> registerForFragmentResult(requestKey: String): FragmentResultRequest<O> {
        checkAllowedToAddRequests()
        val request = FragmentResultRequest<O>(requestKey)
        _fragmentResultRequests.add(request)
        return request
    }

    /**
     * Triggers a new [NavEvent] that sets the fragment [result] for the given [requestKey] and then
     * navigates back to the previous destination.
     */
    public fun <T : Parcelable> navigateBackWithResult(requestKey: String, result: T) {
        val event = FragmentResultEvent(requestKey, result)
        sendNavEvent(event)
        navigateBack()
    }

    private var allowedToAddRequests = true

    private fun checkAllowedToAddRequests() {
        check(allowedToAddRequests) {
            "Failed to register for result! You must call this before this navigator " +
                "gets attached to a fragment, e.g. during initialisation of your navigator subclass."
        }
    }

    /**
     * Access to [FragmentResultRequest] objects that were registered with
     * [registerForFragmentResult]. A `NavEventNavigationHandler` can use these to register
     * the requests during setup so that results are delivered to them.
     */
    @InternalNavigatorApi
    public val fragmentResultRequests: List<FragmentResultRequest<*>>
        get() {
            allowedToAddRequests = false
            return _fragmentResultRequests.toList()
        }
}
