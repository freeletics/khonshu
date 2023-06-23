package com.freeletics.khonshu.sample.feature.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freeletics.khonshu.codegen.compose.ComposeDestination
import com.freeletics.khonshu.codegen.compose.DestinationType
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute

@OptIn(ExperimentalMaterial3Api::class)
@ComposeDestination(
    route = BottomSheetRoute::class,
    stateMachine = BottomSheetStateMachine::class,
    destinationType = DestinationType.OVERLAY,
)
@Composable
fun BottomSheetScreen(
    state: BottomSheetState,
    sendAction: (BottomSheetAction) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = { sendAction(BottomSheetAction.DismissRequested) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText("Feature Bottom Sheet ${state.number}")

            Spacer(Modifier.height(12.dp))

            BasicText(
                modifier = Modifier.clickable { sendAction(BottomSheetAction.ScreenButtonClicked) },
                text = "Open Screen",
            )

            Spacer(Modifier.height(12.dp))

            BasicText(
                modifier = Modifier.clickable { sendAction(BottomSheetAction.DialogButtonClicked) },
                text = "Open Dialog",
            )

            Spacer(Modifier.height(12.dp))

            BasicText(
                modifier = Modifier.clickable { sendAction(BottomSheetAction.BottomSheetButtonClicked) },
                text = "Open Bottom Sheet",
            )
        }
    }
}
