package com.freeletics.mad.example.feature.a

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.freeletics.mad.example.feature.a.nav.FeatureARoute
import com.freeletics.mad.whetstone.compose.ComposeDestination

@ComposeDestination(
    route = FeatureARoute::class,
    stateMachine = FeatureAStateMachine::class,
)
@Composable
fun FeatureAScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        BasicText("Feature A")
    }
}
