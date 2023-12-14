package com.freeletics.khonshu.codegen.codegen.compose

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.common.componentProviderClassName
import com.freeletics.khonshu.codegen.codegen.common.composableName
import com.freeletics.khonshu.codegen.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.codegen.util.asParameter
import com.freeletics.khonshu.codegen.codegen.util.composable
import com.freeletics.khonshu.codegen.codegen.util.composeLocalNavigationExecutor
import com.freeletics.khonshu.codegen.codegen.util.composeNavigationHandler
import com.freeletics.khonshu.codegen.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.codegen.util.localContext
import com.freeletics.khonshu.codegen.codegen.util.navEventNavigator
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.codegen.util.propertyName
import com.freeletics.khonshu.codegen.codegen.util.remember
import com.squareup.kotlinpoet.FunSpec

/**
 * Generates the outer Composable for a NavDestination. This will obtain
 * the component, if needed call NavigationSetup and finally will call the
 * inner Composable.
 */
internal class OuterComposableGenerator(
    override val data: ComposeScreenData,
) : Generator<ComposeScreenData>() {

    internal fun generate(): FunSpec {
        val parameter = data.navigation.asParameter()
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation(InternalCodegenApi, internalNavigatorApi))
            .addParameter(parameter)
            .addStatement("val context = %M.current", localContext)
            .addStatement("val executor = %M.current", composeLocalNavigationExecutor)
            .beginControlFlow("val component = %M(context, executor, %N)", remember, parameter)
            .addStatement("%T.provide(%N, executor, context)", componentProviderClassName, parameter)
            .endControlFlow()
            .addStatement("")
            .addStatement("%M(component.%L)", composeNavigationHandler, navEventNavigator.propertyName)
            .addStatement("")
            .addStatement("%L(component)", composableName)
            .build()
    }
}
