package com.freeletics.khonshu.sample.feature.root

import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.freeletics.khonshu.sample.feature.newroot.nav.NewRootRoute
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(RootRoute::class)
@SingleIn(RootRoute::class)
@ContributesBinding(RootRoute::class, ActivityNavigator::class)
class RootNavigator @Inject constructor(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator) {
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
        replaceAll(NewRootRoute)
    }
}
