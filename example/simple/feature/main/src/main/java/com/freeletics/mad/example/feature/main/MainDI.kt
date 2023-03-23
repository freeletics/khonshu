package com.freeletics.mad.example.feature.main

import com.freeletics.mad.navigator.compose.NavDestination
import com.freeletics.mad.whetstone.AppScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@Module
@ContributesTo(MainActivity::class)
object MainRetainedModule {
    @Provides
    fun provideNavDestinations(destinations: @JvmSuppressWildcards Set<NavDestination>): DestinationsHolder {
        return DestinationsHolder(destinations)
    }
}

// Helper class to inject a Set into MainUi
data class DestinationsHolder(
    val destinations: Set<NavDestination>,
)
