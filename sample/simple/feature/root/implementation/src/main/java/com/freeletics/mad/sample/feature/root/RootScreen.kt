package com.freeletics.mad.sample.feature.root

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
import com.freeletics.mad.sample.feature.root.nav.RootRoute
import com.freeletics.mad.whetstone.compose.ComposeDestination

@ComposeDestination(
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
            modifier = Modifier.clickable { sendAction(RootAction.BottomSheetButtonClicked) },
            text = "Open Bottom Sheet",
        )
    }
}
