package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.HostActivityData
import com.freeletics.khonshu.codegen.util.asLaunchInfo
import com.freeletics.khonshu.codegen.util.contributesTo
import com.freeletics.khonshu.codegen.util.createHostNavigator
import com.freeletics.khonshu.codegen.util.forScope
import com.freeletics.khonshu.codegen.util.hostNavigator
import com.freeletics.khonshu.codegen.util.intent
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.launchInfo
import com.freeletics.khonshu.codegen.util.navRoot
import com.freeletics.khonshu.codegen.util.navigationDestination
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.provides
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.singleIn
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec

internal class HostGraphContributionGenerator(
    override val data: HostActivityData,
) : Generator<BaseData>() {
    private val moduleClassName = ClassName("Khonshu${data.baseName}ActivityGraph")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(contributesTo(data.scope))
            .addFunction(provideLaunchInfo())
            .addFunction(provideHostNavigator())
            .build()
    }

    private fun provideLaunchInfo(): FunSpec {
        return FunSpec.builder("provideLaunchInfo")
            .addAnnotation(provides())
            .addParameter("intent", intent)
            .addParameter("destinations", SET.parameterizedBy(navigationDestination))
            .returns(launchInfo)
            .addStatement("return intent.%M(destinations)", asLaunchInfo)
            .build()
    }

    private fun provideHostNavigator(): FunSpec {
        return FunSpec.builder("provideHostNavigator")
            .addAnnotation(provides())
            .addAnnotation(singleIn(data.scope))
            .addAnnotation(optIn(internalNavigatorApi))
            .addParameter("startRoot", navRoot)
            .addParameter(
                ParameterSpec.builder("savedStateHandle", savedStateHandle).addAnnotation(forScope(data.scope)).build(),
            )
            .addParameter("destinations", SET.parameterizedBy(navigationDestination))
            .returns(hostNavigator)
            .addStatement("return %M(startRoot, destinations, savedStateHandle)", createHostNavigator)
            .build()
    }
}
