package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.util.componentProvider
import com.freeletics.khonshu.codegen.util.context
import com.freeletics.khonshu.codegen.util.destinationId
import com.freeletics.khonshu.codegen.util.getComponent
import com.freeletics.khonshu.codegen.util.getComponentFromRoute
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
    override val data: BaseData,
) : Generator<BaseData>() {

    internal fun generate(): TypeSpec {
        return TypeSpec.objectBuilder(componentProviderClassName)
            .addAnnotation(optInAnnotation())
            .addSuperinterface(
                componentProvider
                    .parameterizedBy(data.navigation!!.route, retainedComponentClassName),
            )
            .addFunction(provideFunction())
            .build()
    }

    private fun provideFunction(): FunSpec {
        return FunSpec.builder("provide")
            .addModifiers(OVERRIDE)
            .addAnnotation(optInAnnotation(internalNavigatorApi))
            .addParameter("route", data.navigation!!.route)
            .addParameter("executor", navigationExecutor)
            .addParameter("context", context)
            .returns(retainedComponentClassName)
            .apply {
                if (data.navigation?.parentScopeIsRoute == true) {
                    beginControlFlow(
                        "return %M(route.%M, route, executor, context, %T::class) " +
                            "{ parentComponent: %T, savedStateHandle, %L ->",
                        getComponentFromRoute,
                        destinationId,
                        data.parentScope,
                        retainedParentComponentClassName,
                        data.navigation!!.route.propertyName,
                    )
                } else {
                    beginControlFlow(
                        "return %M(route.%M, route, executor, context, %T::class) " +
                            "{ parentComponent: %T, savedStateHandle, %L ->",
                        getComponent,
                        destinationId,
                        data.parentScope,
                        retainedParentComponentClassName,
                        data.navigation!!.route.propertyName,
                    )
                }
            }
            .addStatement(
                "parentComponent.%L().%L(savedStateHandle, %L)",
                retainedParentComponentGetterName,
                retainedComponentFactoryCreateName,
                data.navigation!!.route.propertyName,
            )
            .endControlFlow()
            .build()
    }
}
