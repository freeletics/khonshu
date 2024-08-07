package com.freeletics.khonshu.sample.feature.bottomsheet

import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(BottomSheetRoute::class)
@SingleIn(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, ActivityNavigator::class)
class BottomSheetNavigator @Inject constructor(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator)
