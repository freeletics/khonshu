package com.freeletics.khonshu.codegen.fragment

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.internal.Fragment
import com.freeletics.khonshu.statemachine.StateMachine
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

/**
 * Add this annotation to a [ViewRenderer] class. It is required that the `State` and `Action
 * parameters of that renderer match the ones of the given `StateMachine`.
 *
 * This will trigger the generation of
 * - a Fragment that sets up the annotated `ViewRenderer` with the given [stateMachine]
 * - a Dagger subcomponent that uses [scope] as scope marker and [parentScope] as `parentScope`
 */
@Target(CLASS)
@Retention(RUNTIME)
public annotation class RendererFragment(
    val scope: KClass<*>,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val fragmentBaseClass: KClass<out Fragment> = Fragment::class,
)
