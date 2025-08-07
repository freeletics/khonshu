package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.util.contributesTo
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.intoSet
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.provides
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

internal class NavDestinationGraphGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {
    private val moduleClassName = ClassName("Khonshu${data.baseName}NavDestinationGraph")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(optIn())
            .addAnnotation(contributesTo(data.navigation!!.destinationScope))
            .addFunction(providesDestination())
            .build()
    }

    private fun providesDestination(): FunSpec {
        return FunSpec.builder("provide${data.baseName}NavDestination")
            .addAnnotation(provides())
            .addAnnotation(intoSet())
            .addAnnotation(optIn(internalNavigatorApi))
            .returns(data.navigation!!.destinationClass)
            .addCode(providesDestinationCode())
            .build()
    }

    private fun providesDestinationCode(): CodeBlock {
        val navigation = data.navigation!!
        return CodeBlock.builder()
            .beginControlFlow(
                "return %M<%T%L>(%T) { snapshot, route ->",
                navigation.destinationMethod,
                navigation.route,
                if (navigation.parentScopeIsRoute) {
                    CodeBlock.builder().add(", %T", data.parentScope).build()
                } else {
                    ""
                },
                graphProviderClassName,
            )
            .addStatement("%L(snapshot, route)", composableName)
            .endControlFlow()
            .build()
    }
}
