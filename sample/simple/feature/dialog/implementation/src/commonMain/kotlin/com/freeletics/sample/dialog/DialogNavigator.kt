package com.freeletics.sample.dialog

import com.freeletics.khonshu.navigation.DestinationNavigator2
import com.freeletics.sample.dialog.nav.DialogRoute
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(DialogRoute::class)
class DialogNavigator(
    private val destinationNavigator: DestinationNavigator2,
) : DestinationNavigator2 by destinationNavigator
