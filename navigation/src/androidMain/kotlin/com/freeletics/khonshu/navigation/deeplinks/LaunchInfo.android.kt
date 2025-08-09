package com.freeletics.khonshu.navigation.deeplinks

import android.content.Intent
import com.eygraber.uri.toKmpUri

/**
 * Turn this [Intent] into a [LaunchInfo] instance that can be used with [handleDeepLink].
 */
public fun Intent.asLaunchInfo(): LaunchInfo {
    return LaunchInfo(extractDeepLinkRoutes(), this.data?.toKmpUri())
}
