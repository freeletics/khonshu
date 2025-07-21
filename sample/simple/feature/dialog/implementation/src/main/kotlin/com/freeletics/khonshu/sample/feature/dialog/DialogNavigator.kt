package com.freeletics.khonshu.sample.feature.dialog

import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@ForScope(DialogRoute::class)
@SingleIn(DialogRoute::class)
@ContributesBinding(DialogRoute::class, binding<ActivityNavigator>())
class DialogNavigator(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator)
