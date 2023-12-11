package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.asParameter
import com.freeletics.khonshu.codegen.util.composable
import com.freeletics.khonshu.codegen.util.composeLocalNavigationExecutor
import com.freeletics.khonshu.codegen.util.composeNavigationHandler
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.localContext
import com.freeletics.khonshu.codegen.util.navEventNavigator
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.propertyName
import com.freeletics.khonshu.codegen.util.remember
import com.squareup.kotlinpoet.FunSpec

/**
 * Generates the outer Composable for a NavDestination. This will obtain
 * the component, if needed call NavigationSetup and finally will call the
 * inner Composable.
 */
internal class NavDestinationComposableGenerator(
    override val data: NavDestinationData,
) : Generator<NavDestinationData>() {

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
