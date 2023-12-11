/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freeletics.khonshu.navigation.androidx.internal

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi

/**
 * ActivityNavigator implements cross-activity navigation.
 */
@Navigator.Name("activity")
internal class CustomActivityNavigator(
    private val context: Context,
) : Navigator<CustomActivityNavigator.Destination>() {
    private val hostActivity: Activity? = generateSequence(context) {
        if (it is ContextWrapper) {
            it.baseContext
        } else {
            null
        }
    }.firstOrNull {
        it is Activity
    } as Activity?

    override fun createDestination(): Destination {
        return Destination(this)
    }

    override fun popBackStack(): Boolean {
        if (hostActivity != null) {
            hostActivity.finish()
            return true
        }
        return false
    }

    /**
     * Navigate to a destination.
     *
     * <p>Requests navigation to a given destination associated with this navigator in
     * the navigation graph. This method generally should not be called directly;
     * NavController will delegate to it when appropriate.</p>
     *
     * @param destination destination node to navigate to
     * @param args arguments to use for navigation
     * @param navOptions additional options for navigation
     * @param navigatorExtras extras unique to your Navigator.
     * @return The NavDestination that should be added to the back stack or null if
     * no change was made to the back stack (i.e., in cases of single top operations
     * where the destination is already on top of the back stack).
     *
     * @throws IllegalArgumentException if the given destination has no Intent
     */
    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?,
    ): NavDestination? {
        checkNotNull(destination.intent) {
            ("Destination ${destination.id} does not have an Intent set.")
        }
        val intent = Intent(destination.intent)
        if (args != null) {
            @Suppress("DEPRECATION")
            val fillInIntent = args.getParcelable<Intent>(EXTRA_FILL_IN_INTENT)
            if (fillInIntent != null) {
                intent.fillIn(fillInIntent, 0)
            }
        }
        if (hostActivity == null) {
            // If we're not launching from an Activity context we have to launch in a new task.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (navOptions != null && navOptions.shouldLaunchSingleTop()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        if (hostActivity != null) {
            val hostIntent = hostActivity.intent
            if (hostIntent != null) {
                val hostCurrentId = hostIntent.getIntExtra(EXTRA_NAV_CURRENT, 0)
                if (hostCurrentId != 0) {
                    intent.putExtra(EXTRA_NAV_SOURCE, hostCurrentId)
                }
            }
        }
        val destId = destination.id
        intent.putExtra(EXTRA_NAV_CURRENT, destId)

        context.startActivity(intent)

        // You can't pop the back stack from the caller of a new Activity,
        // so we don't add this navigator to the controller's back stack
        return null
    }

    /**
     * NavDestination for activity navigation
     *
     * Construct a new activity destination. This destination is not valid until you set the
     * Intent via [setIntent] or one or more of the other set method.
     *
     * @param activityNavigator The [ActivityNavigator] which this destination
     * will be associated with. Generally retrieved via a
     * [NavController]'s
     * [NavigatorProvider.getNavigator] method.
     */
    @NavDestination.ClassType(Activity::class)
    @InternalNavigationApi
    class Destination(
        activityNavigator: Navigator<out Destination>,
    ) : NavDestination(activityNavigator) {
        /**
         * The Intent associated with this destination.
         */
        var intent: Intent? = null

        /**
         * Construct a new activity destination. This destination is not valid until you set the
         * Intent via [setIntent] or one or more of the other set method.
         *
         * @param navigatorProvider The [NavController] which this destination
         * will be associated with.
         */
        constructor(
            navigatorProvider: NavigatorProvider,
        ) : this(navigatorProvider.getNavigator(CustomActivityNavigator::class.java))

        override fun equals(other: Any?): Boolean {
            if (other == null || other !is Destination) return false
            return super.equals(other) &&
                intent?.filterEquals(other.intent) ?: (other.intent == null)
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + (intent?.filterHashCode() ?: 0)
            return result
        }
    }

    private companion object {
        private const val EXTRA_NAV_SOURCE = "android-support-navigation:ActivityNavigator:source"
        private const val EXTRA_NAV_CURRENT = "android-support-navigation:ActivityNavigator:current"
    }
}
