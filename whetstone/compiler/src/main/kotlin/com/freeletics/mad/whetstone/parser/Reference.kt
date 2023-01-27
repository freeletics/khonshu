package com.freeletics.mad.whetstone.parser

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionAnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.FunctionReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.MemberPropertyReference
import com.squareup.anvil.compiler.internal.reference.PropertyReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.argumentAt
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.name.FqName

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotatedReference.findAnnotation(fqName: FqName): AnnotationReference? {
    return annotations.find { it.fqName == fqName }
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.requireClassArgument(name: String, index: Int): ClassName {
    return optionalClassArgument(name, index) ?:
        throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.optionalClassArgument(name: String, index: Int): ClassName? {
    return argumentAt(name, index)?.value<ClassReference>()?.asClassName()
}

@OptIn(ExperimentalAnvilApi::class)
internal fun AnnotationReference.requireEnumArgument(name: String, index: Int): String {
    return argumentAt(name, index)?.value<FqName>()?.shortName()?.asString() ?:
        throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
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
