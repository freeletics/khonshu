package com.freeletics.sample.bottomsheet

import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.sample.bottomsheet.nav.BottomSheetRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@ForScope(BottomSheetRoute::class)
@SingleIn(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, binding<DestinationNavigator>())
class BottomSheetNavigator(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator)
