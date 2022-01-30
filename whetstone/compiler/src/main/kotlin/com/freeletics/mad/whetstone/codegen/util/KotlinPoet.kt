package com.freeletics.mad.whetstone.codegen.util

import com.freeletics.mad.whetstone.CommonData
import com.squareup.anvil.annotations.ContributesSubcomponent
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.MergeSubcomponent
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
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

internal fun bindsInstanceParameter(spec: ParameterSpec): ParameterSpec {
    return spec.toBuilder()
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

internal fun componentAnnotation(
    scope: ClassName,
    dependencies: ClassName,
    module: ClassName? = null
): AnnotationSpec {
    return AnnotationSpec.builder(MergeComponent::class)
        .addMember("scope = %T::class", scope)
        .addMember("dependencies = [%T::class]", dependencies)
        .apply {
            if (module != null) {
                addMember("modules = [%T::class]", module)
            }
        }
        .build()
}

internal fun subcomponentAnnotation(scope: ClassName, parentScope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(ContributesSubcomponent::class)
        .addMember("scope = %T::class", scope)
        .addMember("parentScope = %T::class", parentScope)
        .build()
}

internal fun subcomponentFactoryAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(ContributesSubcomponent.Factory::class).build()
}

internal fun scopeToAnnotation(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(scopeTo)
        .addMember("%T::class", scope)
        .build()
}

internal fun contributesToAnnotation(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(ContributesTo::class)
        .addMember("%T::class", scope)
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

internal fun CommonData.Navigation?.asParameter(): ParameterSpec {
    if (this?.route != null) {
        return ParameterSpec.builder(route.propertyName, route).build()
    }
    return ParameterSpec.builder("arguments", bundle).build()
}

internal fun CommonData.Navigation?.fragmentConverter(): CodeBlock {
    if (this?.route != null) {
        return CodeBlock.of("%M<%T>()", requireRoute, route)
    }
    return CodeBlock.of("requireArguments()")
}
