package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.bundle
import com.freeletics.khonshu.codegen.util.compositionLocalProvider
import com.freeletics.khonshu.codegen.util.experimentalNavHost
import com.freeletics.khonshu.codegen.util.localActivityComponentProvider
import com.freeletics.khonshu.codegen.util.optInAnnotation
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
            .addAnnotation(optInAnnotation())
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
            .beginControlFlow("val componentProvider = %M", remember)
            .addStatement("%T(this)", componentProviderClassName)
            .endControlFlow()
            .beginControlFlow("val component = %M(componentProvider)", remember)
            .addStatement("componentProvider.provide<%T>(%T::class)", retainedComponentClassName, data.scope)
            .endControlFlow()
            .beginControlFlow("%L(component) { startRoute, modifier, destinationChangedCallback ->", composableName)
            .beginControlFlow(
                "%M(%M provides componentProvider)",
                compositionLocalProvider,
                localActivityComponentProvider,
            )
            .addStatement("%M(", experimentalNavHost)
            .addStatement("  startRoute = startRoute,")
            .addStatement("  destinations = %M(component) { component.destinations },", remember)
            .addStatement("  modifier = modifier,")
            .addStatement("  deepLinkHandlers = %M(component) { component.deepLinkHandlers },", remember)
            .addStatement("  deepLinkPrefixes = %M(component) { component.deepLinkPrefixes },", remember)
            .addStatement("  navEventNavigator = %M(component) { component.navEventNavigator },", remember)
            .addStatement("  destinationChangedCallback = destinationChangedCallback,")
            .addStatement(")")
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .build()
    }
}
