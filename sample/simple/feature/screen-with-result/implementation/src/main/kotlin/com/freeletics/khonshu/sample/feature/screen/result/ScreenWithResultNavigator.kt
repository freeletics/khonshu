package com.freeletics.khonshu.sample.feature.screen.result

import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.sample.feature.screen.result.nav.Result
import com.freeletics.khonshu.sample.feature.screen.result.nav.ScreenWithResultRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@ForScope(ScreenWithResultRoute::class)
@SingleIn(ScreenWithResultRoute::class)
@ContributesBinding(ScreenWithResultRoute::class, binding<ActivityNavigator>())
class ScreenWithResultNavigator(
    hostNavigator: HostNavigator,
    private val route: ScreenWithResultRoute,
) : DestinationNavigator(hostNavigator) {
    fun deliverResult(data: String) {
        deliverNavigationResult(route.key, Result(data))
        navigateBack()
    }
}
