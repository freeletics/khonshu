package com.freeletics.mad.whetstone.compose.internal

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.freeletics.mad.whetstone.ComposeScreen
import com.freeletics.mad.navigator.Navigator
import com.freeletics.mad.navigator.compose.NavigationHandler

/**
 * Default value for the `navigationHandler` parameter of [ComposeScreen]. When the generator finds
 * this class as value it will skip generating navigation related code. This allows consumers to
 * not use [Navigator] and [NavigationHandler] based implementations and just have the
 * standard state machine and ui setup in the generated `Composable`.
 */
internal class EmptyNavigationHandler : NavigationHandler<EmptyNavigator> {
    init {
        throw UnsupportedOperationException("This is a marker class that should never be used")
    }

    @Composable
    override fun Navigation(navController: NavController, navigator: EmptyNavigator) {
        throw UnsupportedOperationException("This is a marker class that should never be used")
    }
}