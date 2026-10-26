package com.freeletics.sample.screen

import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface LocationPermissionNavigator {
    val locationPermissionGranted: Flow<Boolean>

    fun requestLocationPermission()
}

@Inject
@ContributesBinding(ScreenRoute::class)
class NoOpLocationPermissionNavigator : LocationPermissionNavigator {
    override val locationPermissionGranted: Flow<Boolean> = emptyFlow()

    override fun requestLocationPermission() {}
}
