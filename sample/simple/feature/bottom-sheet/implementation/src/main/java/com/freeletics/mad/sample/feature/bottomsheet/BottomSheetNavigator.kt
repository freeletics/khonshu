package com.freeletics.mad.sample.feature.bottomsheet

import com.freeletics.mad.codegen.ScopeTo
import com.freeletics.mad.navigation.NavEventNavigator
import com.freeletics.mad.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, NavEventNavigator::class)
class BottomSheetNavigator @Inject constructor() : NavEventNavigator()
