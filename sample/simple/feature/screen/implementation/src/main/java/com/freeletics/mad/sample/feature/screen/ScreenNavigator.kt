package com.freeletics.mad.sample.feature.screen

import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.mad.sample.feature.screen.nav.ScreenRoute
import com.freeletics.mad.whetstone.ScopeTo
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(ScreenRoute::class)
@ContributesBinding(ScreenRoute::class, NavEventNavigator::class)
class ScreenNavigator @Inject constructor() : NavEventNavigator() {
    fun navigateToBottomSheet() {
        navigateTo(BottomSheetRoute)
    }
}