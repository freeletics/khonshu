package com.freeletics.mad.whetstone.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier.LATEINIT
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

internal val ClassName.propertyName: String get() {
    return simpleNames.first().replaceFirstChar(Char::lowercaseChar) +
            simpleNames.drop(1).joinToString { it.replaceFirstChar(Char::uppercaseChar) }
}

internal fun bindsInstanceParameter(name: String, className: ClassName): ParameterSpec {
    return ParameterSpec.builder(name, className)
        .addAnnotation(bindsInstance)
        .build()
}

internal fun simplePropertySpec(className: ClassName): PropertySpec {
    return PropertySpec.builder(className.propertyName, className).build()
}

internal fun lateinitPropertySpec(className: ClassName): PropertySpec {
    return PropertySpec.builder(className.propertyName, className)
        .addModifiers(PRIVATE, LATEINIT)
        .mutable()
        .build()
}

internal fun internalApiAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(internalWhetstoneApi).build()
}

internal fun optInAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(optIn)
        .addMember("%T::class", internalWhetstoneApi)
        .build()
}
