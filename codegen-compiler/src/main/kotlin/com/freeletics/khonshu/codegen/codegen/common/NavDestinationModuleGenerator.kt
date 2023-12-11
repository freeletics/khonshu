package com.freeletics.khonshu.codegen.codegen.common

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.codegen.util.intoSet
import com.freeletics.khonshu.codegen.codegen.util.module
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.codegen.util.provides
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

internal class NavDestinationModuleGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    private val moduleClassName = ClassName("Khonshu${data.baseName}NavDestinationModule")

    internal fun generate(): TypeSpec {
        return TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(optInAnnotation())
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.navigation!!.destinationScope))
            .addFunction(providesDestination())
            .build()
    }

    private fun providesDestination(): FunSpec {
        return FunSpec.builder("provideNavDestination")
            .addAnnotation(provides)
            .addAnnotation(intoSet)
            .addAnnotation(optInAnnotation(internalNavigatorApi))
            .returns(data.navigation!!.destinationClass)
            .addCode(providesDestinationCode())
            .build()
    }

    private fun providesDestinationCode(): CodeBlock {
        val navigation = data.navigation!!
        return when (data.navigation!!) {
            is Navigation.Compose -> {
                CodeBlock.builder()
                    .beginControlFlow(
                        "return %M<%T>(%T)",
                        navigation.destinationMethod,
                        navigation.route,
                        componentProviderClassName,
                    )
                    .addStatement("%L(it)", composableName)
                    .endControlFlow()
                    .build()
            }
        }
    }
}
