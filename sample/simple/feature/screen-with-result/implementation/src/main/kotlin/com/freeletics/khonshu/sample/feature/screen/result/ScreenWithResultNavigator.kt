package com.freeletics.khonshu.sample.feature.screen.result

import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.sample.feature.screen.result.nav.Result
import com.freeletics.khonshu.sample.feature.screen.result.nav.ScreenWithResultRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(ScreenWithResultRoute::class)
@SingleIn(ScreenWithResultRoute::class)
@ContributesBinding(ScreenWithResultRoute::class, ActivityNavigator::class)
class ScreenWithResultNavigator @Inject constructor(
    hostNavigator: HostNavigator,
    private val route: ScreenWithResultRoute,
) : DestinationNavigator(hostNavigator) {
    fun deliverResult(data: String) {
        deliverNavigationResult(route.key, Result(data))
        navigateBack()
    }
}
