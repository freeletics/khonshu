package com.freeletics.khonshu.navigation.deeplinks

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.core.content.IntentCompat
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.internal.SavedStateConfiguration
import kotlinx.collections.immutable.ImmutableSet

/**
 * Creates an [Intent] that can be used to launch this deep link.
 */
public fun DeepLink.buildIntent(context: Context, destinations: ImmutableSet<NavDestination<*>>): Intent {
    val intent = if (action != null) {
        Intent(action).setPackage(context.packageName)
    } else {
        requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Couldn't obtain launch intent for ${context.packageName}"
        }
    }
    val savedStateConfiguration = SavedStateConfiguration(destinations)
    return intent
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .putExtra(EXTRA_DEEPLINK_ROUTES, encodeToSavedState(routes, savedStateConfiguration))
}

/**
 * Creates a [TaskStackBuilder] that can be used to launch this deep link.
 */
public fun DeepLink.buildTaskStack(context: Context, destinations: ImmutableSet<NavDestination<*>>): TaskStackBuilder {
    return TaskStackBuilder.create(context).addNextIntent(buildIntent(context, destinations))
}

/**
 * Creates a [PendingIntent] that can be used to launch this deep link.
 */
public fun DeepLink.buildPendingIntent(
    context: Context,
    destinations: ImmutableSet<NavDestination<*>>,
    flags: Int = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
): PendingIntent {
    val requestCode: Int = routes.fold(0) { acc, navDirection ->
        31 * acc + navDirection.hashCode()
    }

    return buildTaskStack(context, destinations).getPendingIntent(requestCode, flags)!!
}

private const val EXTRA_DEEPLINK_ROUTES: String = "com.freeletics.khonshu.navigation.DEEPLINK_ROUTES"

internal fun Intent.extractDeepLinkRoutes(destinations: ImmutableSet<NavDestination<*>>): List<BaseRoute>? {
    return IntentCompat.getParcelableExtra(this, EXTRA_DEEPLINK_ROUTES, Bundle::class.java)?.let {
        val savedStateConfiguration = SavedStateConfiguration(destinations)
        decodeFromSavedState(it, savedStateConfiguration)
    }
}
