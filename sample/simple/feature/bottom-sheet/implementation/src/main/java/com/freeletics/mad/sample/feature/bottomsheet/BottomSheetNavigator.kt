package com.freeletics.mad.sample.feature.bottomsheet

import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.mad.whetstone.ScopeTo
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, NavEventNavigator::class)
class BottomSheetNavigator @Inject constructor() : NavEventNavigator()