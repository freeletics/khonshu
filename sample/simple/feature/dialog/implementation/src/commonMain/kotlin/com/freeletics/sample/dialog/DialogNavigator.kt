package com.freeletics.sample.dialog

import com.freeletics.khonshu.navigation.DestinationNavigator2
import com.freeletics.sample.dialog.nav.DialogRoute
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@ForScope(DialogRoute::class)
@SingleIn(DialogRoute::class)
class DialogNavigator(
    destinationNavigator: DestinationNavigator2,
) : DestinationNavigator2 by destinationNavigator
