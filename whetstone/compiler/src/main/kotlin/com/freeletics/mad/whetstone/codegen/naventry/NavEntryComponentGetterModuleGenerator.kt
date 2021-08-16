package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.binds
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.intoMap
import com.freeletics.mad.whetstone.codegen.util.module
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetter
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetterKey
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec

internal class NavEntryComponentGetterModuleGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {

    private val moduleClassName = ClassName("Whetstone${data.baseName}Module")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.parentScope))
            .addFunction(retrieveFunction())
            .build()
    }

    private fun mapKeyAnnotation(): AnnotationSpec {
        return AnnotationSpec.builder(navEntryComponentGetterKey)
            .addMember("%S", data.scope)
            .build()
    }

    private fun retrieveFunction(): FunSpec {
        return FunSpec.builder("bindComponentGetter")
            .addModifiers(ABSTRACT)
            .addAnnotation(binds)
            .addAnnotation(intoMap)
            .addAnnotation(mapKeyAnnotation())
            .addParameter("getter", componentGetterClassName)
            .returns(navEntryComponentGetter)
            .build()
    }
}
