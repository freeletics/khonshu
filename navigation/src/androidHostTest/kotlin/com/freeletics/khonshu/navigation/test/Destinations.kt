package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.OverlayDestination
import com.freeletics.khonshu.navigation.ScreenDestination
import com.freeletics.khonshu.navigation.internal.DestinationId
import kotlinx.serialization.serializer

internal val simpleRootDestination =
    ScreenDestination(DestinationId(SimpleRoot::class), serializer<SimpleRoot>(), null, null) { _, _ -> }
internal val otherRootDestination =
    ScreenDestination(DestinationId(OtherRoot::class), serializer<OtherRoot>(), null, null) { _, _ -> }
internal val simpleRouteDestination =
    ScreenDestination(DestinationId(SimpleRoute::class), serializer<SimpleRoute>(), null, null) { _, _ -> }
internal val otherRouteDestination =
    OverlayDestination(DestinationId(OtherRoute::class), serializer<OtherRoute>(), null, null) { _, _ -> }
internal val thirdRouteDestination =
    OverlayDestination(DestinationId(ThirdRoute::class), serializer<ThirdRoute>(), null, null) { _, _ -> }
internal val fourthRouteDestination =
    ScreenDestination(
        DestinationId(FourthRoute::class),
        serializer<FourthRoute>(),
        DestinationId(SimpleRoute::class),
        null,
    ) {
        _,
        _,
        ->
    }

internal val destinations = listOf(
    simpleRootDestination,
    otherRootDestination,
    simpleRouteDestination,
    otherRouteDestination,
    thirdRouteDestination,
    fourthRouteDestination,
)
