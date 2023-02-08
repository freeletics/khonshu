package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposableParameter
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.codegen.util.appScope
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentFqName
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionAnnotationReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.kotlinpoet.ClassName

@OptIn(ExperimentalAnvilApi::class)
internal val AnnotationReference.scope: ClassName
    get() = requireClassArgument("scope", 0)

@OptIn(ExperimentalAnvilApi::class)
internal val AnnotationReference.route: ClassName
    get() = requireClassArgument("route", 0)

@OptIn(ExperimentalAnvilApi::class)
internal val AnnotationReference.parentScope: ClassName
    get() = optionalClassArgument("parentScope", 1)?: appScope

@OptIn(ExperimentalAnvilApi::class)
internal val AnnotationReference.stateMachine: ClassName
    get() = requireClassArgument("stateMachine", 2)

@OptIn(ExperimentalAnvilApi::class)
internal val AnnotationReference.destinationType: String
    get() = optionalEnumArgument("destinationType", 3) ?: "SCREEN"

@OptIn(ExperimentalAnvilApi::class)
internal val AnnotationReference.destinationScope: ClassName
    get() = optionalClassArgument("destinationScope", 4) ?: appScope

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotatedReference.navEntryData(
    navigation: Navigation
): NavEntryData? {
    val annotation = findAnnotation(navEntryComponentFqName) ?: return null

    return NavEntryData(
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        navigation = navigation,
    )
}

@OptIn(ExperimentalAnvilApi::class)
internal val TopLevelFunctionReference.composeParameters: List<ComposableParameter>
    get() = parameters
        .filter { it.name != "state" && it.name != "sendAction" }
        .map {
            ComposableParameter(
                name = it.name,
                typeName = it.type().asTypeName()
            )
        }
