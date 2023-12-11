/*
 * Copyright 2020 The Android Open Source Project
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

import androidx.compose.runtime.Composable
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.freeletics.khonshu.navigation.androidx.internal.OverlayNavigator.Destination

@Navigator.Name("overlay")
internal class OverlayNavigator : Navigator<Destination>() {

    /**
     * Get the back stack from the [state].
     */
    internal val backStack get() = state.backStack

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Extras?,
    ) {
        entries.forEach { entry ->
            state.push(entry)
        }
    }

    override fun createDestination(): Destination {
        return Destination(this) { }
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
    }

    internal fun onTransitionComplete(entry: NavBackStackEntry) {
        state.markTransitionComplete(entry)
    }

    /**
     * NavDestination specific to [OverlayNavigator]
     */
    @NavDestination.ClassType(Composable::class)
    class Destination(
        navigator: OverlayNavigator,
        internal val content: @Composable (NavBackStackEntry) -> Unit,
    ) : NavDestination(navigator), FloatingWindow
}
