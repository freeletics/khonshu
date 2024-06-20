package com.freeletics.khonshu.sample.feature.screen.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freeletics.khonshu.codegen.NavDestination
import com.freeletics.khonshu.sample.feature.screen.result.nav.ScreenWithResultRoute

@NavDestination(
    route = ScreenWithResultRoute::class,
    stateMachine = ScreenWithResultStateMachine::class,
)
@Composable
fun ScreenWithResultScreen(
    state: ScreenWithResultState,
    sendAction: (ScreenWithResultAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(
            "Feature Screen with Result",
            style = TextStyle.Default.copy(
                fontWeight = FontWeight.Bold,
            ),
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
        ) {
            val input = remember {
                mutableStateOf(state.data)
            }
            TextField(
                modifier = Modifier.weight(1f),
                value = input.value,
                onValueChange = {
                    input.value = it
                    sendAction(ScreenWithResultAction.UpdateResult(it))
                },
            )

            Spacer(Modifier.width(12.dp))

            Button(onClick = { sendAction(ScreenWithResultAction.DeliverResult) }) {
                BasicText("Send result")
            }
        }
    }
}
