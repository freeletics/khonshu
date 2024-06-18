package com.freeletics.khonshu.sample.feature.dialog

import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(DialogRoute::class)
@SingleIn(DialogRoute::class)
@ContributesBinding(DialogRoute::class, ActivityNavigator::class)
class DialogNavigator @Inject constructor(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator)
