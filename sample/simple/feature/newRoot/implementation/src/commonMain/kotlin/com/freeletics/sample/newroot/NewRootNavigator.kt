package com.freeletics.sample.newroot

import com.freeletics.khonshu.navigation.DestinationNavigator2
import com.freeletics.sample.bottomsheet.nav.BottomSheetRoute
import com.freeletics.sample.dialog.nav.DialogRoute
import com.freeletics.sample.newroot.nav.NewRootRoute
import com.freeletics.sample.root.nav.RootRoute
import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@ForScope(NewRootRoute::class)
@SingleIn(NewRootRoute::class)
class NewRootNavigator(
    destinationNavigator: DestinationNavigator2,
) : DestinationNavigator2 by destinationNavigator {
    fun navigateToScreen() {
        navigateTo(ScreenRoute(100))
    }

    fun navigateToDialog() {
        navigateTo(DialogRoute)
    }

    fun navigateToBottomSheet() {
        navigateTo(BottomSheetRoute)
    }

    fun navigateToRoot() {
        showRoot(RootRoute)
    }
}
