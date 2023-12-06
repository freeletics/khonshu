package com.freeletics.khonshu.navigation.fragment

import android.content.Intent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.internal.ActivityDestinationId
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import java.io.Serializable

/**
 * A destination that can be navigated to. See [setGraph] for how to configure a `NavGraph` with it.
 */
@Deprecated("Fragment support will be removed in the next release")
public sealed interface NavDestination

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier. The given `Fragment` class  [F] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Deprecated("Fragment support will be removed in the next release")
@Suppress("FunctionName", "DEPRECATION")
public inline fun <reified T : BaseRoute, reified F : Fragment> ScreenDestination():
    NavDestination = ScreenDestination(DestinationId(T::class), F::class.qualifiedName!!, null)

@InternalNavigationApi
@Suppress("FunctionName", "DEPRECATION")
public inline fun <reified T : BaseRoute, reified F : Fragment> ScreenDestination(extra: Serializable):
    NavDestination = ScreenDestination(DestinationId(T::class), F::class.qualifiedName!!, extra)

@PublishedApi
@Suppress("DEPRECATION")
internal class ScreenDestination<T : BaseRoute>(
    internal val id: DestinationId<T>,
    internal val fragmentClass: String,
    internal val extra: Serializable?,
) : NavDestination

/**
 * Creates a new [NavDestination] that represents a dialog. The class of [T] will be used
 * as a unique identifier. The given `Fragment` class  [F] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Deprecated("Fragment support will be removed in the next release")
@Suppress("FunctionName", "DEPRECATION")
public inline fun <reified T : NavRoute, reified F : DialogFragment> DialogDestination():
    NavDestination = DialogDestination(DestinationId(T::class), F::class.qualifiedName!!, null)

@InternalNavigationApi
@Suppress("FunctionName", "DEPRECATION")
public inline fun <reified T : NavRoute, reified F : DialogFragment> DialogDestination(extra: Serializable):
    NavDestination = DialogDestination(DestinationId(T::class), F::class.qualifiedName!!, extra)

@PublishedApi
@Suppress("DEPRECATION")
internal class DialogDestination<T : NavRoute>(
    internal val id: DestinationId<T>,
    internal val fragmentClass: String,
    internal val extra: Serializable?,
) : NavDestination

/**
 * Creates a new [NavDestination] that represents an `Activity`. The class of [T] will be used
 * as a unique identifier. The given [intent] will be used to launch the `Activity` when using an
 * instance of [T] for navigation.
 */
@Deprecated("Fragment support will be removed in the next release")
@Suppress("FunctionName", "DEPRECATION")
public inline fun <reified T : ActivityRoute> ActivityDestination(
    intent: Intent,
): NavDestination = ActivityDestination(ActivityDestinationId(T::class), intent)

@PublishedApi
@Suppress("DEPRECATION")
internal class ActivityDestination<T : ActivityRoute>(
    internal val id: ActivityDestinationId<T>,
    internal val intent: Intent,
) : NavDestination
