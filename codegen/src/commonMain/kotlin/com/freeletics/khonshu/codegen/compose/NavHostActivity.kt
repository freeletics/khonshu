package com.freeletics.khonshu.codegen.compose

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.statemachine.StateMachine
import kotlin.reflect.KClass

/**
 * Add this annotation to a [androidx.compose.runtime.Composable] function. It is required that
 * the annotated function has `State` as first parameter and `(Action) -> Unit` as second parameter,
 * where `State` and `Action` match the given `StateMachine`. It should also have a parameter of
 * type `@Composable (NavRoot, ((BaseRoute) -> Unit)?) -> Unit`, which is a `NavHost` composable
 * that should be placed within the annotated composable.
 *
 * This will trigger the generation of
 * - an `Activity` that extends [activityBaseClass] and displays the annotated composable
 * - a wrapper Composable that sets up the annotated Composable with the given [stateMachine]
 * - a Dagger subcomponent that uses [scope] as scope marker and [parentScope] as `parentScope`
 * - a `NavDestination` for [route] based on the given [destinationType] that is contributed
 *   to the Dagger component that uses [destinationScope]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NavHostActivity(
    val scope: KClass<*> = ActivityScope::class,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val activityBaseClass: KClass<*>,
)
