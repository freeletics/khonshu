package com.freeletics.sample.screenwithresult

import com.freeletics.khonshu.navigation.DestinationNavigator2
import com.freeletics.khonshu.navigation.deliverNavigationResult
import com.freeletics.sample.screenwithresult.nav.Result
import com.freeletics.sample.screenwithresult.nav.ScreenWithResultRoute
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ScreenWithResultRoute::class)
class ScreenWithResultNavigator(
    private val route: ScreenWithResultRoute,
    private val destinationNavigator: DestinationNavigator2,
) : DestinationNavigator2 by destinationNavigator {
    fun deliverResult(data: String) {
        deliverNavigationResult(route.key, Result(data))
    }
}
