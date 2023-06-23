package com.freeletics.khonshu.sample.feature.bottomsheet

import com.freeletics.khonshu.codegen.ScopeTo
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, NavEventNavigator::class)
class BottomSheetNavigator @Inject constructor() : NavEventNavigator()
