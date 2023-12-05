package com.freeletics.khonshu.sample.feature.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.freeletics.khonshu.codegen.compose.NavHostActivity
import com.freeletics.khonshu.codegen.compose.SimpleNavHost
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute

@NavHostActivity(
    stateMachine = MainStateMachine::class,
    activityBaseClass = ComponentActivity::class,
)
@Composable
internal fun MainScreen(
    navHost: SimpleNavHost,
) {
    navHost(RootRoute, Modifier.fillMaxSize()) {}
}
