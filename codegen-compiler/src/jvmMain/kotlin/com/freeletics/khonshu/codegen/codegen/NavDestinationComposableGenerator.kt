package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.activityNavigatorEffect
import com.freeletics.khonshu.codegen.util.composable
import com.freeletics.khonshu.codegen.util.destinationNavigator
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.localActivityGraphProvider
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.propertyName
import com.freeletics.khonshu.codegen.util.remember
import com.freeletics.khonshu.codegen.util.stackEntry
import com.freeletics.khonshu.codegen.util.stackSnapshot
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Generates the outer Composable for a NavDestination. This will obtain
 * the graph, if needed call NavigationSetup and finally will call the
 * inner Composable.
 */
internal class NavDestinationComposableGenerator(
    override val data: NavDestinationData,
) : Generator<NavDestinationData>() {
    internal fun generate(): FunSpec {
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optIn(InternalCodegenApi, internalNavigatorApi))
            .addParameter("snapshot", stackSnapshot)
            .addParameter("entry", stackEntry.parameterizedBy(data.navigation.route))
            .addStatement("val provider = %M.current", localActivityGraphProvider)
            .beginControlFlow("val graph = %M(entry, snapshot, provider)", remember)
            .addStatement("%T.provide(entry, snapshot, provider)", graphProviderClassName)
            .endControlFlow()
            .addStatement("")
            .addStatement("val navigator = graph.%L", destinationNavigator.propertyName)
            .beginControlFlow("if (navigator != null)")
            .addStatement("%M(navigator)", activityNavigatorEffect)
            .endControlFlow()
            .addStatement("")
            .addStatement("%L(graph)", composableName)
            .build()
    }
}
