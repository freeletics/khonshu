package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.util.forScope
import com.freeletics.khonshu.codegen.util.module
import com.freeletics.khonshu.codegen.util.multibinds
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.Closeable

internal class ComponentModuleGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    private val moduleClassName = ClassName("Khonshu${data.baseName}Module")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.scope))
            .addFunction(multibindsFunction())
            .build()
    }

    private fun multibindsFunction(): FunSpec {
        return FunSpec.builder("bindCloseables")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds)
            .addAnnotation(forScope(data.scope))
            .returns(SET.parameterizedBy(Closeable::class.asClassName()))
            .build()
    }
}
