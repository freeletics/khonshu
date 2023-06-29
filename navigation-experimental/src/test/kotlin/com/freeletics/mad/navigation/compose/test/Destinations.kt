package com.freeletics.mad.navigation.compose.test

import android.content.Intent
import com.freeletics.mad.navigation.compose.ActivityDestination
import com.freeletics.mad.navigation.compose.OverlayDestination
import com.freeletics.mad.navigation.compose.ScreenDestination
import com.freeletics.mad.navigation.internal.ActivityDestinationId
import com.freeletics.mad.navigation.internal.DestinationId

internal val simpleRootDestination = ScreenDestination(DestinationId(SimpleRoot::class)) {}
internal val otherRootDestination = ScreenDestination(DestinationId(OtherRoot::class)) {}
internal val simpleRouteDestination = ScreenDestination(DestinationId(SimpleRoute::class)) {}
internal val otherRouteDestination = OverlayDestination(DestinationId(OtherRoute::class)) {}
internal val thirdRouteDestination = OverlayDestination(DestinationId(ThirdRoute::class)) {}

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
