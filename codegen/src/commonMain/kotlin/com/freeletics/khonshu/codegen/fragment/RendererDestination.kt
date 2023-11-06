package com.freeletics.khonshu.codegen.fragment

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.internal.Fragment
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.statemachine.StateMachine
import kotlin.reflect.KClass

/**
 * Add this annotation to a [ViewRenderer] class. It is required that the `State` and `Action
 * parameters of that renderer match the ones of the given `StateMachine`.
 *
 * This will trigger the generation of
 * - a Fragment that sets up the annotated `ViewRenderer` with the given [stateMachine]
 * - a Dagger subcomponent that uses [route] as scope marker and [parentScope] as `parentScope`
 * - a `NavDestination` for [route] based on the given [destinationType] that is contributed
 *   to the Dagger component that uses [destinationScope]
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class RendererDestination(
    val route: KClass<out BaseRoute>,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val destinationScope: KClass<*> = AppScope::class,
    val fragmentBaseClass: KClass<out Fragment> = Fragment::class,
)
