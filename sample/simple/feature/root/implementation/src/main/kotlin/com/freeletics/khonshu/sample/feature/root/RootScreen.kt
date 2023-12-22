package com.freeletics.khonshu.sample.feature.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freeletics.khonshu.codegen.NavDestination
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute

@NavDestination(
    route = RootRoute::class,
    stateMachine = RootStateMachine::class,
)
@Composable
fun RootScreen(
    sendAction: (RootAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText("Feature Root")

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(RootAction.ScreenButtonClicked) },
            text = "Open Screen",
        )

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(RootAction.DialogButtonClicked) },
            text = "Open Dialog",
        )

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(RootAction.BottomSheetButtonClicked) },
            text = "Open Bottom Sheet",
        )

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(RootAction.ReplaceAllButtonClicked) },
            text = "Replace all with New Root",
        )
    }
}
