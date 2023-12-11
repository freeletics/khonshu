package com.freeletics.khonshu.codegen.parser

import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.util.activityScope
import com.freeletics.khonshu.codegen.util.appScope
import com.freeletics.khonshu.codegen.util.asLambdaParameter
import com.freeletics.khonshu.codegen.util.baseRouteFqName
import com.freeletics.khonshu.codegen.util.functionToLambda
import com.freeletics.khonshu.codegen.util.navDestinationFqName
import com.freeletics.khonshu.codegen.util.navHostActivityFqName
import com.freeletics.khonshu.codegen.util.overlayFqName
import com.freeletics.khonshu.codegen.util.simpleNavHost
import com.freeletics.khonshu.codegen.util.simpleNavHostLambda
import com.freeletics.khonshu.codegen.util.stateMachine
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionAnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionClassReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionFunctionReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.MemberPropertyReference
import com.squareup.anvil.compiler.internal.reference.ParameterReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.TypeParameterReference
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import com.squareup.anvil.compiler.internal.reference.argumentAt
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.name.FqName

internal fun TopLevelFunctionReference.toComposeScreenDestinationData(): NavDestinationData? {
    val annotation = findAnnotation(navDestinationFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val (stateParameter, actionParameter) = stateMachine.stateMachineParameters()

    val navigation = Navigation(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScopeReference.extendsBaseRoute(),
        overlay = annotation.routeReference.extendsOverlay(),
        destinationScope = annotation.destinationScope,
    )

    return NavDestinationData(
        baseName = name,
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}

internal fun TopLevelFunctionReference.toNavHostActivityData(): List<NavHostActivityData> {
    return findAnnotations(navHostActivityFqName).map {
        toNavHostActivityData(it)
    }
}

internal fun TopLevelFunctionReference.toNavHostActivityData(annotation: AnnotationReference): NavHostActivityData {
    val stateMachine = annotation.stateMachineReference
    val (stateParameter, actionParameter) = stateMachine.stateMachineParameters()

    val navHostParameter = navHostParameter()

    return NavHostActivityData(
        originalName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        activityBaseClass = annotation.activityBaseClass,
        experimentalNavigation = annotation.experimentalNavigation,
        navHostParameter = navHostParameter,
        composableParameter = getInjectedParameters(stateParameter, actionParameter, navHostParameter.typeName),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}

private val AnnotationReference.scope: ClassName
    get() = optionalClassArgument("scope", 0) ?: activityScope

private val AnnotationReference.route: ClassName
    get() = routeReference.asClassName()

private val AnnotationReference.routeReference: ClassReference
    get() = requireClassReferenceArgument("route", 0)

private val AnnotationReference.parentScope: ClassName
    get() = parentScopeReference?.asClassName() ?: appScope

private val AnnotationReference.parentScopeReference: ClassReference?
    get() = optionalClassReferenceArgument("parentScope", 1)

private val AnnotationReference.stateMachineReference: ClassReference
    get() = requireClassReferenceArgument("stateMachine", 2)

private val AnnotationReference.destinationScope: ClassName
    get() = optionalClassArgument("destinationScope", 4) ?: appScope

private val AnnotationReference.activityBaseClass: ClassName
    get() = requireClassReferenceArgument("activityBaseClass", 3).asClassName()

internal val AnnotationReference.experimentalNavigation: Boolean
    get() = argumentAt("experimentalNavigation", 4)?.value() ?: false

private fun TopLevelFunctionReference.getParameterWithType(expectedType: TypeName): ComposableParameter? {
    return parameters.firstNotNullOfOrNull { parameter ->
        parameter.toComposableParameter { it == expectedType }
    }
}

private fun TopLevelFunctionReference.getInjectedParameters(vararg exclude: TypeName): List<ComposableParameter> {
    return parameters.mapNotNull { parameter ->
        parameter.toComposableParameter { !exclude.contains(it) }
    }
}

private fun ParameterReference.toComposableParameter(condition: (TypeName) -> Boolean): ComposableParameter? {
    return type().asTypeName().functionToLambda().takeIf(condition)?.let { ComposableParameter(name, it) }
}

private fun TopLevelFunctionReference.navHostParameter(): ComposableParameter {
    return getParameterWithType(simpleNavHost) ?: getParameterWithType(simpleNavHostLambda)
        ?: throw AnvilCompilationExceptionFunctionReference(
            this,
            "Could not find a NavHost parameter with type $simpleNavHost",
        )
}

private fun ClassReference.stateMachineParameters(): Pair<TypeName, TypeName> {
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

private fun ClassReference?.extendsBaseRoute(): Boolean {
    return this?.allSuperTypeClassReferences()?.any { superType ->
        superType.fqName == baseRouteFqName
    } == true
}

private fun ClassReference?.extendsOverlay(): Boolean {
    return this?.allSuperTypeClassReferences()?.any { superType ->
        superType.fqName == overlayFqName
    } == true
}

private fun AnnotatedReference.findAnnotation(fqName: FqName): AnnotationReference? {
    return annotations.find { it.fqName == fqName }
}

private fun AnnotatedReference.findAnnotations(fqName: FqName): List<AnnotationReference> {
    return annotations.filter { it.fqName == fqName }
}

private fun AnnotationReference.optionalClassArgument(name: String, index: Int): ClassName? {
    return optionalClassReferenceArgument(name, index)?.asClassName()
}

private fun AnnotationReference.requireClassReferenceArgument(name: String, index: Int): ClassReference {
    return optionalClassReferenceArgument(name, index)
        ?: throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}

private fun AnnotationReference.optionalClassReferenceArgument(name: String, index: Int): ClassReference? {
    return argumentAt(name, index)?.value()
}

private val AnnotatedReference.packageName: String
    get() = when (this) {
        is ClassReference -> packageName
        is TopLevelFunctionReference -> packageName
        is MemberPropertyReference -> declaringClass.packageName
        is MemberFunctionReference -> declaringClass.packageName
        else -> throw UnsupportedOperationException("Can't retrieve packageName for $this")
    }

private val TopLevelFunctionReference.packageName: String
    get() = when (this) {
        is TopLevelFunctionReference.Psi -> function.containingKtFile.packageFqName
        is TopLevelFunctionReference.Descriptor -> function.containingPackage()!!
    }.packageString()

private val ClassReference.packageName: String
    get() = packageFqName.packageString()

private fun FqName.packageString(): String {
    return pathSegments().joinToString(separator = ".")
}

/**
 * Creates a [Sequence] of all direct and indirect super types. This uses a depth first search.
 *
 * While walking through the type hierarchy the method will resolve type parameters so that the emitted
 * `TypeName` values will have resolved `typeArguments` instead of intermediate `TypeVariableNames`. See
 * [updateWith] for more details.
 */
@VisibleForTesting
internal fun ClassReference.allSuperTypes(): Sequence<TypeName> = allSuperTypes(asClassName())

private fun ClassReference.allSuperTypes(
    typeName: TypeName,
): Sequence<TypeName> = sequence {
    val parentTypeParameters = typeParameters
    val parentTypeArguments = (typeName as? ParameterizedTypeName)?.typeArguments ?: emptyList()
    directSuperTypeReferences().forEach { superType ->
        var superTypeName = superType.asTypeName()
        if (parentTypeArguments.isNotEmpty() && superTypeName is ParameterizedTypeName) {
            superTypeName = superTypeName.updateWith(parentTypeArguments, parentTypeParameters)
        }
        yield(superTypeName)

        val superDeclaration = superType.asClassReferenceOrNull() ?: return@forEach
        yieldAll(superDeclaration.allSuperTypes(superTypeName))
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
    parentTypeParameters: List<TypeParameterReference>,
): ParameterizedTypeName {
    val updated = this.typeArguments.map { argument ->
        if (argument is TypeVariableName) {
            val index = parentTypeParameters.indexOfFirst { it.name == argument.name }
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
