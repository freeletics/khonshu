package com.freeletics.mad.whetstone.fragment.internal

import androidx.fragment.app.Fragment
import com.freeletics.mad.whetstone.ComposeFragment
import com.freeletics.mad.whetstone.RendererFragment
import com.freeletics.mad.navigator.Navigator
import com.freeletics.mad.navigator.fragment.NavigationHandler

/**
 * Default value for the `navigationHandler` parameter of [ComposeFragment] and [RendererFragment].
 * When the generator finds this class as value it will skip generating navigation related code.
 * This allows consumers to not use [Navigator] and [NavigationHandler] based
 * implementations and  just have the standard state machine and ui setup in the generated
 * `Fragment`.
 */
internal class EmptyNavigationHandler : NavigationHandler<EmptyNavigator> {
    init {
        throw UnsupportedOperationException("This is a marker class that should never be used")
    }

    override fun handle(fragment: Fragment, navigator: EmptyNavigator) {
        throw UnsupportedOperationException("This is a marker class that should never be used")
    }
}
