package com.freeletics.mad.sample.feature.dialog

import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.sample.feature.dialog.nav.DialogRoute
import com.freeletics.mad.whetstone.ScopeTo
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(DialogRoute::class)
@ContributesBinding(DialogRoute::class, NavEventNavigator::class)
class DialogNavigator @Inject constructor() : NavEventNavigator()
