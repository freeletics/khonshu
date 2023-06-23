package com.freeletics.khonshu.navigation.compose.test

import android.content.Intent
import com.freeletics.khonshu.navigation.compose.ActivityDestination
import com.freeletics.khonshu.navigation.compose.OverlayDestination
import com.freeletics.khonshu.navigation.compose.ScreenDestination
import com.freeletics.khonshu.navigation.internal.ActivityDestinationId
import com.freeletics.khonshu.navigation.internal.DestinationId

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
