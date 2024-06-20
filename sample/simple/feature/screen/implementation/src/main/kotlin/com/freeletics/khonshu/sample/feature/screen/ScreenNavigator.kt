package com.freeletics.khonshu.sample.feature.screen

import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.ResultNavigator.Companion.registerForNavigationResult
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.freeletics.khonshu.sample.feature.newroot.nav.NewRootRoute
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.freeletics.khonshu.sample.feature.screen.result.nav.Result
import com.freeletics.khonshu.sample.feature.screen.result.nav.ScreenWithResultRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(ScreenRoute::class)
@SingleIn(ScreenRoute::class)
@ContributesBinding(ScreenRoute::class, ActivityNavigator::class)
class ScreenNavigator @Inject constructor(
    hostNavigator: HostNavigator,
    private val route: ScreenRoute,
) : DestinationNavigator(hostNavigator) {

    val destinationResult = registerForNavigationResult<ScreenRoute, Result>()

    fun navigateToScreen() {
        navigateTo(ScreenRoute(route.number + 1))
    }

    fun navigateToDialog() {
        navigateTo(DialogRoute)
    }

    fun navigateToBottomSheet() {
        navigateTo(BottomSheetRoute)
    }

    fun replaceAllWithNewRoot() {
        replaceAll(NewRootRoute)
    }

    fun navigateToScreenForResult() {
        navigateTo(ScreenWithResultRoute(destinationResult.key))
    }
}
