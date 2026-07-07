package com.freeletics.sample.newroot

import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.sample.bottomsheet.nav.BottomSheetRoute
import com.freeletics.sample.dialog.nav.DialogRoute
import com.freeletics.sample.newroot.nav.NewRootRoute
import com.freeletics.sample.root.nav.RootRoute
import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@ForScope(NewRootRoute::class)
@SingleIn(NewRootRoute::class)
@ContributesBinding(NewRootRoute::class, binding<DestinationNavigator>())
class NewRootNavigator(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator) {
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
