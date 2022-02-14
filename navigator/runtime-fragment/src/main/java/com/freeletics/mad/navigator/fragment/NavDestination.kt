package com.freeletics.mad.navigator.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.fragment.NavDestination.Activity
import com.freeletics.mad.navigator.fragment.NavDestination.Dialog
import com.freeletics.mad.navigator.fragment.NavDestination.Screen
import com.freeletics.mad.navigator.internal.ObsoleteNavigatorApi
import kotlin.reflect.KClass

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier. The given `Fragment` class  [F] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute, reified F : Fragment> ScreenDestination():
    NavDestination = Screen(T::class, F::class)

/**
 * Creates a new [NavDestination] that represents a dialog. The class of [T] will be used
 * as a unique identifier. The given `Fragment` class  [F] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute, reified F : DialogFragment> DialogDestination():
    NavDestination = Dialog(T::class, F::class)

/**
 * Creates a new [NavDestination] that represents an `Activity`. The class of [T] will be used
 * as a unique identifier. The given [intent] will be used to launch the `Activity` when using an
 * instance of [T] for navigation.
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> ActivityDestination(
    intent: Intent,
): NavDestination = Activity(T::class, intent)

/**
 * A destination that can be navigated to. See [setGraph] for how to configure a `NavGraph` with it.
 *
 * [route] will be used as a unique identifier. The destination can be reached by navigating using
 * an instance of [route].
 */
public sealed interface NavDestination {
    /**
     * Represents a full screen. The [route] will be used as a unique identifier. The given
     * [fragmentClass] will be shown when the screen is being
     * navigated to using an instance of [route].
     */
    public class Screen<T : BaseRoute> @ObsoleteNavigatorApi constructor(
        internal val route: KClass<T>,
        internal val fragmentClass: KClass<out Fragment>,
        internal val defaultArguments: Bundle?,
    ) : NavDestination {
        public constructor(
            route: KClass<T>,
            fragmentClass: KClass<out Fragment>,
        ) : this(route, fragmentClass, null)
    }

    /**
     * Represents a dialog. The [route] will be used as a unique identifier. The given
     * [fragmentClass] will be shown when it's being navigated to using an instance of [route].
     */
    public class Dialog<T : NavRoute> @ObsoleteNavigatorApi constructor(
        internal val route: KClass<T>,
        internal val fragmentClass: KClass<out DialogFragment>,
        internal val defaultArguments: Bundle?,
    ) : NavDestination {
        public constructor(
            route: KClass<T>,
            fragmentClass: KClass<out DialogFragment>,
        ) : this(route, fragmentClass, null)
    }

    /**
     * Represents an `Activity`. The [route] will be used as a unique identifier. The given
     * [intent] will be used to launch the `Activity` when using an instance of [route] for
     * navigation.
     */
    public class Activity<T : NavRoute>(
        internal val route: KClass<T>,
        internal val intent: Intent,
    ) : NavDestination
}
