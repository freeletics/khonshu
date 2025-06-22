package com.freeletics.sample.screenwithresult

import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.deliverNavigationResult
import com.freeletics.sample.screenwithresult.nav.Result
import com.freeletics.sample.screenwithresult.nav.ScreenWithResultRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@ForScope(ScreenWithResultRoute::class)
@SingleIn(ScreenWithResultRoute::class)
@ContributesBinding(ScreenWithResultRoute::class, binding<DestinationNavigator>())
class ScreenWithResultNavigator(
    hostNavigator: HostNavigator,
    private val route: ScreenWithResultRoute,
) : DestinationNavigator(hostNavigator) {
    fun deliverResult(data: String) {
        deliverNavigationResult(route.key, Result(data))
    }
}
