package com.freeletics.mad.codegen.compose

import com.freeletics.mad.codegen.AppScope
import com.freeletics.mad.statemachine.StateMachine
import kotlin.reflect.KClass

/**
 * Add this annotation to a [androidx.compose.runtime.Composable] function. It is required that
 * the annotated function has `State` as first parameter and `(Action) -> Unit` as second parameter,
 * where `State` and `Action` match the given `StateMachine`.
 *
 * This will trigger the generation of
 * - a wrapper Composable that sets up the annotated Composable with the given [stateMachine]
 * - a Dagger subcomponent that uses [scope] as scope marker and [parentScope] as `parentScope`
 *
 * The generated Composable will have the same name as the annotated one with `Mad` as prefix
 * and takes a `Bundle` of arguments as its sole parameter.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ComposeScreen(
    val scope: KClass<*>,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
)
