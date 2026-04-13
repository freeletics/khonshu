package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.HostViewControllerData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.composeUiViewController
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
import com.freeletics.khonshu.codegen.util.uiViewController
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.viewControllerName
    get() = "Khonshu${data.baseName}ViewController"

internal class HostViewControllerGenerator(
    override val data: HostViewControllerData,
) : Generator<HostViewControllerData>() {
    internal fun generate(): TypeSpec {
        return TypeSpec.Companion.classBuilder(viewControllerName)
            .addAnnotation(optIn(InternalCodegenApi, internalNavigatorApi))
            .primaryConstructor(constructor())
            .addProperty(globalGraphProviderProperty())
            .addProperty(launchInfoProperty())
            .addFunction(viewControllerFun())
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

    private fun viewControllerFun(): FunSpec {
        return FunSpec.builder("viewController")
            .returns(uiViewController)
            // TODO: ComposeUIViewControllerConfiguration parameter
            .beginControlFlow("return %M", composeUiViewController)
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
