package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.context
import com.freeletics.khonshu.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.util.createHostNavigator
import com.freeletics.khonshu.codegen.util.hostNavigator
import com.freeletics.khonshu.codegen.util.immutableSet
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.jvmSuppressWildcards
import com.freeletics.khonshu.codegen.util.module
import com.freeletics.khonshu.codegen.util.multiStackHostNavigatorViewModel
import com.freeletics.khonshu.codegen.util.navRoot
import com.freeletics.khonshu.codegen.util.navigationDestination
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.provides
import com.freeletics.khonshu.codegen.util.scopeToAnnotation
import com.freeletics.khonshu.codegen.util.toImmutableSet
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec

internal class ActivityModuleGenerator(
    override val data: NavHostActivityData,
) : Generator<BaseData>() {

    private val moduleClassName = ClassName("Khonshu${data.baseName}ActivityModule")

    internal fun generate(): TypeSpec {
        return TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.scope))
            .addFunction(provideImmutableNavDestinationsFunction())
            .addFunction(provideHostNavigator())
            .build()
    }

    private fun provideImmutableNavDestinationsFunction(): FunSpec {
        return FunSpec.builder("provideImmutableNavDestinations")
            .addAnnotation(provides)
            .addParameter("destinations", SET.parameterizedBy(navigationDestination).jvmSuppressWildcards())
            .returns(immutableSet.parameterizedBy(navigationDestination))
            .addStatement("return destinations.%M()", toImmutableSet)
            .build()
    }

    private fun provideHostNavigator(): FunSpec {
        return FunSpec.builder("provideHostNavigator")
            .addAnnotation(provides)
            .addAnnotation(scopeToAnnotation(data.scope))
            .addAnnotation(optInAnnotation(internalNavigatorApi))
            .addParameter("context", context)
            .addParameter("viewModel", multiStackHostNavigatorViewModel)
            .addParameter("startRoot", navRoot)
            .addParameter("destinations", immutableSet.parameterizedBy(navigationDestination).jvmSuppressWildcards())
            .returns(hostNavigator)
            .addStatement(
                "return %M(context, viewModel, startRoot, destinations)",
                createHostNavigator,
            )
            .build()
    }
}
