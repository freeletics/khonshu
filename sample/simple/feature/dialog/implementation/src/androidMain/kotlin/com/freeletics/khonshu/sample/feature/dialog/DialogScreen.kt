package com.freeletics.khonshu.sample.feature.dialog

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
import androidx.compose.ui.window.Dialog
import com.freeletics.khonshu.codegen.NavDestination
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute

@NavDestination(
    route = DialogRoute::class,
    stateMachine = DialogStateMachine::class,
)
@Composable
fun DialogScreen(
    sendAction: (DialogAction) -> Unit,
) {
    Dialog(onDismissRequest = { sendAction(DialogAction.DismissRequested) }) {
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
}
