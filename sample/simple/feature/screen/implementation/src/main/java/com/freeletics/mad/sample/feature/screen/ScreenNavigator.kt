package com.freeletics.mad.sample.feature.screen

import com.freeletics.mad.codegen.ScopeTo
import com.freeletics.mad.navigation.NavEventNavigator
import com.freeletics.mad.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.mad.sample.feature.dialog.nav.DialogRoute
import com.freeletics.mad.sample.feature.screen.nav.ScreenRoute
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(ScreenRoute::class)
@ContributesBinding(ScreenRoute::class, NavEventNavigator::class)
class ScreenNavigator @Inject constructor(
    private val route: ScreenRoute,
) : NavEventNavigator() {

    fun navigateToScreen() {
        navigateTo(ScreenRoute(route.number + 1))
    }

    fun navigateToDialog() {
        navigateTo(DialogRoute)
    }

    fun navigateToBottomSheet() {
        navigateTo(BottomSheetRoute)
    }
}
