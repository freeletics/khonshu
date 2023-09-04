package com.freeletics.khonshu.codegen.parser.anvil

import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.codegen.util.activityScope
import com.freeletics.khonshu.codegen.codegen.util.appScope
import com.freeletics.khonshu.codegen.codegen.util.asLambdaParameter
import com.freeletics.khonshu.codegen.codegen.util.baseRoute
import com.freeletics.khonshu.codegen.codegen.util.baseRouteFqName
import com.freeletics.khonshu.codegen.codegen.util.fragment
import com.freeletics.khonshu.codegen.codegen.util.function2
import com.freeletics.khonshu.codegen.codegen.util.functionToLambda
import com.freeletics.khonshu.codegen.codegen.util.navHostLambda
import com.freeletics.khonshu.codegen.codegen.util.stateMachine
import com.freeletics.khonshu.codegen.codegen.util.viewRendererFactoryFqName
import com.freeletics.khonshu.navigation.NavRoot
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionClassReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionFunctionReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.ParameterReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName

internal val AnnotationReference.scope: ClassName
    get() = optionalClassArgument("scope", 0) ?: activityScope

internal val AnnotationReference.route: ClassName
    get() = requireClassArgument("route", 0)

internal val AnnotationReference.parentScope: ClassName
    get() = parentScopeReference?.asClassName() ?: appScope

internal val AnnotationReference.parentScopeReference: ClassReference?
    get() = optionalClassReferenceArgument("parentScope", 1)

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

internal val AnnotationReference.activityBaseClass: ClassName
    get() = requireClassReferenceArgument("activityBaseClass", 3).asClassName()

internal fun TopLevelFunctionReference.getParameterWithType(expectedType: TypeName): ComposableParameter? {
    return parameters.firstNotNullOfOrNull { parameter ->
        parameter.toComposableParameter { it == expectedType }
    }
}

internal fun TopLevelFunctionReference.getInjectedParameters(vararg exclude: TypeName): List<ComposableParameter> {
    return parameters.mapNotNull { parameter ->
        parameter.toComposableParameter { !exclude.contains(it) }
    }
}

private fun ParameterReference.toComposableParameter(condition: (TypeName) -> Boolean): ComposableParameter? {
    return type().asTypeName().functionToLambda().takeIf(condition)?.let { ComposableParameter(name, it) }
}

internal fun TopLevelFunctionReference.navHostParameter(): ComposableParameter {
    return getParameterWithType(navHostLambda)
        ?: throw AnvilCompilationExceptionFunctionReference(
            this,
            "Could not find a NavHost parameter with type $navHostLambda",
        )
}

internal fun ClassReference.stateMachineParameters(): Pair<TypeName, TypeName> {
    val stateMachineType = allSuperTypes().firstNotNullOfOrNull { superType ->
        (superType as? ParameterizedTypeName)?.takeIf { it.rawType == stateMachine }
    } ?: throw AnvilCompilationExceptionClassReference(
        this,
        "Couldn't find a StateMachine in type hierarchy",
    )

    val stateParameter = stateMachineType.typeArguments[0]
    val actionParameter = stateMachineType.typeArguments[1].asLambdaParameter()
    return stateParameter to actionParameter
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

internal fun ClassReference?.extendsBaseRoute(): Boolean {
    return this?.allSuperTypeClassReferences()?.any { superType ->
        superType.fqName == baseRouteFqName
    } == true
}
