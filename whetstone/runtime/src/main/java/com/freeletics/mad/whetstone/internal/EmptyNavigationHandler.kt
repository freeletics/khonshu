package com.freeletics.mad.whetstone.internal

import androidx.activity.OnBackPressedDispatcher
import androidx.navigation.NavController
import com.freeletics.mad.navigator.ComposeNavigationHandler
import com.freeletics.mad.whetstone.ComposeFragment
import com.freeletics.mad.whetstone.ComposeScreen
import com.freeletics.mad.whetstone.RendererFragment
import com.freeletics.mad.navigator.NavigationHandler
import com.freeletics.mad.navigator.Navigator
import kotlinx.coroutines.CoroutineScope

/**
 * Default value for the `navigationHandler` parameter of [ComposeScreen], [ComposeFragment]
 * and [RendererFragment]. When the generator finds this class as value it will skip generating
 * navigation related code. This allows consumers to not use [Navigator] and [NavigationHandler]
 * based implementations and just have the standard state machine and ui setup in the generated
 * Fragment.
 */
internal class EmptyNavigationHandler : ComposeNavigationHandler<EmptyNavigator> {
    init {
        throw UnsupportedOperationException("This is a marker class that should never be used")
    }

    override fun handle(
        scope: CoroutineScope,
        navController: NavController,
        onBackPressedDispatcher: OnBackPressedDispatcher,
        navigator: EmptyNavigator
    ) {
        throw UnsupportedOperationException("This is a marker class that should never be used")
    }
}
