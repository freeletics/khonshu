package com.freeletics.khonshu.sample.feature.main

import androidx.compose.runtime.Composable
import com.freeletics.khonshu.codegen.compose.ComposeScreen
import com.freeletics.khonshu.navigation.compose.NavDestination
import com.freeletics.khonshu.navigation.compose.NavHost
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute

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
