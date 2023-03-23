package com.freeletics.mad.sample.feature.a

import com.freeletics.mad.sample.feature.a.nav.FeatureARoute
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.whetstone.ScopeTo
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ScopeTo(FeatureARoute::class)
@ContributesBinding(FeatureARoute::class, NavEventNavigator::class)
class FeatureANavigator @Inject constructor() : NavEventNavigator()
