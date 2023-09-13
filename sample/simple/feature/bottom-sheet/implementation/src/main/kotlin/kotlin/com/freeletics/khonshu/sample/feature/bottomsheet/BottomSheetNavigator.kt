package com.freeletics.khonshu.sample.feature.bottomsheet

import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(BottomSheetRoute::class)
@SingleIn(BottomSheetRoute::class)
@ContributesBinding(BottomSheetRoute::class, NavEventNavigator::class)
class BottomSheetNavigator @Inject constructor() : NavEventNavigator()
