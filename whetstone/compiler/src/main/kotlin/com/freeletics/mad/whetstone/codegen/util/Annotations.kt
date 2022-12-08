@file:OptIn(ExperimentalAnvilApi::class)
package com.freeletics.mad.whetstone.codegen.util

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionAnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.argumentAt
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

internal fun AnnotatedReference.findAnnotation(fqName: FqName): AnnotationReference? {
    return annotations.find { it.fqName == fqName }
}

internal fun AnnotationReference.requireClassArgument(name: String, index: Int): ClassName {
    return optionalClassArgument(name, index) ?:
        throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}

internal fun AnnotationReference.optionalClassArgument(name: String, index: Int): ClassName? {
    return argumentAt(name, index)?.value<ClassReference>()?.asClassName()
}

internal fun AnnotationReference.requireEnumArgument(name: String, index: Int): String {
    return argumentAt(name, index)?.value<FqName>()?.shortName()?.asString() ?:
        throw AnvilCompilationExceptionAnnotationReference(this, "Couldn't find $name for $fqName")
}
