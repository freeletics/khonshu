package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.util.activityGraphProvider
import com.freeletics.khonshu.codegen.util.getGraph
import com.freeletics.khonshu.codegen.util.getGraphFromRoute
import com.freeletics.khonshu.codegen.util.graphProvider
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.stackEntry
import com.freeletics.khonshu.codegen.util.stackSnapshot
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.graphProviderClassName
    get() = ClassName("Khonshu${data.baseName}GraphProvider")

internal class NavDestinationGraphProviderGenerator(
    override val data: NavDestinationData,
) : Generator<NavDestinationData>() {
    internal fun generate(): TypeSpec {
        return TypeSpec.objectBuilder(graphProviderClassName)
            .addAnnotation(optIn())
            .addSuperinterface(graphProvider.parameterizedBy(data.navigation.route, graphClassName))
            .addFunction(provideFunction())
            .build()
    }

    private fun provideFunction(): FunSpec {
        return FunSpec.builder("provide")
            .addModifiers(OVERRIDE)
            .addAnnotation(optIn(internalNavigatorApi))
            .addParameter("entry", stackEntry.parameterizedBy(data.navigation.route))
            .addParameter("snapshot", stackSnapshot)
            .addParameter("provider", activityGraphProvider)
            .returns(graphClassName)
            .apply {
                if (data.navigation.parentScopeIsRoute) {
                    beginControlFlow(
                        "return %M(entry, snapshot, provider, %T::class) { factory: %T ->",
                        getGraphFromRoute,
                        data.parentScope,
                        graphFactoryClassName,
                    )
                } else {
                    beginControlFlow(
                        "return %M(entry, provider, %T::class) { factory: %T ->",
                        getGraph,
                        data.parentScope,
                        graphFactoryClassName,
                    )
                }
            }
            .addStatement(
                "factory.%L(entry.savedStateHandle, entry.route)",
                graphFactoryCreateFunctionName,
            )
            .endControlFlow()
            .build()
    }
}
