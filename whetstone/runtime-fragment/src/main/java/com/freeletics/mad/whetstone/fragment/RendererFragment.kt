package com.freeletics.mad.whetstone.fragment

import androidx.fragment.app.Fragment
import com.freeletics.mad.statemachine.StateMachine
import com.freeletics.mad.whetstone.AppScope
import com.gabrielittner.renderer.ViewRenderer
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
