package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.module
import com.freeletics.mad.whetstone.codegen.util.multibinds
import com.freeletics.mad.whetstone.codegen.util.navEntryAnnotation
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.Closeable

internal class NavEntrySubcomponentModuleGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {

    private val moduleClassName = ClassName("NavEntry${data.baseName}Module")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.scope))
            .addFunction(multibindsFunction())
            .build()
    }

    private fun multibindsFunction(): FunSpec {
        return FunSpec.builder("bindCancellable")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds)
            .addAnnotation(navEntryAnnotation(data.scope))
            .returns(SET.parameterizedBy(Closeable::class.asClassName()))
            .build()
    }
}
