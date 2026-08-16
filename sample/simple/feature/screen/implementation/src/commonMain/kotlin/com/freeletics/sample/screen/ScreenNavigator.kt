package com.freeletics.sample.screen

import com.freeletics.khonshu.navigation.DestinationNavigator2
import com.freeletics.khonshu.navigation.registerForNavigationResult
import com.freeletics.sample.bottomsheet.nav.BottomSheetRoute
import com.freeletics.sample.dialog.nav.DialogRoute
import com.freeletics.sample.newroot.nav.NewRootRoute
import com.freeletics.sample.screen.nav.ScreenRoute
import com.freeletics.sample.screenwithresult.nav.Result
import com.freeletics.sample.screenwithresult.nav.ScreenWithResultRoute
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ScreenRoute::class)
class ScreenNavigator(
    private val route: ScreenRoute,
    private val destinationNavigator: DestinationNavigator2,
) : DestinationNavigator2 by destinationNavigator {
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
        replaceAllBackStacks(NewRootRoute)
    }

    fun navigateToScreenForResult() {
        navigateTo(ScreenWithResultRoute(destinationResult.key))
    }
}
