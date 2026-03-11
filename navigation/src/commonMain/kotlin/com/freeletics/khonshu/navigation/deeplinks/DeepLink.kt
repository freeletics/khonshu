package com.freeletics.khonshu.navigation.deeplinks

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import dev.drewhamilton.poko.Poko

/**
 * A deep link created with this will open the app with the given [routes] added to the back
 * stack on top of the start destination. The last of the given routes will be the visible
 * screen.
 */
public fun DeepLink(
    routes: List<NavRoute>,
): DeepLink = DeepLink(routes as List<BaseRoute>)

/**
 * A deep link created with this will open the app and create a back stack with [root] on top
 * of the start destination. The given [routes] will be added to that back stack. The last of
 * the given routes will be the visible screen, if none is provided `root` will be visible.
 */
public fun DeepLink(
    root: NavRoot,
    routes: List<NavRoute> = emptyList(),
): DeepLink = DeepLink(listOf<BaseRoute>(root) + routes)

/**
 * Represents a link into the app.
 */
@Poko
public class DeepLink internal constructor(
    internal val routes: List<BaseRoute>,
)
