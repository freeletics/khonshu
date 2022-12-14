package com.freeletics.mad.navigator

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.freeletics.mad.navigator.internal.InternalNavigatorApi

public sealed interface BaseRoute : Parcelable

/**
 * Represents the route to a destination.
 *
 * The instance of this will be put into the navigation arguments as a [Parcelable] and is then
 * available to the target screens.
 */
public interface NavRoute : BaseRoute

/**
 * This is similar to a [NavRoute] but represents the route to the start destination used in
 * a backstack. When you navigate to a [NavRoot] the current backstack is saved and removed
 * so that the [NavRoot] is right on top of the start destination.
 *
 * The instance of this will be put into the navigation arguments as a [Parcelable] and is then
 * available to the target screens.
 */
public interface NavRoot : BaseRoute

/**
 * Represents the route to an `Activity`. Should be used through []
 */
public sealed interface ActivityRoute : Parcelable{
    public fun fillInIntent(): Intent
}



/**
 * Represents the route to an `Activity` within the current app. The instance of this route
 * will be added to the resulting `Intent` and can be accessed in the launched `Activity` by calling
 * [getRoute] or [requireRoute].
 */
public abstract class InternalActivityRoute : ActivityRoute {
    override fun fillInIntent(): Intent {
        return Intent().putExtra(EXTRA_ROUTE, this)
    }
}

/**
 * Represents the route to an `Activity` in another app. [fillInIntent] can be used to dynamically
 * add extras to the resulting `Intent`.
 */
public interface ExternalActivityRoute : ActivityRoute

/**
 * Returns the [ActivityRoute] that was used to navigate to this [Activity].
 */
public fun <T : ActivityRoute> Activity.requireRoute(): T {
    return requireNotNull(getRoute()) {
        "Error extracting ActivityRoute from Activity's Intent"
    }
}

/**
 * Returns the [ActivityRoute] that was used to navigate to this [Activity] if it's present in
 * Activity's Intent.
 */
public fun <T : ActivityRoute> Activity.getRoute(): T? {
    @Suppress("DEPRECATION")
    return intent.extras?.getParcelable(EXTRA_ROUTE)
}

@InternalNavigatorApi
public const val EXTRA_ROUTE: String = "com.freeletics.mad.navigation.ROUTE"
