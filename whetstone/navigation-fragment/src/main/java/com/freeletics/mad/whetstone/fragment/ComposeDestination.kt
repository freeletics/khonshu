package com.freeletics.mad.whetstone.fragment

import androidx.fragment.app.Fragment
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.statemachine.StateMachine
import com.freeletics.mad.whetstone.AppScope
import kotlin.reflect.KClass

/**
 * Add this annotation to a [androidx.compose.runtime.Composable] function. It is required that
 * the annotated function has `State` as first parameter and `(Action) -> Unit` as second parameter,
 * where `State` and `Action` match the given `StateMachine`.
 *
 * This will trigger the generation of
 * - a Fragment that sets up the annotated Composable with the given [stateMachine]
 * - a Dagger subcomponent that uses [route] as scope marker and [parentScope] as `parentScope`
 * - a `NavDestination` for [route] based on the given [destinationType] that is contributed
 *   to the Dagger component that uses [destinationScope]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ComposeDestination(
    val route: KClass<out BaseRoute>,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val destinationType: DestinationType = DestinationType.SCREEN,
    val destinationScope: KClass<*> = AppScope::class,
    val fragmentBaseClass: KClass<out Fragment> = Fragment::class,
)
