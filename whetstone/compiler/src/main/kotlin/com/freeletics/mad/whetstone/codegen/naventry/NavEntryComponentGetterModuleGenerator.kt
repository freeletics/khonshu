package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.binds
import com.freeletics.mad.whetstone.codegen.bindsInstance
import com.freeletics.mad.whetstone.codegen.bundle
import com.freeletics.mad.whetstone.codegen.context
import com.freeletics.mad.whetstone.codegen.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.inject
import com.freeletics.mad.whetstone.codegen.internalApiAnnotation
import com.freeletics.mad.whetstone.codegen.intoMap
import com.freeletics.mad.whetstone.codegen.module
import com.freeletics.mad.whetstone.codegen.navBackStackEntry
import com.freeletics.mad.whetstone.codegen.navEntryComponentGetter
import com.freeletics.mad.whetstone.codegen.navEntryComponentGetterKey
import com.freeletics.mad.whetstone.codegen.navEntryIdScope
import com.freeletics.mad.whetstone.codegen.optInAnnotation
import com.freeletics.mad.whetstone.codegen.viewModelProvider
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.FINAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal class NavEntryComponentGetterModuleGenerator(
    override val data: NavEntryData,
) : NavEntryGenerator() {

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
