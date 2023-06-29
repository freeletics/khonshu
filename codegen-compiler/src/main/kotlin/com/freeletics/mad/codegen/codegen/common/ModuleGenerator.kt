package com.freeletics.mad.codegen.codegen.common

import com.freeletics.mad.codegen.BaseData
import com.freeletics.mad.codegen.NavEntryData
import com.freeletics.mad.codegen.codegen.Generator
import com.freeletics.mad.codegen.codegen.util.contributesToAnnotation
import com.freeletics.mad.codegen.codegen.util.module
import com.freeletics.mad.codegen.codegen.util.multibinds
import com.freeletics.mad.codegen.codegen.util.navEntryAnnotation
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.Closeable

internal class ModuleGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    private val moduleClassName = ClassName("Mad${data.baseName}Module")

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
            .apply {
                if (data is NavEntryData) {
                    addAnnotation(navEntryAnnotation(data.scope))
                }
            }
            .returns(SET.parameterizedBy(Closeable::class.asClassName()))
            .build()
    }
}
