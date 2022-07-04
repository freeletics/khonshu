package com.freeletics.mad.whetstone

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * This [Qualifier] is used for objects automatically provided inside a generated
 * subcomponent for [NavEntryComponent].
 */
@Qualifier
@Target(CLASS, FUNCTION, PROPERTY, PROPERTY_GETTER, FUNCTION, VALUE_PARAMETER)
@Retention(RUNTIME)
public annotation class NavEntry(
    val scope: KClass<*>,
)
