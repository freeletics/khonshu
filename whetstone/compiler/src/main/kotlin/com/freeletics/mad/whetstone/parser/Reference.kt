package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposableParameter
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionAnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionClassReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.MemberPropertyReference
import com.squareup.anvil.compiler.internal.reference.ParameterReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.TypeReference
import com.squareup.anvil.compiler.internal.reference.argumentAt
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.name.FqName

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotatedReference.findAnnotation(fqName: FqName): AnnotationReference? {
    return annotations.find { it.fqName == fqName }
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.requireClassArgument(name: String, index: Int): ClassName {
    return requireClassReferenceArgument(name, index).asClassName()
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.optionalClassArgument(name: String, index: Int): ClassName? {
    return optionalClassReferenceArgument(name, index)?.asClassName()
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.requireClassReferenceArgument(name: String, index: Int): ClassReference {
    return optionalClassReferenceArgument(name, index) ?:
        throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.optionalClassReferenceArgument(name: String, index: Int): ClassReference? {
    return argumentAt(name, index)?.value()
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.requireEnumArgument(name: String, index: Int): String {
    return optionalEnumArgument(name, index) ?:
        throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.optionalEnumArgument(name: String, index: Int): String? {
    return argumentAt(name, index)?.value<FqName>()?.shortName()?.asString()
}

@OptIn(ExperimentalAnvilApi::class)
internal fun ParameterReference.toComposableParameter(): ComposableParameter {
    return ComposableParameter(
        name = name,
        typeName = type().asTypeName()
    )
}

@OptIn(ExperimentalAnvilApi::class)
internal val AnnotatedReference.packageName: String
    get() = when (this) {
        is ClassReference -> packageName
        is TopLevelFunctionReference -> packageName
        is MemberPropertyReference -> declaringClass.packageName
        is MemberFunctionReference -> declaringClass.packageName
        else -> throw UnsupportedOperationException("Can't retrieve packageName for $this")
    }

@OptIn(ExperimentalAnvilApi::class)
internal val TopLevelFunctionReference.packageName: String
    get() = when (this) {
        is TopLevelFunctionReference.Psi -> function.containingKtFile.packageFqName
        is TopLevelFunctionReference.Descriptor -> function.containingPackage()!!
    }.packageString()

@OptIn(ExperimentalAnvilApi::class)
internal val ClassReference.packageName: String
    get() = packageFqName.packageString()

private fun FqName.packageString(): String {
    return pathSegments().joinToString(separator = ".")
}

internal fun TypeName.asFunction1Parameter(): TypeName {
    return Function1::class.asClassName().parameterizedBy(this, UNIT)
}

@OptIn(ExperimentalAnvilApi::class)
public fun ClassReference.resolveTypeParameter(
    parameter: String,
    superTypes: List<TypeReference>,
): TypeName {
    var currentName = parameter
    superTypes.forEach { superType ->
        // find the index of the type parameters that the current class has
        // e.g. for a StateMachine and State this would return 0
        val index = superType.asClassReference().typeParameters.indexOfFirst { it.name == currentName }
        // this is the type that is used in the implementation
        // e.g. for ... : StateMachine<S, A> this would be S
        val unwrappedType = superType.unwrappedTypes[index]
        // resolve the type using the implementation class
        val resolved = unwrappedType.resolveGenericTypeOrNull(this)
        if (resolved != null) {
            return resolved.asTypeName()
        }
        currentName = unwrappedType.asTypeName().toString()
    }
    throw AnvilCompilationExceptionClassReference(this, "Error resolving type parameters of $fqName")
}

@OptIn(ExperimentalAnvilApi::class)
public fun ClassReference.superTypeReference(superClass: FqName): List<TypeReference> {
    fun ClassReference.depthFirstSearch(superClass: FqName): List<TypeReference>? {
        directSuperTypeReferences().forEach {
            val classReference = it.asClassReferenceOrNull()
            if (classReference != null) {
                if (classReference.fqName == superClass) {
                    return listOf(it)
                }

                val fromSuperClasses = classReference.depthFirstSearch(superClass)
                if (fromSuperClasses != null) {
                    return fromSuperClasses + it
                }
            }
        }
        return null
    }

    return depthFirstSearch(superClass) ?: throw AnvilCompilationExceptionClassReference(this,
        "$fqName does not extend $superClass"
    )
}
