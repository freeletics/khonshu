package com.freeletics.mad.sample.feature.main

import androidx.compose.runtime.Composable
import com.freeletics.mad.codegen.compose.ComposeScreen
import com.freeletics.mad.navigation.compose.NavDestination
import com.freeletics.mad.navigation.compose.NavHost
import com.freeletics.mad.sample.feature.root.nav.RootRoute

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
