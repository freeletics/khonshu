package com.freeletics.khonshu.navigation.deeplinks

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.eygraber.uri.toKmpUri

/**
 * Turn this [Intent] into a [LaunchInfo] instance that can be used with [handleDeepLink].
 */
public fun Intent.asLaunchInfo(): LaunchInfo {
    return LaunchInfo(data?.toKmpUri())
}

@Composable
internal actual fun obtainLaunchInfo(): LaunchInfo {
    val activity = requireNotNull(LocalActivity.current) { "No Activity found" }
    return activity.intent.asLaunchInfo()
}
