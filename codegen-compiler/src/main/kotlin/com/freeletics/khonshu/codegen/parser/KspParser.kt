package com.freeletics.khonshu.codegen.parser

import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.util.asLambdaParameter
import com.freeletics.khonshu.codegen.util.baseRoute
import com.freeletics.khonshu.codegen.util.overlay
import com.freeletics.khonshu.codegen.util.simpleNavHost
import com.freeletics.khonshu.codegen.util.simpleNavHostLambda
import com.freeletics.khonshu.codegen.util.stateMachine
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import org.jetbrains.annotations.VisibleForTesting

internal fun KSFunctionDeclaration.toComposeScreenDestinationData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): NavDestinationData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    val navigation = Navigation(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScope.extendsBaseRoute(resolver),
        overlay = annotation.route.extendsOverlay(resolver),
        destinationScope = annotation.destinationScope,
    )

    return NavDestinationData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = this.getParameterWithType(stateParameter),
        sendActionParameter = this.getParameterWithType(actionParameter),
    )
}

internal fun KSFunctionDeclaration.toNavHostActivityData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): NavHostActivityData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    val navHostParameter = navHostParameter(logger) ?: return null

    return NavHostActivityData(
        originalName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        activityBaseClass = annotation.activityBaseClass,
        experimentalNavigation = annotation.experimentalNavigation,
        navHostParameter = navHostParameter,
        composableParameter = getInjectedParameters(stateParameter, actionParameter, navHostParameter.typeName),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}

private val KSAnnotation.scope: ClassName
    get() = (findArgument("scope").value as KSType).toClassName()

private val KSAnnotation.route: ClassName
    get() = (findArgument("route").value as KSType).toClassName()

private val KSAnnotation.parentScope: ClassName
    get() = (findArgument("parentScope").value as KSType).toClassName()

private val KSAnnotation.stateMachine: ClassName
    get() = (findArgument("stateMachine").value as KSType).toClassName()

private val KSAnnotation.destinationScope: ClassName
    get() = (findArgument("destinationScope").value as KSType).toClassName()

private val KSAnnotation.activityBaseClass: ClassName
    get() = (findArgument("activityBaseClass").value as KSType).toClassName()

internal val KSAnnotation.experimentalNavigation: Boolean
    get() = findArgument("experimentalNavigation").value as Boolean

private fun KSAnnotation.findArgument(name: String): KSValueArgument {
    return arguments.find { it.name?.asString() == name }
        ?: defaultArguments.find { it.name?.asString() == name }!!
}

private fun KSFunctionDeclaration.getParameterWithType(expectedType: TypeName): ComposableParameter? {
    return parameters.firstNotNullOfOrNull { parameter ->
        parameter.toComposableParameter { it == expectedType }
    }
}

private fun KSFunctionDeclaration.getInjectedParameters(vararg exclude: TypeName): List<ComposableParameter> {
    return parameters.mapNotNull { parameter ->
        parameter.toComposableParameter { !exclude.contains(it) }
    }
}

private fun KSValueParameter.toComposableParameter(condition: (TypeName) -> Boolean): ComposableParameter? {
    val type = type.toTypeName()
    return if (condition(type)) {
        ComposableParameter(name!!.asString(), type)
    } else {
        null
    }
}

private fun KSFunctionDeclaration.navHostParameter(logger: KSPLogger): ComposableParameter? {
    val parameter = getParameterWithType(simpleNavHost) ?: getParameterWithType(simpleNavHostLambda)
    if (parameter == null) {
        logger.error("Could not find a NavHost parameter with type $simpleNavHost")
    }
    return parameter
}

private fun ClassName.stateMachineParameters(resolver: Resolver, logger: KSPLogger): Pair<TypeName, TypeName>? {
    val stateMachineDeclaration = resolver.getClassDeclarationByName(toString())!!

    val stateMachineType = stateMachineDeclaration.allSuperTypes(true).firstNotNullOfOrNull { superType ->
        superType.asParameterized()?.takeIf { it.rawType == stateMachine }
    }
    if (stateMachineType == null) {
        logger.error("Could not find StateMachine super type for $this")
        return null
    }

    val stateParameter = stateMachineType.typeArguments[0]
    val actionParameter = stateMachineType.typeArguments[1].asLambdaParameter()
    return stateParameter to actionParameter
}

