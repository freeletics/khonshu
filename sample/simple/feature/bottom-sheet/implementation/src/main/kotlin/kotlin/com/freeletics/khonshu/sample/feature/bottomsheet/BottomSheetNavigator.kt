package com.freeletics.khonshu.sample.feature.bottomsheet

import com.freeletics.khonshu.codegen.ForScope
import com.freeletics.khonshu.codegen.ScopeTo
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ForScope(BottomSheetRoute::class)
@ScopeTo(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, NavEventNavigator::class)
class BottomSheetNavigator @Inject constructor() : NavEventNavigator()