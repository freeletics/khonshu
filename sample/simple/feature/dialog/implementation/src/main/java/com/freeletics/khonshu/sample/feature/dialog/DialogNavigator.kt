package com.freeletics.khonshu.sample.feature.dialog

import com.freeletics.khonshu.codegen.ScopeTo
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(DialogRoute::class)
@ContributesBinding(DialogRoute::class, NavEventNavigator::class)
class DialogNavigator @Inject constructor(
    private val route: DialogRoute,
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
