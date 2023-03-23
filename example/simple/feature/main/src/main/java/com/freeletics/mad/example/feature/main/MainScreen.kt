package com.freeletics.mad.example.feature.main

import androidx.compose.runtime.Composable
import com.freeletics.mad.example.feature.a.nav.FeatureARoute
import com.freeletics.mad.navigator.compose.NavHost
import com.freeletics.mad.whetstone.compose.ComposeScreen

@ComposeScreen(
    scope = Main::class,
    stateMachine = MainStateMachine::class,
)
@Composable
internal fun MainScreen(
    destinationsHolder: DestinationsHolder,
) {
    NavHost(
        startRoute = FeatureARoute,
        destinations = destinationsHolder.destinations,
    )
}
