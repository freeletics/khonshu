package com.freeletics.khonshu.codegen.codegen.compose

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.codegen.util.deepLinkHandler
import com.freeletics.khonshu.codegen.codegen.util.deepLinkPrefix
import com.freeletics.khonshu.codegen.codegen.util.module
import com.freeletics.khonshu.codegen.codegen.util.multibinds
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec

internal class ActivityModuleGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    private val moduleClassName = ClassName("Khonshu${data.baseName}ActivityModule")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.scope))
            .addFunction(bindDeepLinkHandlerFunction())
            .addFunction(bindDeepLinkPrefixFunction())
            .build()
    }

    private fun bindDeepLinkHandlerFunction(): FunSpec {
        return FunSpec.builder("bindDeepLinkHandler")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds)
            .returns(SET.parameterizedBy(deepLinkHandler))
            .build()
    }

    private fun bindDeepLinkPrefixFunction(): FunSpec {
        return FunSpec.builder("bindDeepLinkPrefix")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds)
            .returns(SET.parameterizedBy(deepLinkPrefix))
            .build()
    }
}
