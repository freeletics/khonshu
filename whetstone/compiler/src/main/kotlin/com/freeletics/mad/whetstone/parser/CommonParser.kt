package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposableParameter
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.codegen.util.appScope
import com.freeletics.mad.whetstone.codegen.util.fragment
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentFqName
import com.freeletics.mad.whetstone.codegen.util.viewRendererFactoryFqName
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionClassReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import com.squareup.anvil.compiler.internal.reference.asClassName
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
internal fun AnnotationReference.fragmentBaseClass(index: Int): ClassName {
    return optionalClassArgument("fragmentBaseClass", index) ?: fragment
}

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

private const val STATE_PARAMETER = "state"
private const val SEND_ACTION_PARAMETER = "sendAction"

@OptIn(ExperimentalAnvilApi::class)
internal val TopLevelFunctionReference.stateParameter: ComposableParameter?
    get() = parameters
        .find { it.name == STATE_PARAMETER }
        ?.toComposableParameter()

@OptIn(ExperimentalAnvilApi::class)
internal val TopLevelFunctionReference.sendActionParameter: ComposableParameter?
    get() = parameters
        .find { it.name == SEND_ACTION_PARAMETER }
        ?.toComposableParameter()

@OptIn(ExperimentalAnvilApi::class)
internal val TopLevelFunctionReference.composeParameters: List<ComposableParameter>
    get() = parameters
        .filter { it.name != STATE_PARAMETER && it.name != SEND_ACTION_PARAMETER }
        .map { it.toComposableParameter() }

@OptIn(ExperimentalAnvilApi::class)
internal fun ClassReference.findRendererFactory(): ClassName {
    val factoryClass = innerClasses().find { innerClass ->
        innerClass.allSuperTypeClassReferences(false).any { superType ->
            superType.fqName == viewRendererFactoryFqName
        }
    }
    return factoryClass?.asClassName() ?:
    throw AnvilCompilationExceptionClassReference(this,
        "Couldn't find a ViewRender.Factory subclass nested inside $fqName"
    )
}
