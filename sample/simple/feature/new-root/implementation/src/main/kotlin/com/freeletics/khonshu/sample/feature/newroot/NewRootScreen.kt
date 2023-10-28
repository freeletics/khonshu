package com.freeletics.khonshu.sample.feature.newroot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.freeletics.khonshu.codegen.compose.NavDestination
import com.freeletics.khonshu.sample.feature.root.nav.NewRootRoute

@NavDestination(
    route = NewRootRoute::class,
    stateMachine = NewRootStateMachine::class,
)
@Composable
fun NewRootScreen(
    sendAction: (NewRootAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText("Feature New Root")
    }
}
