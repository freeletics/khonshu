package com.freeletics.sample.screen

import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.registerForNavigationResult
import com.freeletics.sample.bottomsheet.nav.BottomSheetRoute
import com.freeletics.sample.dialog.nav.DialogRoute
import com.freeletics.sample.newroot.nav.NewRootRoute
import com.freeletics.sample.screen.nav.ScreenRoute
import com.freeletics.sample.screenwithresult.nav.Result
import com.freeletics.sample.screenwithresult.nav.ScreenWithResultRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@ForScope(ScreenRoute::class)
@SingleIn(ScreenRoute::class)
@ContributesBinding(ScreenRoute::class, binding<DestinationNavigator>())
class ScreenNavigator(
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
        replaceAllBackStacks(NewRootRoute)
    }

    fun navigateToScreenForResult() {
        navigateTo(ScreenWithResultRoute(destinationResult.key))
    }
}
