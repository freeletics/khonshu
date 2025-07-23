package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.statemachine.StateMachine
import dev.zacsweers.metro.AppScope
import kotlin.reflect.KClass

/**
 * Add this annotation to a [androidx.compose.runtime.Composable] function. It is required that
 * the annotated function has `State` as first parameter and `(Action) -> Unit` as second parameter,
 * where `State` and `Action` match the given `StateMachine`.
 *
 * This will trigger the generation of
 * - a wrapper Composable that sets up the annotated Composable with the given [stateMachine]
 * - a Metro graph extension that uses [route] as scope marker and [parentScope] as `scope` for its factory
 * - a `NavDestination` for [route] that is contributed to the [destinationScope]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NavDestination(
    val route: KClass<out BaseRoute>,
    val parentScope: KClass<*> = ActivityScope::class,
    val stateMachine: KClass<*>,
    val destinationScope: KClass<*> = AppScope::class,
)
