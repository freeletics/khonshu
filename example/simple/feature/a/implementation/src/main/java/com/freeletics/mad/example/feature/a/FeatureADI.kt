package com.freeletics.mad.example.feature.a

import com.freeletics.mad.example.feature.a.nav.FeatureARoute
import com.freeletics.mad.whetstone.AppScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module

@Module
@ContributesTo(FeatureARoute::class)
object FeatureARetainedModule

@Module
@ContributesTo(AppScope::class)
object FeatureASingletonModule
