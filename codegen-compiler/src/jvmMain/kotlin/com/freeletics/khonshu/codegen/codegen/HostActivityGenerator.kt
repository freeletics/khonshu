package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.HostActivityData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.bundle
import com.freeletics.khonshu.codegen.util.compositionLocalProvider
import com.freeletics.khonshu.codegen.util.globalGraphProvider
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.localHostGraphProvider
import com.freeletics.khonshu.codegen.util.navHost
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.remember
import com.freeletics.khonshu.codegen.util.retain
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.setContent
import com.freeletics.khonshu.codegen.util.stackEntryStore
import com.freeletics.khonshu.codegen.util.stackEntryStoreHolder
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.LATEINIT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.activityName
    get() = "Khonshu${data.baseName}Activity"

internal class HostActivityGenerator(
    override val data: HostActivityData,
) : Generator<HostActivityData>() {
    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(activityName)
            .addAnnotation(optIn(InternalCodegenApi, internalNavigatorApi))
            .superclass(data.activityBaseClass)
            .addProperty(savedStateHandleProperty())
            .addFunction(onCreateFun())
            .addFunction(onSaveInstanceStateFun())
            .build()
    }

    private fun savedStateHandleProperty(): PropertySpec {
        return PropertySpec.builder("savedStateHandle", savedStateHandle)
            .addModifiers(PRIVATE, LATEINIT)
            .mutable(true)
            .build()
    }

    private fun onCreateFun(): FunSpec {
        return FunSpec.builder("onCreate")
            .addModifiers(OVERRIDE)
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .addStatement("super.onCreate(savedInstanceState)")
            .addStatement("savedStateHandle = %T.createHandle(savedInstanceState, null)", savedStateHandle)
            .beginControlFlow("%M", setContent)
            .beginControlFlow("val stackEntryStoreHolder = %M", retain)
            .addStatement("%T()", stackEntryStoreHolder)
            .endControlFlow()
            .beginControlFlow("val graphProvider = %M", remember)
            .addStatement(
                "%T(application as %T, stackEntryStoreHolder, savedStateHandle, intent)",
                graphProviderClassName,
                globalGraphProvider,
            )
            .endControlFlow()
            .beginControlFlow("val graph = %M(graphProvider)", remember)
            .addStatement("graphProvider.provide<%T>(%T::class)", graphClassName, data.scope)
            .endControlFlow()
            .beginControlFlow("%L(graph) { modifier, destinationChangedCallback ->", composableName)
            .beginControlFlow(
                "%M(%M provides graphProvider)",
                compositionLocalProvider,
                localHostGraphProvider,
            )
            .addStatement("%M(⇥", navHost)
            .addStatement("navigator = %M(graph) { graph.hostNavigator },", remember)
            .addStatement("modifier = modifier,")
            .addStatement("destinationChangedCallback = destinationChangedCallback,")
            .addStatement("⇤)")
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .build()
    }

    private fun onSaveInstanceStateFun(): FunSpec {
        return FunSpec.builder("onSaveInstanceState")
            .addModifiers(OVERRIDE)
            .addParameter("outState", bundle)
            .addStatement("val bundle = savedStateHandle.savedStateProvider().saveState()")
            .addStatement("outState.putAll(bundle)")
            .build()
    }
}
