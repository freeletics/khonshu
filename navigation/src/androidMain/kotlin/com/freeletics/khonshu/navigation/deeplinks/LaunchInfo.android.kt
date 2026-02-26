package com.freeletics.khonshu.navigation.deeplinks

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.eygraber.uri.toKmpUri
import com.freeletics.khonshu.navigation.NavDestination

/**
 * Turn this [Intent] into a [LaunchInfo] instance that can be used with [handleDeepLink].
 */
public fun Intent.asLaunchInfo(destinations: Set<NavDestination<*>>): LaunchInfo {
    return LaunchInfo(extractDeepLinkRoutes(destinations), this.data?.toKmpUri())
}

@Composable
internal actual fun obtainLaunchInfo(destinations: Set<NavDestination<*>>): LaunchInfo {
    val activity = requireNotNull(LocalActivity.current) { "No Activity found" }
    return activity.intent.asLaunchInfo(destinations)
}
