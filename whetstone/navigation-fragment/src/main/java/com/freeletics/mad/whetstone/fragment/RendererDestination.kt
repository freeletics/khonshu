package com.freeletics.mad.whetstone.fragment

import androidx.fragment.app.Fragment
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.statemachine.StateMachine
import com.gabrielittner.renderer.ViewRenderer
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
    val parentScope: KClass<*>,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val destinationType: DestinationType = DestinationType.SCREEN,
    val destinationScope: KClass<*>,
    val fragmentBaseClass: KClass<out Fragment> = Fragment::class,
    val rendererFactory: KClass<out ViewRenderer.Factory<*, *>>,
)
