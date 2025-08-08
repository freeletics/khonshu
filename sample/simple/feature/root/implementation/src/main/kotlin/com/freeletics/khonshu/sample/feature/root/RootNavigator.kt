package com.freeletics.khonshu.sample.feature.root

import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.activity.ActivityNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.freeletics.khonshu.sample.feature.newroot.nav.NewRootRoute
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@ForScope(RootRoute::class)
@SingleIn(RootRoute::class)
@ContributesBinding(RootRoute::class, binding<ActivityNavigator>())
class RootNavigator(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator) {
    fun navigateToScreen() {
        navigateTo(ScreenRoute(1))
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
}
