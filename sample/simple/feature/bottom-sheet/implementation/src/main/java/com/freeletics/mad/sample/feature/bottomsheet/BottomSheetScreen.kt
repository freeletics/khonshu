package com.freeletics.mad.sample.feature.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freeletics.mad.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.mad.whetstone.compose.ComposeDestination
import com.freeletics.mad.whetstone.compose.DestinationType

@ComposeDestination(
    route = BottomSheetRoute::class,
    stateMachine = BottomSheetStateMachine::class,
    destinationType = DestinationType.BOTTOM_SHEET,
)
@Composable
fun BottomSheetScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText("Feature BottomSheet")
    }
}
