package com.freeletics.sample.screen

import android.Manifest
import com.freeletics.khonshu.navigation.activity.ActivityNavigator
import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest.PermissionResult
import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@SingleIn(ScreenRoute::class)
@ContributesBinding(ScreenRoute::class, binding<LocationPermissionNavigator>(), replaces = [NoOpLocationPermissionNavigator::class])
class ScreenActivityNavigator(
    @ForScope(ScreenRoute::class) private val activityNavigator: ActivityNavigator,
) : LocationPermissionNavigator {
    // registering can happen at any time, so doing it at construction of a navigator that is
    // injected into the state machine works as well
    private val permissionRequest = activityNavigator.registerForPermissionsResult()

    override val locationPermissionGranted: Flow<Boolean> = permissionRequest.results.map {
        it[Manifest.permission.ACCESS_COARSE_LOCATION] is PermissionResult.Granted
    }

    override fun requestLocationPermission() {
        activityNavigator.requestPermissions(permissionRequest, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}
