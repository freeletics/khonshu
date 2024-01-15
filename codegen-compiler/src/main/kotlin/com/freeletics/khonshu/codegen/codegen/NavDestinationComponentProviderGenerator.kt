package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.util.activityComponentProvider
import com.freeletics.khonshu.codegen.util.componentProvider
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.navigationExecutor
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.propertyName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.componentProviderClassName
    get() = ClassName("Khonshu${data.baseName}ComponentProvider")

internal class NavDestinationComponentProviderGenerator(
    override val data: NavDestinationData,
) : Generator<NavDestinationData>() {

    internal fun generate(): TypeSpec {
        return TypeSpec.objectBuilder(componentProviderClassName)
            .addAnnotation(optInAnnotation())
            .addSuperinterface(componentProvider.parameterizedBy(data.navigation.route, retainedComponentClassName))
            .addFunction(provideFunction())
            .build()
    }

    private fun provideFunction(): FunSpec {
        return FunSpec.builder("provide")
            .addModifiers(OVERRIDE)
            .addAnnotation(optInAnnotation(internalNavigatorApi))
            .addParameter("route", data.navigation.route)
            .addParameter("executor", navigationExecutor)
            .addParameter("provider", activityComponentProvider)
            .returns(retainedComponentClassName)
            .beginControlFlow(
                "return %M(route, executor, provider, %T::class) " +
                    "{ parentComponent: %T, savedStateHandle, %L ->",
                data.navigation.parentComponentLookup,
                data.parentScope,
                retainedParentComponentClassName,
                data.navigation.route.propertyName,
            )
            .addStatement(
                "parentComponent.%L().%L(savedStateHandle, %L)",
                retainedParentComponentGetterName,
                retainedComponentFactoryCreateName,
                data.navigation.route.propertyName,
            )
            .endControlFlow()
            .build()
    }
}
