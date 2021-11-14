package com.freeletics.mad.whetstone.internal

import com.freeletics.mad.whetstone.ComposeScreen
import com.freeletics.mad.navigator.Navigator

/**
 * Default value for the `navigator` parameter of [ComposeScreen]. When the generator finds
 * this class as value it will skip generating navigation related code. This allows consumers to
 * not use [Navigator] and [NavigationHandler] based implementations and just have the
 * standard state machine and ui setup in the generated `Composable`.
 */
internal class EmptyNavigator : Navigator {
    init {
        throw UnsupportedOperationException("This is a marker class that should never be used")
    }
}
