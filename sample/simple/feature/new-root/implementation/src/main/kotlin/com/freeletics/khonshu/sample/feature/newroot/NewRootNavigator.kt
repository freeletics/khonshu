package com.freeletics.khonshu.sample.feature.newroot

import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.sample.feature.root.nav.NewRootRoute
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(NewRootRoute::class)
@SingleIn(NewRootRoute::class)
@ContributesBinding(NewRootRoute::class, NavEventNavigator::class)
class NewRootNavigator @Inject constructor() : NavEventNavigator()
