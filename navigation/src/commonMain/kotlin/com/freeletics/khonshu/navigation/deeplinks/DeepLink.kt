package com.freeletics.khonshu.navigation.deeplinks

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.internal.Parcelable
import dev.drewhamilton.poko.Poko

/**
 * A deep link created with this will open the app with the given [routes] added to the back
 * stack on top of the start destination. The last of the given routes will be the visible
 * screen.
 *
 * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
 * will use it as it's [Intent.getAction]. If no `action` is provided the app's
 * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
 */
public fun DeepLink(
    routes: List<NavRoute>,
    action: String? = null,
): DeepLink = DeepLink(action, routes)

/**
 * A deep link created with this will open the app and create a back stack with [root] on top
 * of the start destination. The given [routes] will be added to that back stack. The last of
 * the given routes will be the visible screen, if none is provided `root` will be visible.
 *
 * If an [action] is provided the [Intent] returned by [buildIntent] and the other builders
 * will use it as it's [Intent.getAction]. If no `action` is provided the app's
 * [android.content.pm.PackageManager.getLaunchIntentForPackage] will be used.
 */
public fun DeepLink(
    root: NavRoot,
    routes: List<NavRoute> = emptyList(),
    action: String? = null,
): DeepLink = DeepLink(action, listOf<BaseRoute>(root) + routes)

/**
 * Represents a link into the app.
 */
@Poko
public class DeepLink internal constructor(
    internal val action: String?,
    internal val routes: List<Parcelable>,
)
