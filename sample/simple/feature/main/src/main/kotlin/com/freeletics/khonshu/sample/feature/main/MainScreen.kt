package com.freeletics.khonshu.sample.feature.main

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import com.freeletics.khonshu.codegen.compose.NavHostActivity
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute

@NavHostActivity(
    stateMachine = MainStateMachine::class,
    activityBaseClass = ComponentActivity::class,
)
@Composable
internal fun MainScreen(
    navHost: @Composable (NavRoot, ((BaseRoute) -> Unit)?) -> Unit,
) {
    navHost(RootRoute) {}
}
