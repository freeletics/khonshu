package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.OverlayDestination
import com.freeletics.khonshu.navigation.ScreenDestination
import com.freeletics.khonshu.navigation.internal.DestinationId

internal val simpleRootDestination = ScreenDestination(DestinationId(SimpleRoot::class), null, null) { _, _ -> }
internal val otherRootDestination = ScreenDestination(DestinationId(OtherRoot::class), null, null) { _, _ -> }
internal val simpleRouteDestination = ScreenDestination(DestinationId(SimpleRoute::class), null, null) { _, _ -> }
internal val otherRouteDestination = OverlayDestination(DestinationId(OtherRoute::class), null, null) { _, _ -> }
internal val thirdRouteDestination = OverlayDestination(DestinationId(ThirdRoute::class), null, null) { _, _ -> }
internal val fourthRouteDestination = ScreenDestination(DestinationId(FourthRoute::class), DestinationId(SimpleRoute::class), null) { _, _ -> }

internal val destinations = listOf(
    simpleRootDestination,
    otherRootDestination,
    simpleRouteDestination,
    otherRouteDestination,
    thirdRouteDestination,
    fourthRouteDestination,
)
