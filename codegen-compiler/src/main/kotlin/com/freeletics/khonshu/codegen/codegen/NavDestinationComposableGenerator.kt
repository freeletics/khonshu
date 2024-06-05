package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.activityResultNavigator
import com.freeletics.khonshu.codegen.util.composable
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.localActivityComponentProvider
import com.freeletics.khonshu.codegen.util.navigationSetup
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.propertyName
import com.freeletics.khonshu.codegen.util.remember
import com.freeletics.khonshu.codegen.util.stackEntry
import com.freeletics.khonshu.codegen.util.stackSnapshot
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Generates the outer Composable for a NavDestination. This will obtain
 * the component, if needed call NavigationSetup and finally will call the
 * inner Composable.
 */
internal class NavDestinationComposableGenerator(
    override val data: NavDestinationData,
) : Generator<NavDestinationData>() {

    internal fun generate(): FunSpec {
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation(InternalCodegenApi, internalNavigatorApi))
            .addParameter("snapshot", stackSnapshot)
            .addParameter("entry", stackEntry.parameterizedBy(data.navigation.route))
            .addStatement("val provider = %M.current", localActivityComponentProvider)
            .beginControlFlow("val component = %M(entry, snapshot, provider)", remember)
            .addStatement("%T.provide(entry, snapshot, provider)", componentProviderClassName)
            .endControlFlow()
            .addStatement("")
            .addStatement("%M(component.%L)", navigationSetup, activityResultNavigator.propertyName)
            .addStatement("")
            .addStatement("%L(component)", composableName)
            .build()
    }
}
