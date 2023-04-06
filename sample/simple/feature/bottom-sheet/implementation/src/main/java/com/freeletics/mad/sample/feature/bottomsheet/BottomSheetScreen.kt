package com.freeletics.mad.sample.feature.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        BasicText("Feature BottomSheet")
    }
}
