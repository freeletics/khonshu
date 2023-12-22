package com.freeletics.khonshu.sample.feature.screen

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
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute

@NavDestination(
    route = ScreenRoute::class,
    stateMachine = ScreenStateMachine::class,
)
@Composable
fun ScreenScreen(
    state: ScreenState,
    sendAction: (ScreenAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText("Feature Screen ${state.number}")

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(ScreenAction.ScreenButtonClicked) },
            text = "Open Screen",
        )

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(ScreenAction.DialogButtonClicked) },
            text = "Open Dialog",
        )

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(ScreenAction.BottomSheetButtonClicked) },
            text = "Open Bottom Sheet",
        )

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(ScreenAction.ReplaceAllButtonClicked) },
            text = "Replace all with New Root",
        )
    }
}
