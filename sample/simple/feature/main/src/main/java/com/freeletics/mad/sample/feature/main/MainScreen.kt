package com.freeletics.mad.sample.feature.main

import androidx.compose.runtime.Composable
import com.freeletics.mad.navigator.compose.NavDestination
import com.freeletics.mad.navigator.compose.NavHost
import com.freeletics.mad.sample.feature.root.nav.RootRoute
import com.freeletics.mad.whetstone.compose.ComposeScreen

@ComposeScreen(
    scope = MainActivity::class,
    stateMachine = MainStateMachine::class,
)
@Composable
internal fun MainScreen(
    destinations: Set<NavDestination>,
) {
    NavHost(
        startRoute = RootRoute,
        destinations = destinations,
    )
}
