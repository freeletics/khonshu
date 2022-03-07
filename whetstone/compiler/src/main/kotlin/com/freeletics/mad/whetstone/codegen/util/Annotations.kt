@file:OptIn(ExperimentalAnvilApi::class)
package com.freeletics.mad.whetstone.codegen.util

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionAnnotationReference
import com.squareup.anvil.compiler.internal.reference.argumentAt
import com.squareup.anvil.compiler.internal.requireFqName
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.psi.KtConstantExpression

internal fun AnnotatedReference.findAnnotation(fqName: FqName): AnnotationReference? {
    return annotations.find { it.fqName == fqName }
}

internal fun AnnotationReference.requireClassArgument(
    name: String,
    index: Int,
    module: ModuleDescriptor
): ClassName {
    return optionalClassArgument(name, index, module) ?:
        throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}

internal fun AnnotationReference.optionalClassArgument(
    name: String,
    index: Int,
    module: ModuleDescriptor
): ClassName? {
    return argumentAt(name, index)
        ?.value<KtClassLiteralExpression>()
        ?.requireFqName(module)
        ?.asClassName(module)
}

//TODO replace with a way to get default value
internal fun AnnotationReference.optionalBooleanArgument(
    name: String,
    index: Int,
): Boolean? {
    return argumentAt(name, index)
        ?.value<KtConstantExpression>()
        ?.node
        ?.firstChildNode
        ?.text
        ?.toBoolean()
}
