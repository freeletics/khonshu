package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.asParameter
import com.freeletics.khonshu.codegen.util.composable
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.localActivityComponentProvider
import com.freeletics.khonshu.codegen.util.localNavigationExecutor
import com.freeletics.khonshu.codegen.util.navEventNavigator
import com.freeletics.khonshu.codegen.util.navigationSetup
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
            .addStatement("val executor = %M.current", localNavigationExecutor)
            .addStatement("val provider = %M.current", localActivityComponentProvider)
            .beginControlFlow("val component = %M(%N, executor, provider)", remember, parameter)
            .addStatement("%T.provide(%N, executor, provider)", componentProviderClassName, parameter)
            .endControlFlow()
            .addStatement("")
            .addStatement("%M(component.%L)", navigationSetup, navEventNavigator.propertyName)
            .addStatement("")
            .addStatement("%L(component)", composableName)
            .build()
    }
}
