package com.freeletics.khonshu.codegen.codegen.compose

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.common.composableName
import com.freeletics.khonshu.codegen.codegen.common.retainedComponentClassName
import com.freeletics.khonshu.codegen.codegen.common.retainedComponentFactoryCreateName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentClassName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentGetterName
import com.freeletics.khonshu.codegen.codegen.util.bundle
import com.freeletics.khonshu.codegen.codegen.util.getComponent
import com.freeletics.khonshu.codegen.codegen.util.lateinitPropertySpec
import com.freeletics.khonshu.codegen.codegen.util.navHost
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.codegen.util.propertyName
import com.freeletics.khonshu.codegen.codegen.util.setContent
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
            .addProperty(lateinitPropertySpec(retainedComponentClassName))
            .addFunction(onCreateFun())
            .build()
    }

    private fun onCreateFun(): FunSpec {
        return FunSpec.builder("onCreate")
            .addModifiers(OVERRIDE)
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .addStatement("super.onCreate(savedInstanceState)")
            .beginControlFlow("if (!::%L.isInitialized)", retainedComponentClassName.propertyName)
            .beginControlFlow(
                "%L = %M(this, this, %T::class, intent.extras) { " +
                    "parentComponent: %T, savedStateHandle, extras ->",
                retainedComponentClassName.propertyName,
                getComponent,
                data.parentScope,
                retainedParentComponentClassName,
            )
            .addStatement(
                "parentComponent.%L().%L(savedStateHandle, extras)",
                retainedParentComponentGetterName,
                retainedComponentFactoryCreateName,
            )
            .endControlFlow()
            .endControlFlow()
            .addStatement("")
            .beginControlFlow("%M", setContent)
            .beginControlFlow(
                "%L(%L) { startRoute, destinationChangedCallback ->",
                composableName,
                retainedComponentClassName.propertyName,
            )
            .addStatement("%M(", navHost)
            .addStatement("  startRoute = startRoute,")
            .addStatement("  destinations = %L.destinations,", retainedComponentClassName.propertyName)
            .addStatement("  deepLinkHandlers = %L.deepLinkHandlers,", retainedComponentClassName.propertyName)
            .addStatement("  deepLinkPrefixes = %L.deepLinkPrefixes,", retainedComponentClassName.propertyName)
            .addStatement("  destinationChangedCallback = destinationChangedCallback,")
            .addStatement(")")
            .endControlFlow()
            .endControlFlow()
            .build()
    }
}
