package com.freeletics.mad.sample.feature.screen

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
import com.freeletics.mad.sample.feature.screen.nav.ScreenRoute
import com.freeletics.mad.whetstone.compose.ComposeDestination

@ComposeDestination(
    route = ScreenRoute::class,
    stateMachine = ScreenStateMachine::class,
)
@Composable
fun ScreenScreen(
    sendAction: (ScreenAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText("Feature Screen")

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(ScreenAction.BottomSheetButtonClicked) },
            text = "Open Bottom Sheet",
        )
    }
}