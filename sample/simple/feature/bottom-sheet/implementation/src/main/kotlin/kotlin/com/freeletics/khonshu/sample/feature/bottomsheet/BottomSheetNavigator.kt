package com.freeletics.khonshu.sample.feature.bottomsheet

import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(BottomSheetRoute::class)
@SingleIn(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, NavEventNavigator::class)
class BottomSheetNavigator @Inject constructor(
    private val route: BottomSheetRoute,
) : NavEventNavigator() {

    fun navigateToScreen() {
        navigateTo(ScreenRoute(route.number + 1))
    }

    fun navigateToDialog() {
        navigateTo(DialogRoute(route.number + 1))
    }

    fun navigateToBottomSheet() {
        navigateTo(BottomSheetRoute(route.number + 1))
    }
}
