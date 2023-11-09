package com.freeletics.khonshu.codegen.parser.anvil

import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionAnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.MemberPropertyReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.TypeParameterReference
import com.squareup.anvil.compiler.internal.reference.argumentAt
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.name.FqName

internal fun AnnotatedReference.findAnnotation(fqName: FqName): AnnotationReference? {
    return annotations.find { it.fqName == fqName }
}

internal fun AnnotationReference.optionalClassArgument(name: String, index: Int): ClassName? {
    return optionalClassReferenceArgument(name, index)?.asClassName()
}

internal fun AnnotationReference.requireClassReferenceArgument(name: String, index: Int): ClassReference {
    return optionalClassReferenceArgument(name, index)
        ?: throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}

internal fun AnnotationReference.optionalClassReferenceArgument(name: String, index: Int): ClassReference? {
    return argumentAt(name, index)?.value()
}

internal val AnnotatedReference.packageName: String
    get() = when (this) {
        is ClassReference -> packageName
        is TopLevelFunctionReference -> packageName
        is MemberPropertyReference -> declaringClass.packageName
        is MemberFunctionReference -> declaringClass.packageName
        else -> throw UnsupportedOperationException("Can't retrieve packageName for $this")
    }

internal val TopLevelFunctionReference.packageName: String
    get() = when (this) {
        is TopLevelFunctionReference.Psi -> function.containingKtFile.packageFqName
        is TopLevelFunctionReference.Descriptor -> function.containingPackage()!!
    }.packageString()

internal val ClassReference.packageName: String
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
