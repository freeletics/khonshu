package com.freeletics.mad.navigator.compose

import android.content.Intent
import androidx.compose.runtime.Composable
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.compose.NavDestination.Activity
import com.freeletics.mad.navigator.compose.NavDestination.BottomSheet
import com.freeletics.mad.navigator.compose.NavDestination.Dialog
import com.freeletics.mad.navigator.compose.NavDestination.Screen
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlin.reflect.KClass

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier. The given [screenContent] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute> ScreenDestination(
    noinline screenContent: @Composable (T) -> Unit,
): NavDestination = Screen(T::class, screenContent)

/**
 * Creates a new [NavDestination] that represents a dialog. The class of [T] will be used
 * as a unique identifier. The given [dialogContent] will be shown inside the dialog window when
 * navigating to it by using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> DialogDestination(
    noinline dialogContent: @Composable (T) -> Unit,
): NavDestination = Dialog(T::class, dialogContent)

/**
 * Creates a new [NavDestination] that represents a bottom sheet. The class of [T] will be used
 * as a unique identifier. The given [bottomSheetContent] will be shown inside the bottom sheet
 * when navigating to it by using an instance of [T].
 */
@Suppress("FunctionName")
@ExperimentalMaterialNavigationApi
public inline fun <reified T : NavRoute> BottomSheetDestination(
    noinline bottomSheetContent: @Composable (T) -> Unit,
): NavDestination = BottomSheet(T::class, bottomSheetContent)

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
 * A destination that can be navigated to. See [NavHost] for how to configure a `NavGraph` with it.
 */
public sealed interface NavDestination {
    /**
     * Represents a full screen. The [route] will be used as a unique identifier.
     * The given [screenContent] will be shown when the screen is being
     * navigated to using an instance of [route].
     */
    public class Screen<T : BaseRoute>(
        internal val route: KClass<T>,
        internal val screenContent: @Composable (T) -> Unit,
    ) : NavDestination

    /**
     * Represents a dialog. The [route] will be used as a unique identifier together.
     * The given [dialogContent] will be shown inside the dialog window
     * when navigating to it by using an instance of [route].
     */
    public class Dialog<T : NavRoute>(
        internal val route: KClass<T>,
        internal val dialogContent: @Composable (T) -> Unit,
    ) : NavDestination

    /**
     * Represents a bottom sheet. The [route] will be used as a unique identifier.
     * The given [bottomSheetContent] will be shown inside the bottom sheet
     * when navigating to it by using an instance of [route].
     */
    @ExperimentalMaterialNavigationApi
    public class BottomSheet<T : NavRoute>(
        internal val route: KClass<T>,
        internal val bottomSheetContent: @Composable (T) -> Unit,
    ) : NavDestination

    /**
     * Represents an `Activity`. The [route] will be used as a unique identifier.
     * The given [intent] will be used to launch the `Activity` when using
     * an instance of [route] for navigation.
     */
    public class Activity(
        internal val route: KClass<out NavRoute>,
        internal val intent: Intent,
    ) : NavDestination
}
