package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.util.activityComponentProvider
import com.freeletics.khonshu.codegen.util.componentProvider
import com.freeletics.khonshu.codegen.util.getComponent
import com.freeletics.khonshu.codegen.util.getComponentFromRoute
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.stackEntry
import com.freeletics.khonshu.codegen.util.stackSnapshot
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
            .addParameter("entry", stackEntry.parameterizedBy(data.navigation.route))
            .addParameter("snapshot", stackSnapshot)
            .addParameter("provider", activityComponentProvider)
            .returns(retainedComponentClassName)
            .apply {
                if (data.navigation.parentScopeIsRoute) {
                    beginControlFlow(
                        "return %M(entry, snapshot, provider, %T::class) { parentComponent: %T ->",
                        getComponentFromRoute,
                        data.parentScope,
                        retainedParentComponentClassName,
                    )
                } else {
                    beginControlFlow(
                        "return %M(entry, provider, %T::class) { parentComponent: %T ->",
                        getComponent,
                        data.parentScope,
                        retainedParentComponentClassName,
                    )
                }
            }
            .addStatement(
                "parentComponent.%L().%L(entry.savedStateHandle, entry.route)",
                retainedParentComponentGetterName,
                retainedComponentFactoryCreateName,
            )
            .endControlFlow()
            .build()
    }
}
