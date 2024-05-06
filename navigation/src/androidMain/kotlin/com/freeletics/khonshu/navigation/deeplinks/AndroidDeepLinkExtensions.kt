package com.freeletics.khonshu.navigation.deeplinks

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.TaskStackBuilder
import com.eygraber.uri.toUri
import com.freeletics.khonshu.navigation.internal.Parcelable

/**
 * Creates an [Intent] that can be used to launch this deep link.
 */
public fun DeepLink.buildIntent(context: Context): Intent {
    val intent = if (action != null) {
        Intent(action).setPackage(context.packageName)
    } else {
        requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Couldn't obtain launch intent for ${context.packageName}"
        }
    }
    return intent
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .putParcelableArrayListExtra(EXTRA_DEEPLINK_ROUTES, ArrayList(routes))
}

/**
 * Creates a [TaskStackBuilder] that can be used to launch this deep link.
 */
public fun DeepLink.buildTaskStack(context: Context): TaskStackBuilder {
    return TaskStackBuilder.create(context).addNextIntent(buildIntent(context))
}

/**
 * Creates a [PendingIntent] that can be used to launch this deep link.
 */
public fun DeepLink.buildPendingIntent(
    context: Context,
    flags: Int = defaultFlag(),
): PendingIntent {
    val requestCode: Int = routes.fold(0) { acc, navDirection ->
        31 * acc + navDirection.hashCode()
    }

    return buildTaskStack(context).getPendingIntent(requestCode, flags)!!
}

private const val EXTRA_DEEPLINK_ROUTES: String = "com.freeletics.khonshu.navigation.DEEPLINK_ROUTES"

private fun defaultFlag(): Int {
    return if (Build.VERSION.SDK_INT >= 23) {
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    } else {
        FLAG_UPDATE_CURRENT
    }
}

internal fun Intent.extractDeepLinkRoutes(
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
): List<Parcelable> {
    if (hasExtra(EXTRA_DEEPLINK_ROUTES)) {
        @Suppress("DEPRECATION")
        return getParcelableArrayListExtra(EXTRA_DEEPLINK_ROUTES)!!
    }
    val uri = data
    if (uri != null) {
        val deepLink = deepLinkHandlers.createDeepLinkIfMatching(uri.toUri(), deepLinkPrefixes)
        return deepLink?.routes ?: emptyList()
    }
    return emptyList()
}
