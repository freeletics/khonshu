package com.freeletics.sample.bottomsheet

import com.freeletics.khonshu.navigation.DestinationNavigator2
import com.freeletics.sample.bottomsheet.nav.BottomSheetRoute
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@ForScope(BottomSheetRoute::class)
@SingleIn(BottomSheetRoute::class)
class BottomSheetNavigator(
    destinationNavigator: DestinationNavigator2,
) : DestinationNavigator2 by destinationNavigator
