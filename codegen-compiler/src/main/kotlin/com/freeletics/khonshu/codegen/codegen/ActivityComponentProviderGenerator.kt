package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.activityComponentProvider
import com.freeletics.khonshu.codegen.util.componentActivity
import com.freeletics.khonshu.codegen.util.getComponent
import com.freeletics.khonshu.codegen.util.multiStackHostNavigatorViewModel
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.savedStateViewModelFactory
import com.freeletics.khonshu.codegen.util.viewModelProvider
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.FINAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

internal class ActivityComponentProviderGenerator(
    override val data: NavHostActivityData,
) : Generator<NavHostActivityData>() {
    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(componentProviderClassName)
            .addAnnotation(optInAnnotation())
            .addSuperinterface(activityComponentProvider)
            .primaryConstructor(constructor())
            .addProperty(activityProperty())
            .addFunction(provideFunction())
            .build()
    }

    private fun constructor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("activity", componentActivity)
            .build()
    }

    private fun activityProperty(): PropertySpec {
        return PropertySpec.builder("activity", componentActivity)
            .addModifiers(PRIVATE, FINAL)
            .initializer("activity")
            .build()
    }

    private fun provideFunction(): FunSpec {
        val typeVariable = TypeVariableName("C")
        return FunSpec.builder("provide")
            .addModifiers(OVERRIDE)
            .addTypeVariable(typeVariable)
            .addParameter("scope", KClass::class.asClassName().parameterizedBy(STAR))
            .returns(typeVariable)
            .beginControlFlow(
                "return %M(activity, scope, %T::class, %T::class) { parentComponent: %T, savedStateHandle ->",
                getComponent,
                data.scope,
                data.parentScope,
                retainedParentComponentClassName,
            )
            .addStatement(
                "val viewModel = %T(activity, %T())[%T::class.java]",
                viewModelProvider,
                savedStateViewModelFactory,
                multiStackHostNavigatorViewModel,
            )
            .addStatement(
                "parentComponent.%L().%L(viewModel, savedStateHandle, activity.intent)",
                retainedParentComponentGetterName,
                RETAINED_COMPONENT_FACTORY_CREATE_NAME,
            )
            .endControlFlow()
            .build()
    }
}
