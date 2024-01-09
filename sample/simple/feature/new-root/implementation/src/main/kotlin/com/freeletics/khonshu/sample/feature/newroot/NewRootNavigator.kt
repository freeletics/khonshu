package com.freeletics.khonshu.sample.feature.newroot

import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.freeletics.khonshu.sample.feature.newroot.nav.NewRootRoute
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(NewRootRoute::class)
@SingleIn(NewRootRoute::class)
@ContributesBinding(NewRootRoute::class, NavEventNavigator::class)
class NewRootNavigator @Inject constructor() : NavEventNavigator() {
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
        navigateToRoot(RootRoute)
    }
}
