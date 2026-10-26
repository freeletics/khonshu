package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.DestinationData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.composable
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.localHostGraphProvider
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.platformNavigator
import com.freeletics.khonshu.codegen.util.platformNavigatorEffect
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
internal class DestinationComposableGenerator(
    override val data: DestinationData,
) : Generator<DestinationData>() {
    internal fun generate(): FunSpec {
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optIn(InternalCodegenApi, internalNavigatorApi))
            .addParameter("snapshot", stackSnapshot)
            .addParameter("entry", stackEntry.parameterizedBy(data.navigation.route))
            .addStatement("val provider = %M.current", localHostGraphProvider)
            .beginControlFlow("val graph = %M(entry, snapshot, provider)", remember)
            .addStatement("%T.provide(entry, snapshot, provider)", graphProviderClassName)
            .endControlFlow()
            .addStatement("")
            // the content composable is called before the platform navigator effect so that
            // navigators that are created while it composes (e.g. because they are injected into a
            // state machine) can register for activity results before the first read
            .addStatement("%L(graph)", composableName)
            .addStatement("")
            .addStatement("%M(graph.%L)", platformNavigatorEffect, platformNavigator.propertyName)
            .build()
    }
}
