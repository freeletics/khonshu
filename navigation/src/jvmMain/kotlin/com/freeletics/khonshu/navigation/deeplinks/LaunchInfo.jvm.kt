package com.freeletics.khonshu.navigation.deeplinks

import androidx.compose.runtime.Composable
import com.freeletics.khonshu.navigation.NavDestination

@Composable
internal actual fun obtainLaunchInfo(destinations: Set<NavDestination<*>>): LaunchInfo {
    return LaunchInfo(null, null)
}
