package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.context
import com.freeletics.mad.whetstone.codegen.util.inject
import com.freeletics.mad.whetstone.codegen.util.navBackStackEntry
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetter
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetterKey
import com.freeletics.mad.whetstone.codegen.util.navEntryIdScope
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.viewModelProvider
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<NavEntryData>.componentGetterClassName get() = ClassName("${data.baseName}ComponentGetter")

internal class NavEntryComponentGetterGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {


    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(componentGetterClassName)
            .addAnnotation(optInAnnotation())
// TODO use this and delete NavEntryComponentGetterModuleGenerator after
//  https://github.com/square/anvil/issues/477 is fixed
//            .addAnnotation(mapKeyAnnotation())
//            .addAnnotation(contributesMultibindingAnnotation())
            .addSuperinterface(navEntryComponentGetter)
            .primaryConstructor(ctor())
            .addProperty(idProperty())
            .addFunction(retrieveFunction())
            .build()
    }

//    private fun mapKeyAnnotation(): AnnotationSpec {
//        return AnnotationSpec.builder(navEntryComponentGetterKey)
//            .addMember("%T::class", data.scope)
//            .build()
//    }
//
//    private fun contributesMultibindingAnnotation(): AnnotationSpec {
//        return AnnotationSpec.builder(ContributesMultibinding::class)
//            .addMember("%T::class", data.parentScope)
//            .addMember("%T::class", navEntryComponentGetter)
//            .build()
//    }

    private fun ctor(): FunSpec {
        val scopeIdAnnoation = AnnotationSpec.builder(navEntryIdScope)
            .addMember("%T::class", data.scope)
            .build()
        val parameter = ParameterSpec.builder("id", INT)
            .addAnnotation(scopeIdAnnoation)
            .build()
        return FunSpec.constructorBuilder()
            .addAnnotation(inject)
            .addParameter(parameter)
            .build()
    }

    private fun idProperty(): PropertySpec {
        return PropertySpec.builder("id", INT, PRIVATE)
            .initializer("id")
            .build()
    }

    private fun retrieveFunction(): FunSpec {
        return FunSpec.builder("retrieve")
            .addModifiers(OVERRIDE)
            .addAnnotation(optInAnnotation())
            .addParameter("findEntry", LambdaTypeName.get(parameters = arrayOf(INT), returnType = navBackStackEntry))
            .addParameter("context", context)
            .returns(ANY)
            .addStatement("val entry = findEntry(id)")
            .beginControlFlow("val viewModelProvider = %M<%T>(entry, context, %T::class) { component, handle -> ", viewModelProvider, navEntrySubcomponentFactoryProviderClassName, data.parentScope)
            // arguments: external method
            .addStatement("val arguments = entry.arguments ?: %T.EMPTY", bundle)
            .addStatement("%T(component, handle, arguments)", viewModelClassName)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("return viewModel.%L", viewModelComponentName)
            .build()
    }
}
