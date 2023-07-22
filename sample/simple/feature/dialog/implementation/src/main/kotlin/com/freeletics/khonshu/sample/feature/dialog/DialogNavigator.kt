package com.freeletics.khonshu.sample.feature.dialog

import com.freeletics.khonshu.codegen.ForScope
import com.freeletics.khonshu.codegen.ScopeTo
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ForScope(DialogRoute::class)
@ScopeTo(DialogRoute::class)
@ContributesBinding(DialogRoute::class, NavEventNavigator::class)
class DialogNavigator @Inject constructor() : NavEventNavigator()
