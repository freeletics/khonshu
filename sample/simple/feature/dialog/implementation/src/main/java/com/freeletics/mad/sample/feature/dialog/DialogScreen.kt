package com.freeletics.mad.sample.feature.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freeletics.mad.sample.feature.dialog.nav.DialogRoute
import com.freeletics.mad.whetstone.compose.ComposeDestination
import com.freeletics.mad.whetstone.compose.DestinationType

@ComposeDestination(
    route = DialogRoute::class,
    stateMachine = DialogStateMachine::class,
    destinationType = DestinationType.DIALOG,
)
@Composable
fun DialogScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText("Feature Dialog")
    }
}
