package com.freeletics.khonshu.codegen.fragment

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.internal.Fragment
import com.freeletics.khonshu.statemachine.StateMachine
import kotlin.reflect.KClass

/**
 * Add this annotation to a [androidx.compose.runtime.Composable] function. It is required that
 * the annotated function has `State` as first parameter and `(Action) -> Unit` as second parameter,
 * where `State` and `Action` match the given `StateMachine`.
 *
 * This will trigger the generation of
 * - a Fragment that sets up the annotated Composable with the given [stateMachine]
 * - a Dagger subcomponent that uses [scope] as scope marker and [parentScope] as `parentScope`
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ComposeFragment(
    val scope: KClass<*>,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val fragmentBaseClass: KClass<out Fragment> = Fragment::class,
)
