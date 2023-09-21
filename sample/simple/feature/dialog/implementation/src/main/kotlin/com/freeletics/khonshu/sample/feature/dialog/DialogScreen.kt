package com.freeletics.khonshu.sample.feature.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.freeletics.khonshu.codegen.compose.ComposeDestination
import com.freeletics.khonshu.codegen.compose.DestinationType
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute

@ComposeDestination(
    route = DialogRoute::class,
    stateMachine = DialogStateMachine::class,
    destinationType = DestinationType.OVERLAY,
)
@Composable
fun DialogScreen(
    state: DialogState,
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
            BasicText("Feature Dialog ${state.number}")

            Spacer(Modifier.height(12.dp))

            BasicText(
                modifier = Modifier.clickable { sendAction(DialogAction.ScreenButtonClicked) },
                text = "Open Screen",
            )

            Spacer(Modifier.height(12.dp))

            BasicText(
                modifier = Modifier.clickable { sendAction(DialogAction.DialogButtonClicked) },
                text = "Open Dialog",
            )

            Spacer(Modifier.height(12.dp))

            BasicText(
                modifier = Modifier.clickable { sendAction(DialogAction.BottomSheetButtonClicked) },
                text = "Open Bottom Sheet",
            )
        }
    }
}
