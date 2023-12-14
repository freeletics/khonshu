package com.freeletics.khonshu.codegen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.statemachine.StateMachine
import kotlin.reflect.KClass

/**
 * Add this annotation to a [androidx.compose.runtime.Composable] function. It is required that
 * the annotated function has `State` as first parameter and `(Action) -> Unit` as second parameter,
 * where `State` and `Action` match the given `StateMachine`. It should also have a parameter of
 * type `SimpleNavHost`, which is a `NavHost` composable that should be placed within the annotated composable.
 *
 * This will trigger the generation of
 * - an `Activity` that extends [activityBaseClass] and displays the annotated composable
 * - a wrapper Composable that sets up the annotated Composable with the given [stateMachine]
 * - a Dagger subcomponent that uses [scope] as scope marker and [parentScope] as `parentScope`
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NavHostActivity(
    val scope: KClass<*> = ActivityScope::class,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val activityBaseClass: KClass<*>,
    val experimentalNavigation: Boolean = false,
)

/**
 * A simplified wrapper around `NavHost`
 *
 * Parameters:
 * - [NavRoot]: the start destination of the `NavHost`
 * - [Modifier]: passed to `NavHost`
 * - an optional destination changed callback
 */
public typealias SimpleNavHost = @Composable (NavRoot, Modifier, ((BaseRoute) -> Unit)?) -> Unit
