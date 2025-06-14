package com.freeletics.khonshu.navigation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState

/**
 * Represents the route to an `Activity`. Should be used through [InternalActivityRoute]
 * and [ExternalActivityRoute].
 */
public sealed interface ActivityRoute {
    public fun buildIntent(context: Context): Intent
}

/**
 * Represents the route to an `Activity` within the current app. The instance of this route
 * will be added to the resulting `Intent` and can be accessed in the launched `Activity` by calling
 * [getRoute] or [requireRoute]. The navigator will also ensure that the [Intent] will only target the
 * current app.
 */
public abstract class InternalActivityRoute : ActivityRoute

/**
 * Represents the route to an `Activity` in another app. [buildIntent] can be used to dynamically
 * add extras to the resulting `Intent`.
 */
public interface ExternalActivityRoute : ActivityRoute

/**
 * Returns the [ActivityRoute] that was used to navigate to this [Activity].
 */
public inline fun <reified T : InternalActivityRoute> Activity.requireRoute(): T {
    return requireNotNull(getRoute()) {
        "Error extracting ActivityRoute from Activity's Intent"
    }
}

/**
 * Returns the [ActivityRoute] that was used to navigate to this [Activity] if it's present in
 * Activity's Intent.
 */
public inline fun <reified T : InternalActivityRoute> Activity.getRoute(): T? {
    val savedState = IntentCompat.getParcelableExtra(intent, EXTRA_ROUTE, SavedState::class.java) ?: return null
    return decodeFromSavedState(savedState)
}

internal fun Intent.putRoute(route: ActivityRoute): Intent {
    return putExtra(EXTRA_ROUTE, encodeToSavedState(route))
}

@PublishedApi
internal const val EXTRA_ROUTE: String = "com.freeletics.khonshu.navigation.ROUTE"
