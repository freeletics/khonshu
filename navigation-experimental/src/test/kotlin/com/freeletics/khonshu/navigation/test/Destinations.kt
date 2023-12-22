package com.freeletics.khonshu.navigation.test

import android.content.Intent
import com.freeletics.khonshu.navigation.ActivityDestination
import com.freeletics.khonshu.navigation.OverlayDestination
import com.freeletics.khonshu.navigation.ScreenDestination
import com.freeletics.khonshu.navigation.internal.ActivityDestinationId
import com.freeletics.khonshu.navigation.internal.DestinationId

internal val simpleRootDestination = ScreenDestination(DestinationId(SimpleRoot::class), null) {}
internal val otherRootDestination = ScreenDestination(DestinationId(OtherRoot::class), null) {}
internal val simpleRouteDestination = ScreenDestination(DestinationId(SimpleRoute::class), null) {}
internal val otherRouteDestination = OverlayDestination(DestinationId(OtherRoute::class), null) {}
internal val thirdRouteDestination = OverlayDestination(DestinationId(ThirdRoute::class), null) {}

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
