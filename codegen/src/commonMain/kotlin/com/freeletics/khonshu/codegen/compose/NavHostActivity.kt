package com.freeletics.khonshu.codegen.compose

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.statemachine.StateMachine
import kotlin.reflect.KClass

/**
 * TODO
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NavHostActivity(
    val scope: KClass<*> = ActivityScope::class,
    val parentScope: KClass<*> = AppScope::class,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val activityBaseClass: KClass<*>,
)
