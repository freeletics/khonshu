package com.freeletics.mad.navigator.compose

import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.compose.NavDestination.Activity
import com.freeletics.mad.navigator.compose.NavDestination.BottomSheet
import com.freeletics.mad.navigator.compose.NavDestination.Dialog
import com.freeletics.mad.navigator.compose.NavDestination.RootScreen
import com.freeletics.mad.navigator.compose.NavDestination.Screen
import com.freeletics.mad.navigator.internal.ObsoleteNavigatorApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlin.reflect.KClass

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given [screenContent] will be shown
 * when the screen is being navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> ScreenDestination(
    destinationId: Int,
    noinline screenContent: @Composable (Bundle) -> Unit,
): NavDestination = Screen(T::class, destinationId, screenContent)

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given [screenContent] will be shown
 * when the screen is being navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoot> RootScreenDestination(
    destinationId: Int,
    noinline screenContent: @Composable (Bundle) -> Unit,
): NavDestination = RootScreen(T::class, destinationId, screenContent)

/**
 * Creates a new [NavDestination] that represents a dialog. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given [dialogContent] will be shown
 * inside the dialog window when navigating to it by using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> DialogDestination(
    destinationId: Int,
    noinline dialogContent: @Composable (Bundle) -> Unit,
): NavDestination = Dialog(T::class, destinationId, dialogContent)

/**
 * Creates a new [NavDestination] that represents a bottom sheet. The class of [T] will be used
 * as a unique identifier together with [destinationId]. The given [bottomSheetContent] will be
 * shown inside the bottom sheet when navigating to it by using an instance of [T].
 */
@Suppress("FunctionName")
@ExperimentalMaterialNavigationApi
public inline fun <reified T : NavRoute> BottomSheetDestination(
    destinationId: Int,
    noinline bottomSheetContent: @Composable (Bundle) -> Unit,
): NavDestination = BottomSheet(T::class, destinationId, bottomSheetContent)

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
 * A destination that can be navigated to. See [NavHost] for how to configure a `NavGraph` with it.
 */
public sealed interface NavDestination {
    /**
     * Represents a full screen. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [screenContent] will be shown when the screen is being
     * navigated to using an instance of [route].
     */
    public class Screen @ObsoleteNavigatorApi constructor(
        internal val route: KClass<out NavRoute>,
        internal val destinationId: Int,
        internal val defaultArguments: Bundle?,
        internal val screenContent: @Composable (Bundle) -> Unit,
    ) : NavDestination {
        public constructor(
            route: KClass<out NavRoute>,
            destinationId: Int,
            screenContent: @Composable (Bundle) -> Unit,
        ) : this(route, destinationId, null, screenContent)
    }

    /**
     * Represents a full screen. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [screenContent] will be shown when the screen is being
     * navigated to using an instance of [route].
     */
    public class RootScreen @ObsoleteNavigatorApi constructor(
        internal val route: KClass<out NavRoot>,
        internal val destinationId: Int,
        internal val defaultArguments: Bundle?,
        internal val screenContent: @Composable (Bundle) -> Unit,
    ) : NavDestination {
        public constructor(
            route: KClass<out NavRoot>,
            destinationId: Int,
            screenContent: @Composable (Bundle) -> Unit,
        ) : this(route, destinationId, null, screenContent)
    }

    /**
     * Represents a dialog. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [dialogContent] will be shown inside the dialog window
     * when navigating to it by using an instance of [route].
     */
    public class Dialog @ObsoleteNavigatorApi constructor(
        internal val route: KClass<out NavRoute>,
        internal val destinationId: Int,
        internal val defaultArguments: Bundle?,
        internal val dialogContent: @Composable (Bundle) -> Unit,
    ) : NavDestination {
        public constructor(
            route: KClass<out NavRoute>,
            destinationId: Int,
            dialogContent: @Composable (Bundle) -> Unit,
        ) : this(route, destinationId, null, dialogContent)
    }

    /**
     * Represents a bottom sheet. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [bottomSheetContent] will be shown inside the bottom sheet
     * when navigating to it by using an instance of [route].
     */
    @ExperimentalMaterialNavigationApi
    public class BottomSheet @ObsoleteNavigatorApi constructor(
        internal val route: KClass<out NavRoute>,
        internal val destinationId: Int,
        internal val defaultArguments: Bundle?,
        internal val bottomSheetContent: @Composable (Bundle) -> Unit,
    ) : NavDestination {
        public constructor(
            route: KClass<out NavRoute>,
            destinationId: Int,
            bottomSheetContent: @Composable (Bundle) -> Unit,
        ) : this(route, destinationId, null, bottomSheetContent)
    }

    /**
     * Represents an `Activity`. The [route] will be used as a unique identifier together
     * with [destinationId]. The given [intent] will be used to launch the `Activity` when using
     * an instance of [route] for navigation.
     */
    public class Activity(
        internal val route: KClass<out NavRoute>,
        internal val destinationId: Int,
        internal val intent: Intent,
    ) : NavDestination
}
