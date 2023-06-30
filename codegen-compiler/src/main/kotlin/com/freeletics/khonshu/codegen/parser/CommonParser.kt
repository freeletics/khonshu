package com.freeletics.khonshu.codegen.parser

import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.NavEntryData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.codegen.util.appScope
import com.freeletics.khonshu.codegen.codegen.util.fragment
import com.freeletics.khonshu.codegen.codegen.util.navEntryComponentFqName
import com.freeletics.khonshu.codegen.codegen.util.viewRendererFactoryFqName
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionClassReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.TypeReference
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

internal val AnnotationReference.scope: ClassName
    get() = requireClassArgument("scope", 0)

internal val AnnotationReference.route: ClassName
    get() = requireClassArgument("route", 0)

internal val AnnotationReference.parentScope: ClassName
    get() = optionalClassArgument("parentScope", 1) ?: appScope

internal val AnnotationReference.stateMachine: ClassName
    get() = stateMachineReference.asClassName()

internal val AnnotationReference.stateMachineReference: ClassReference
    get() = requireClassReferenceArgument("stateMachine", 2)

internal val AnnotationReference.destinationType: String
    get() = optionalEnumArgument("destinationType", 3) ?: "SCREEN"

internal val AnnotationReference.destinationScope: ClassName
    get() = optionalClassArgument("destinationScope", 4) ?: appScope

internal fun AnnotationReference.fragmentBaseClass(index: Int): ClassName {
    return optionalClassArgument("fragmentBaseClass", index) ?: fragment
}

internal fun AnnotatedReference.navEntryData(
    navigation: Navigation,
): NavEntryData? {
    val annotation = findAnnotation(navEntryComponentFqName) ?: return null

    return NavEntryData(
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        navigation = navigation,
    )
}

internal fun TopLevelFunctionReference.getStateParameter(stateParameter: TypeName): ComposableParameter? {
    return parameters
        .find { it.type().asTypeName() == stateParameter }
        ?.toComposableParameter()
}

internal fun TopLevelFunctionReference.getSendActionParameter(actionParameter: TypeName): ComposableParameter? {
    return parameters
        .find { it.type().asTypeName() == actionParameter }
        ?.toComposableParameter()
}

internal fun TopLevelFunctionReference.getComposeParameters(
    stateParameter: TypeName,
    actionParameter: TypeName,
): List<ComposableParameter> {
    return parameters
        .filter {
            val type = it.type().asTypeName()
            type != stateParameter && type != actionParameter
        }
        .map { it.toComposableParameter() }
}

internal fun ClassReference.stateMachineStateParameter(stateMachineSuperType: List<TypeReference>): TypeName {
    return resolveTypeParameter("State", stateMachineSuperType)
}

internal fun ClassReference.stateMachineActionParameter(stateMachineSuperType: List<TypeReference>): TypeName {
    return resolveTypeParameter("Action", stateMachineSuperType)
}

internal fun ClassReference.stateMachineActionFunctionParameter(stateMachineSuperType: List<TypeReference>): TypeName {
    return stateMachineActionParameter(stateMachineSuperType).asFunction1Parameter()
}

internal fun ClassReference.findRendererFactory(): ClassName {
    val factoryClass = innerClasses().find { innerClass ->
        innerClass.allSuperTypeClassReferences().any { superType ->
            superType.fqName == viewRendererFactoryFqName
        }
    }
    return factoryClass?.asClassName()
        ?: throw AnvilCompilationExceptionClassReference(
            this,
            "Couldn't find a ViewRender.Factory subclass nested inside $fqName",
        )
}
