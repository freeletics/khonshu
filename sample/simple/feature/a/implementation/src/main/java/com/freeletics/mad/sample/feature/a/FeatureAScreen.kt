package com.freeletics.mad.sample.feature.a

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freeletics.mad.sample.feature.a.nav.FeatureARoute
import com.freeletics.mad.whetstone.compose.ComposeDestination

@ComposeDestination(
    route = FeatureARoute::class,
    stateMachine = FeatureAStateMachine::class,
)
@Composable
fun FeatureAScreen(
    sendAction: (FeatureAAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText("Feature A")

        Spacer(Modifier.height(12.dp))

        BasicText(
            modifier = Modifier.clickable { sendAction(FeatureAAction.ButtonClicked) },
            text = "Open Bottom Sheet",
        )
    }
}
