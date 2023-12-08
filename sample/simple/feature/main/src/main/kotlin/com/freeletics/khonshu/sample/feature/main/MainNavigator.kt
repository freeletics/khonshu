package com.freeletics.khonshu.sample.feature.main

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import javax.inject.Inject

@ForScope(ActivityScope::class)
@SingleIn(ActivityScope::class)
@ContributesBinding(ActivityScope::class, NavEventNavigator::class)
class MainNavigator @Inject constructor() : NavEventNavigator()
