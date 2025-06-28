package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.HostWindowData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.composable
import com.freeletics.khonshu.codegen.util.compositionLocalProvider
import com.freeletics.khonshu.codegen.util.globalGraphProvider
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.launchInfo
import com.freeletics.khonshu.codegen.util.localHostGraphProvider
import com.freeletics.khonshu.codegen.util.navHost
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.remember
import com.freeletics.khonshu.codegen.util.retain
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.window
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

internal val Generator<out BaseData>.windowName
    get() = "Khonshu${data.baseName}Window"

internal class HostWindowGenerator(
    override val data: HostWindowData,
) : Generator<HostWindowData>() {
    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(windowName)
            .addAnnotation(optIn(InternalCodegenApi, internalNavigatorApi))
            .primaryConstructor(constructor())
            .addProperty(globalGraphProviderProperty())
            .addProperty(launchInfoProperty())
            .addFunction(showFun())
            .build()
    }

    private fun globalGraphProviderProperty(): PropertySpec {
        return PropertySpec.builder("globalGraphProvider", globalGraphProvider)
            .addModifiers(PRIVATE)
            .initializer("globalGraphProvider")
            .build()
    }

    private fun launchInfoProperty(): PropertySpec {
        return PropertySpec.builder("launchInfo", launchInfo)
            .addModifiers(PRIVATE)
            .initializer("launchInfo")
            .build()
    }

    private fun constructor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("globalGraphProvider", globalGraphProvider)
            .addParameter("launchInfo", launchInfo)
            .build()
    }

    private fun showFun(): FunSpec {
        return FunSpec.builder("Show")
            .addAnnotation(composable)
            // TODO: other Window parameters
            .addParameter("onCloseRequest", LambdaTypeName.get(returnType = UNIT))
            .beginControlFlow("%M(onCloseRequest = onCloseRequest)", window)
            .beginControlFlow("val graph = %M", retain)
            .addStatement(
                "val parentGraph = globalGraphProvider.getGraph<%T>(%T::class)",
                graphFactoryClassName,
                data.parentScope,
            )
            .addStatement("val savedStateHandle = %T.createHandle(null, null)", savedStateHandle)
            .addStatement("parentGraph.%L(savedStateHandle, launchInfo)", graphFactoryCreateFunctionName)
            .endControlFlow()
            .beginControlFlow("val graphProvider = %M(graph)", remember)
            .addStatement("%T(graph, globalGraphProvider)", graphProviderClassName)
            .endControlFlow()
            .beginControlFlow("%L(graph) { modifier, destinationChangedCallback ->", composableName)
            .beginControlFlow(
                "%M(%M provides graphProvider)",
                compositionLocalProvider,
                localHostGraphProvider,
            )
            .addStatement("%M(", navHost)
            .addStatement("  navigator = %M(graph) { graph.hostNavigator },", remember)
            .addStatement("  modifier = modifier,")
            .addStatement("  destinationChangedCallback = destinationChangedCallback,")
            .addStatement(")")
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .build()
    }
}
