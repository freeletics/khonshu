package com.freeletics.khonshu.codegen.util

import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.Navigation
import com.squareup.anvil.annotations.ContributesSubcomponent
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.ForScope
import com.squareup.anvil.annotations.optional.SingleIn
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier.LATEINIT
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT

internal val ClassName.propertyName: String get() {
    return simpleNames.first().replaceFirstChar(Char::lowercaseChar) +
        simpleNames.drop(1).joinToString { it.replaceFirstChar(Char::uppercaseChar) }
}

internal fun bindsInstanceParameter(
    name: String,
    className: ClassName,
    annotation: AnnotationSpec? = null,
): ParameterSpec {
    return ParameterSpec.builder(name, className)
        .addAnnotation(bindsInstance)
        .apply { if (annotation != null) addAnnotation(annotation) }
        .build()
}

internal fun bindsInstanceParameter(
    spec: ParameterSpec,
    annotation: AnnotationSpec? = null,
): ParameterSpec {
    return spec.toBuilder()
        .addAnnotation(bindsInstance)
        .apply { if (annotation != null) addAnnotation(annotation) }
        .build()
}

internal fun TypeName.jvmSuppressWildcards(): TypeName {
    val suppress = AnnotationSpec.builder(JvmSuppressWildcards::class).build()
    return copy(annotations = annotations + suppress)
}

internal fun navHostParameter(parameter: ComposableParameter): ParameterSpec {
    return ParameterSpec.builder(parameter.name, simpleNavHost)
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

internal fun subcomponentAnnotation(
    scope: ClassName,
    parentScope: ClassName,
    module: ClassName? = null,
): AnnotationSpec {
    return AnnotationSpec.builder(ContributesSubcomponent::class)
        .addMember("scope = %T::class", scope)
        .addMember("parentScope = %T::class", parentScope)
        .apply {
            if (module != null) {
                addMember("modules = [%T::class]", module)
            }
        }
        .build()
}

internal fun subcomponentFactoryAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(ContributesSubcomponent.Factory::class).build()
}

internal fun scopeToAnnotation(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(SingleIn::class)
        .addMember("%T::class", scope)
        .build()
}

internal fun contributesToAnnotation(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(ContributesTo::class)
        .addMember("%T::class", scope)
        .build()
}

internal fun forScope(scope: ClassName, target: UseSiteTarget? = null): AnnotationSpec {
    return AnnotationSpec.builder(ForScope::class)
        .addMember("%T::class", scope)
        .useSiteTarget(target)
        .build()
}

internal fun optInAnnotation(): AnnotationSpec {
    return optInAnnotation(InternalCodegenApi)
}

internal fun optInAnnotation(vararg classNames: ClassName): AnnotationSpec {
    val member = CodeBlock.builder()
    classNames.forEachIndexed { index, className ->
        if (index > 0) {
            member.add(", ")
        }
        member.add("%T::class", className)
    }
    return AnnotationSpec.builder(optIn)
        .addMember(member.build())
        .build()
}

internal fun Navigation?.asParameter(): ParameterSpec {
    if (this != null) {
        return ParameterSpec.builder(route.propertyName, route).build()
    }
    return ParameterSpec.builder("arguments", bundle).build()
}

internal fun TypeName.asLambdaParameter(): TypeName {
    return LambdaTypeName.get(null, this, returnType = UNIT)
}

// KSP and Anvil don't have the same behavior for returning lambdas
// this turns all Function1 and Function2 types into lambdas
internal fun TypeName.functionToLambda(): TypeName {
    if (this is ParameterizedTypeName && (rawType == function1 || rawType == function2 || rawType == function3)) {
        val parameters = typeArguments.dropLast(1).map { it.functionToLambda() }.toTypedArray()
        return LambdaTypeName.get(null, *parameters, returnType = typeArguments.last())
            .copy(nullable = isNullable)
    }
    return this
}
