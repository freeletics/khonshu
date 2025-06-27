package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.bundle
import com.freeletics.khonshu.codegen.util.compositionLocalProvider
import com.freeletics.khonshu.codegen.util.localActivityGraphProvider
import com.freeletics.khonshu.codegen.util.navHost
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.remember
import com.freeletics.khonshu.codegen.util.setContent
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.activityName
    get() = "Khonshu${data.baseName}Activity"

internal class ActivityGenerator(
    override val data: NavHostActivityData,
) : Generator<NavHostActivityData>() {
    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(activityName)
            .addAnnotation(optIn())
            .superclass(data.activityBaseClass)
            .addFunction(onCreateFun())
            .build()
    }

    private fun onCreateFun(): FunSpec {
        return FunSpec.builder("onCreate")
            .addModifiers(OVERRIDE)
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .addStatement("super.onCreate(savedInstanceState)")
            .beginControlFlow("%M", setContent)
            .beginControlFlow("val graphProvider = %M", remember)
            .addStatement("%T(this)", graphProviderClassName)
            .endControlFlow()
            .beginControlFlow("val graph = %M(graphProvider)", remember)
            .addStatement("graphProvider.provide<%T>(%T::class)", graphClassName, data.scope)
            .endControlFlow()
            .beginControlFlow("%L(graph) { modifier, destinationChangedCallback ->", composableName)
            .beginControlFlow(
                "%M(%M provides graphProvider)",
                compositionLocalProvider,
                localActivityGraphProvider,
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
