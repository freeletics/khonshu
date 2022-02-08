package com.freeletics.mad.whetstone

import android.content.Context
import androidx.navigation.NavBackStackEntry
import com.freeletics.mad.whetstone.internal.InternalWhetstoneApi
import com.freeletics.mad.whetstone.internal.NavEntryComponentGetter
import javax.inject.Inject
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * This annotation can be used in combination with `ComposeScreen`, `ComposeFragment` and
 * `RendererFragment` to enable the integration of a `NavEventNavigator` into the generated code.
 * The navigator will automatically be set up so that it's ready to handle events.
 *
 * The `NavEventNavigator` subclass is expected to be bound to `NavEventNavigator` for
 * `ComposeScreen` and to `FragmentNavEventNavigator for `ComposeFragment` and
 * `RendererFragment`.
 */
@Target(FUNCTION)
@Retention(RUNTIME)
public annotation class NavDestination
