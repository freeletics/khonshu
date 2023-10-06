package com.freeletics.khonshu.navigation.deeplinks

import android.content.Intent
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute

/**
 * A deep link created with this will open the app with it's start destination and then launch
 * [ActivityRoute] on top of it.
 *
 * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
 * will use it as it's [Intent.getAction]. If no `action` is provided the app's
 * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
 */
public fun DeepLink(
    activityRoute: ActivityRoute,
    action: String? = null,
): DeepLink = DeepLink(action, listOf(activityRoute))

/**
 * A deep link created with this will open the app with the given [routes] added to the back
 * stack on top of the start destination, [activityRoute] will then be launched on top of this.
 *
 * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
 * will use it as it's [Intent.getAction]. If no `action` is provided the app's
 * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
 */
public fun DeepLink(
    routes: List<NavRoute>,
    activityRoute: ActivityRoute,
    action: String? = null,
): DeepLink = DeepLink(action, routes + activityRoute)

/**
 * A deep link created with this will open the app and create a back stack with [root] on top
 * of the start destination. The given [routes] will be added to that back stack and
 * [activityRoute] will then be launched on top of this.
 *
 * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
 * will use it as it's [Intent.getAction]. If no `action` is provided the app's
 * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
 */
public fun DeepLink(
    root: NavRoot,
    routes: List<NavRoute>,
    activityRoute: ActivityRoute,
    action: String? = null,
): DeepLink = DeepLink(action, listOf<BaseRoute>(root) + routes + activityRoute)
