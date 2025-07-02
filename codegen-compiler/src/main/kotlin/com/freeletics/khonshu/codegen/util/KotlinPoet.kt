package com.freeletics.khonshu.codegen.util

import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.Navigation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import dev.zacsweers.metro.ContributesGraphExtension
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

internal val ClassName.propertyName: String get() {
    return simpleNames.first().replaceFirstChar(Char::lowercaseChar) +
        simpleNames.drop(1).joinToString { it.replaceFirstChar(Char::uppercaseChar) }
}

internal fun providesParameter(
    name: String,
    className: ClassName,
    annotation: AnnotationSpec? = null,
): ParameterSpec {
    return ParameterSpec.builder(name, className)
        .addAnnotation(provides())
        .apply { if (annotation != null) addAnnotation(annotation) }
        .build()
}

internal fun providesParameter(
    spec: ParameterSpec,
    annotation: AnnotationSpec? = null,
): ParameterSpec {
    return spec.toBuilder()
        .addAnnotation(provides())
        .apply { if (annotation != null) addAnnotation(annotation) }
        .build()
}

internal fun navHostParameter(parameter: ComposableParameter): ParameterSpec {
    return ParameterSpec.builder(parameter.name, simpleNavHost)
        .build()
}

internal fun simplePropertySpec(className: ClassName): PropertySpec {
    return PropertySpec.builder(className.propertyName, className).build()
}

internal fun contributesGraphExtension(
    scope: ClassName,
    isExtendable: Boolean = true,
): AnnotationSpec {
    return AnnotationSpec.builder(ContributesGraphExtension::class)
        .addMember("%T::class", scope)
        .addMember("isExtendable = %L", isExtendable)
        .build()
}

internal fun contributesGraphExtensionFactory(
    parentScope: ClassName,
): AnnotationSpec {
    return AnnotationSpec.builder(ContributesGraphExtension.Factory::class)
        .addMember("%T::class", parentScope)
        .build()
}

internal fun singleIn(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(SingleIn::class)
        .addMember("%T::class", scope)
        .build()
}

internal fun contributesTo(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(ContributesTo::class)
        .addMember("%T::class", scope)
        .build()
}

internal fun multibinds(allowEmpty: Boolean): AnnotationSpec {
    return AnnotationSpec.builder(Multibinds::class)
        .addMember("allowEmpty = %L", allowEmpty)
        .build()
}

internal fun provides(): AnnotationSpec {
    return AnnotationSpec.builder(Provides::class).build()
}

internal fun intoSet(): AnnotationSpec {
    return AnnotationSpec.builder(IntoSet::class).build()
}

internal fun forScope(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(ForScope::class)
        .addMember("%T::class", scope)
        .build()
}

internal fun optIn(): AnnotationSpec {
    return optIn(InternalCodegenApi)
}

internal fun optIn(vararg classNames: ClassName): AnnotationSpec {
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
    return ParameterSpec.builder("intent", intent).build()
}

internal fun TypeName.asLambdaParameter(): TypeName {
    return LambdaTypeName.get(null, this, returnType = UNIT)
}

// KSP and Anvil don't have the same behavior for returning lambdas
// this turns all Function1 and Function2 types into lambdas
internal fun TypeName.functionToLambda(): TypeName {
    if (this is LambdaTypeName) {
        val parameters = parameters.map { it.toBuilder(type = it.type.functionToLambda()).build() }
        return LambdaTypeName.get(receiver, parameters, returnType.functionToLambda())
            .copy(nullable = isNullable)
    }
    if (this is ParameterizedTypeName && (rawType == function1 || rawType == function2 || rawType == function3)) {
        val parameters = typeArguments.dropLast(1).map { it.functionToLambda() }.toTypedArray()
        return LambdaTypeName.get(null, *parameters, returnType = typeArguments.last())
            .copy(nullable = isNullable)
    }
    if (this is ParameterizedTypeName) {
        return rawType.parameterizedBy(typeArguments.map { it.functionToLambda() })
            .copy(nullable = isNullable)
    }
    return this
}
