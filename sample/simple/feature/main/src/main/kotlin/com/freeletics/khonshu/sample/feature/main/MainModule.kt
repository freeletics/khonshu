package com.freeletics.khonshu.sample.feature.main

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.UseExperimentalNavigation
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@Module
@ContributesTo(ActivityScope::class)
object MainModule {
    @Provides
    @UseExperimentalNavigation
    fun provideUseExperimentalNavigation(): Boolean = true
}
