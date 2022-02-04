package com.freeletics.mad.navigator.fragment

import android.content.Intent
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.fragment.NavDestination.Activity
import com.freeletics.mad.navigator.fragment.NavDestination.Dialog
import com.freeletics.mad.navigator.fragment.NavDestination.RootScreen
import com.freeletics.mad.navigator.fragment.NavDestination.Screen
import kotlin.reflect.KClass

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given `Fragment` class  [F] will be
 * shown when the screen is being navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute, reified F : Fragment> ScreenDestination(
    destinationId: Int,
): NavDestination = Screen(T::class, destinationId, F::class)

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given `Fragment` class  [F] will be
 * shown when the screen is being navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoot, reified F : Fragment> RootScreenDestination(
    destinationId: Int,
): NavDestination = RootScreen(T::class, destinationId, F::class)

/**
 * Creates a new [NavDestination] that represents a dialog. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given `Fragment` class  [F] will be
 * shown when the screen is being navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute, reified F : DialogFragment> DialogDestination(
    destinationId: Int,
): NavDestination = Dialog(T::class, destinationId, F::class)

/**
 * Creates a new [NavDestination] that represents an `Activity`. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given [intent] will be
 * used to launch the `Activity` when using an instance of [T] for navigation.
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> ActivityDestination(
    destinationId: Int,
    intent: Intent,
): NavDestination = Activity(T::class, destinationId, intent)

/**
 * A destination that can be navigated to. See [setGraph] for how to configure a `NavGraph` with it.
 *
 * [route] will be used as a unique identifier together with [destinationId]. The destination can
 * be reached by navigating using an instance of [route].
 */
public interface NavDestination {
    public val route: KClass<*>
    @get:IdRes public val destinationId: Int

    /**
     * Represents a full screen. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [fragmentClass] will be shown when the screen is being
     * navigated to using an instance of [route].
     */
    public class Screen(
        override val route: KClass<out NavRoute>,
        override val destinationId: Int,
        internal val fragmentClass: KClass<out Fragment>,
    ) : NavDestination

    /**
     * Represents a full screen. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [fragmentClass] will be shown when the screen is being
     * navigated to using an instance of [route].
     */
    public class RootScreen(
        override val route: KClass<out NavRoot>,
        override val destinationId: Int,
        internal val fragmentClass: KClass<out Fragment>,
    ) : NavDestination

    /**
     * Represents a dialog. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [fragmentClass] will be shown when it's being navigated to
     * using an instance of [route].
     */
    public class Dialog(
        override val route: KClass<out NavRoute>,
        override val destinationId: Int,
        internal val fragmentClass: KClass<out DialogFragment>,
    ) : NavDestination

    /**
     * Represents an `Activity`. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [intent] will be used to launch the `Activity` when using
     * an instance of [route] for navigation.
     */
    public class Activity(
        override val route: KClass<out NavRoute>,
        override val destinationId: Int,
        internal val intent: Intent,
    ) : NavDestination
}
