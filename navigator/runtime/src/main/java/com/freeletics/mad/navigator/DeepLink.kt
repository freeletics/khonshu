package com.freeletics.mad.navigator

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.core.app.TaskStackBuilder
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import dev.drewhamilton.poko.Poko

/**
 * Represents a link into the app.
 */
@Poko
public class DeepLink internal constructor(
    private val action: String?,
    private val routes: List<Parcelable>,
) {

    /**
     * A deep link created with this will open the app with the given [routes] added to the back
     * stack on top of the start destination. The last of the given routes will be the visible
     * screen.
     *
     * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
     * will use it as it's [Intent.getAction]. If no `action` is provided the app's
     * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
     */
    public constructor(
        routes: List<NavRoute>,
        action: String? = null
    ) : this(action, routes)

    /**
     * A deep link created with this will open the app and create a back stack with [root] on top
     * of the start destination. The given [routes] will be added to that back stack. The last of
     * the given routes will be the visible screen, if none is provided `root` will be visible.
     *
     * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
     * will use it as it's [Intent.getAction]. If no `action` is provided the app's
     * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
     */
    public constructor(
        root: NavRoot,
        routes: List<NavRoute> = emptyList(),
        action: String? = null,
    ) : this(action, listOf<BaseRoute>(root) + routes)

    /**
     * A deep link created with this will open the app with it's start destination and then launch
     * [ActivityRoute] on top of it.
     *
     * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
     * will use it as it's [Intent.getAction]. If no `action` is provided the app's
     * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
     */
    public constructor(
        activityRoute: ActivityRoute,
        action: String? = null
    ) : this(action, listOf(activityRoute))

    /**
     * A deep link created with this will open the app with the given [routes] added to the back
     * stack on top of the start destination, [activityRoute] will then be launched on top of this.
     *
     * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
     * will use it as it's [Intent.getAction]. If no `action` is provided the app's
     * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
     */
    public constructor(
        routes: List<NavRoute>,
        activityRoute: ActivityRoute,
        action: String? = null
    ) : this(action, routes + activityRoute)

    /**
     * A deep link created with this will open the app and create a back stack with [root] on top
     * of the start destination. The given [routes] will be added to that back stack and
     * [activityRoute] will then be launched on top of this.
     *
     * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
     * will use it as it's [Intent.getAction]. If no `action` is provided the app's
     * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
     */
    public constructor(
        root: NavRoot,
        routes: List<NavRoute>,
        activityRoute: ActivityRoute,
        action: String? = null,
    ) : this(action, listOf<BaseRoute>(root) + routes + activityRoute)

    /**
     * Creates an [Intent] that can be used to launch this deep link.
     */
    public fun buildIntent(context: Context): Intent {
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
    public fun buildTaskStack(context: Context): TaskStackBuilder {
        return TaskStackBuilder.create(context).addNextIntent(buildIntent(context))
    }

    /**
     * Creates a [PendingIntent] that can be used to launch this deep link.
     */
    public fun buildPendingIntent(
        context: Context,
        flags: Int = defaultFlag(),
    ): PendingIntent {
        val requestCode: Int = routes.fold(0) { acc, navDirection ->
            31 * acc + navDirection.hashCode()
        }

        return buildTaskStack(context).getPendingIntent(requestCode, flags)!!
    }

    @InternalNavigatorApi
    public companion object {
        @property:InternalNavigatorApi
        public const val EXTRA_DEEPLINK_ROUTES: String = "com.freeletics.mad.navigation.DEEPLINK_ROUTES"

        private fun defaultFlag(): Int {
            return if (Build.VERSION.SDK_INT >= 23) {
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            } else {
                FLAG_UPDATE_CURRENT
            }
        }
    }
}
