package com.freeletics.sample.screen

import android.Manifest
import com.freeletics.khonshu.navigation.activity.ActivityNavigator
import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest
import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Inject
@SingleIn(ScreenRoute::class)
@ContributesBinding(ScreenRoute::class, binding<LocationPermissionNavigator>(), replaces = [NoOpLocationPermissionNavigator::class])
class ScreenActivityNavigator(
    @ForScope(ScreenRoute::class) private val activityNavigator: ActivityNavigator,
) : LocationPermissionNavigator {
    private val granted = MutableStateFlow<Boolean?>(null)

    override val locationPermissionGranted: Flow<Boolean> = granted.filterNotNull()

    private var request: PermissionsResultRequest? = null

    override fun requestLocationPermission() {
        val r = request ?: activityNavigator.registerForPermissionsResult().also { request = it }
        activityNavigator.requestPermissions(r, Manifest.permission.ACCESS_COARSE_LOCATION)
        CoroutineScope(Dispatchers.Main.immediate).launch {
            r.results.collect {
                granted.value =
                    it[Manifest.permission.ACCESS_COARSE_LOCATION] is PermissionsResultRequest.PermissionResult.Granted
            }
        }
    }
}
