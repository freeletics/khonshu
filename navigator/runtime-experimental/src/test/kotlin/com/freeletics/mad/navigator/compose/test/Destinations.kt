package com.freeletics.mad.navigator.compose.test

import android.content.Intent
import com.freeletics.mad.navigator.compose.ActivityDestination
import com.freeletics.mad.navigator.compose.BottomSheetDestination
import com.freeletics.mad.navigator.compose.DialogDestination
import com.freeletics.mad.navigator.compose.ScreenDestination
import com.freeletics.mad.navigator.internal.ActivityDestinationId
import com.freeletics.mad.navigator.internal.DestinationId

internal val simpleRootDestination = ScreenDestination(DestinationId(SimpleRoot::class)) {}
internal val otherRootDestination = ScreenDestination(DestinationId(OtherRoot::class)) {}
internal val simpleRouteDestination = ScreenDestination(DestinationId(SimpleRoute::class)) {}
internal val otherRouteDestination = DialogDestination(DestinationId(OtherRoute::class)) {}
internal val thirdRouteDestination = BottomSheetDestination(DestinationId(ThirdRoute::class)) {}

internal val destinations = listOf(
    simpleRootDestination,
    otherRootDestination,
    simpleRouteDestination,
    otherRouteDestination,
    thirdRouteDestination,
)

internal val simpleActivityDestination = ActivityDestination(ActivityDestinationId(SimpleActivity::class), Intent())
internal val otherActivityDestination = ActivityDestination(ActivityDestinationId(OtherActivity::class), Intent())

internal val activityDestinations = listOf(
    simpleActivityDestination,
    otherActivityDestination,
)
