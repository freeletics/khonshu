package com.freeletics.mad.whetstone.codegen.nav

import com.freeletics.mad.whetstone.BaseData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.common.composableName
import com.freeletics.mad.whetstone.codegen.fragment.fragmentName
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.intoSet
import com.freeletics.mad.whetstone.codegen.util.module
import com.freeletics.mad.whetstone.codegen.util.provides
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

internal class NavDestinationModuleGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    private val moduleClassName = ClassName("Whetstone${data.baseName}NavDestinationModule")

    internal fun generate(): TypeSpec {
        return TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.navigation!!.destinationScope))
            .addFunction(providesFunction())
            .build()
    }

    private fun providesFunction(): FunSpec {
        return FunSpec.builder("provideNavDestination")
            .addAnnotation(provides)
            .addAnnotation(intoSet)
            .returns(data.navigation!!.destinationClass)
            .addCode(providesCode())
            .build()
    }

    private fun providesCode(): CodeBlock {
        val navigation = data.navigation!!
        return when (data.navigation!!) {
            is Navigation.Compose -> {
                CodeBlock.builder()
                    .beginControlFlow(
                        "return %M<%T>",
                        navigation.destinationMethod,
                        navigation.route,
                    )
                    .addStatement("%L(it)", composableName)
                    .endControlFlow()
                    .build()
            }
            is Navigation.Fragment -> {
                CodeBlock.of(
                    "return %M<%T, %T>()",
                    navigation.destinationMethod,
                    navigation.route,
                    ClassName(fragmentName),
                )
            }
        }
    }
}