private fun ClassName.extendsBaseRoute(resolver: Resolver): Boolean {
    val declaration = resolver.getClassDeclarationByName(toString())!!
    return declaration.allSuperTypes(false).any { it == baseRoute }
}

private fun ClassName.extendsOverlay(resolver: Resolver): Boolean {
    val declaration = resolver.getClassDeclarationByName(toString())!!
    return declaration.allSuperTypes(false).any { it == overlay }
}

private fun TypeName.asParameterized(): ParameterizedTypeName? {
    return this as? ParameterizedTypeName
}

/**
 * Creates a [Sequence] of all direct and indirect super types. This uses a depth first search.
 *
 * While walking through the type hierarchy the method will resolve type parameters so that the emitted
 * `TypeName` values will have resolved `typeArguments` instead of intermediate `TypeVariableNames`. See
 * [updateWith] for more details.
 */
@VisibleForTesting
internal fun KSClassDeclaration.allSuperTypes(resolveTypeParameters: Boolean): Sequence<TypeName> {
    return allSuperTypes(toClassName(), resolveTypeParameters)
}

private fun KSClassDeclaration.allSuperTypes(
    typeName: TypeName,
    resolveTypeParameters: Boolean,
): Sequence<TypeName> = sequence {
    val parentTypeParameters = typeParameters
    val parentTypeArguments = (typeName as? ParameterizedTypeName)?.typeArguments ?: emptyList()
    superTypes.forEach { superTypeReference ->
        val superType = superTypeReference.resolve()
        var superTypeName: TypeName
        if (resolveTypeParameters) {
            superTypeName = superType.toTypeName(parentTypeParameters.toTypeParameterResolver())
            if (parentTypeArguments.isNotEmpty() && superTypeName is ParameterizedTypeName) {
                superTypeName = superTypeName.updateWith(parentTypeArguments, parentTypeParameters)
            }
        } else if (superType is KSClassDeclaration) {
            superTypeName = (superType as KSClassDeclaration).toClassName()
        } else {
            superTypeName = superType.toTypeName()
        }
        if (superTypeName is ParameterizedTypeName && !resolveTypeParameters) {
            superTypeName = superTypeName.rawType
        }
        yield(superTypeName)

        val superDeclaration = superType.declaration as? KSClassDeclaration ?: return@forEach
        yieldAll(superDeclaration.allSuperTypes(superTypeName, resolveTypeParameters))
    }
}

/**
 * Changes the type arguments of the `ParameterizedTypeName` this is called on to the
 * real resolved `TypeName` values given to this function.
 *
 * Example type hierarchy (that the type parameter order gets swapped is intentional):
 * - TestStateMachine : FooStateMachine<com.test.TestAction, com.test.TestState>
 * - FooStateMachine<A, S> : StateMachine<S, A>
 * - StateMachine<State, Action>
 *
 * When this is called for `StateMachine<S, A>`, `parentTypeArguments` will be
 * `[com.test.TestAction, com.test.TestState]` and `parentTypeParameters` will be `[A, S]`.
 *
 * The function will then iterate through `[S, A]` from the `ParameterizedTypeName`. In each iteration it will
 * find the index of the variable name in `parentTypeParameters` and then use that index to get the actual
 * type from `parentTypeArguments`.
 *
 * Example:
 * 1) First iteration
 *      a) search `S`
 *      b) index in `parentTypeParameters` is `1`
 *      c) `TypeName` at index `1` in `parentTypeArguments` is `com.test.TestState`
 * 2) Second iteration
 *      a) search `A`
 *      b) index in `parentTypeParameters` is `0`
 *      c) `TypeName` at index `0` in `parentTypeArguments` is `com.test.TestAction`
 *
 * Result: `StateMachine<S, A>` becomes `StateMachine<com.test.TestState, com.test.TestAction>`
 */
private fun ParameterizedTypeName.updateWith(
    parentTypeArguments: List<TypeName>,
    parentTypeParameters: List<KSTypeParameter>,
): ParameterizedTypeName {
    val updated = typeArguments.map { argument ->
        if (argument is TypeVariableName) {
            val index = parentTypeParameters.indexOfFirst { it.name.asString() == argument.name }
            if (index >= 0) {
                parentTypeArguments[index]
            } else {
                argument
            }
        } else {
            argument
        }
    }
    return rawType.parameterizedBy(updated)
}
