package com.freeletics.mad.navigator.fragment

import android.content.Intent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.internal.ActivityDestinationId
import com.freeletics.mad.navigator.internal.DestinationId

/**
 * A destination that can be navigated to. See [setGraph] for how to configure a `NavGraph` with it.
 */
public sealed interface NavDestination

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier. The given `Fragment` class  [F] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute, reified F : Fragment> ScreenDestination():
    NavDestination = ScreenDestination(DestinationId(T::class), F::class.qualifiedName!!)

@PublishedApi
internal class ScreenDestination<T : BaseRoute>(
    internal val id: DestinationId<T>,
    internal val fragmentClass: String,
) : NavDestination

/**
 * Creates a new [NavDestination] that represents a dialog. The class of [T] will be used
 * as a unique identifier. The given `Fragment` class  [F] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute, reified F : DialogFragment> DialogDestination():
    NavDestination = DialogDestination(DestinationId(T::class), F::class.qualifiedName!!)

@PublishedApi
internal class DialogDestination<T : NavRoute>(
    internal val id: DestinationId<T>,
    internal val fragmentClass: String,
) : NavDestination

/**
 * Creates a new [NavDestination] that represents an `Activity`. The class of [T] will be used
 * as a unique identifier. The given [intent] will be used to launch the `Activity` when using an
 * instance of [T] for navigation.
 */
@Suppress("FunctionName")
public inline fun <reified T : ActivityRoute> ActivityDestination(
    intent: Intent,
): NavDestination = ActivityDestination(ActivityDestinationId(T::class), intent)

@PublishedApi
internal class ActivityDestination<T : ActivityRoute>(
    internal val id: ActivityDestinationId<T>,
    internal val intent: Intent,
) : NavDestination
