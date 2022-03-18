package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.context
import com.freeletics.mad.whetstone.codegen.util.destinationId
import com.freeletics.mad.whetstone.codegen.util.inject
import com.freeletics.mad.whetstone.codegen.util.internalNavigatorApi
import com.freeletics.mad.whetstone.codegen.util.internalWhetstoneApi
import com.freeletics.mad.whetstone.codegen.util.navBackStackEntry
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetter
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetterKey
import com.freeletics.mad.whetstone.codegen.util.navEntryIdScope
import com.freeletics.mad.whetstone.codegen.util.navEntryViewModelProvider
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.toRoute
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

internal val Generator<NavEntryData>.componentGetterClassName
    get() = ClassName("${data.baseName}ComponentGetter")

internal class NavEntryComponentGetterGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {


    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(componentGetterClassName)
            .addAnnotation(optInAnnotation())
            .addAnnotation(mapKeyAnnotation())
            .addAnnotation(contributesMultibindingAnnotation())
            .addSuperinterface(navEntryComponentGetter)
            .primaryConstructor(ctor())
            .addFunction(retrieveFunction())
            .build()
    }

    private fun mapKeyAnnotation(): AnnotationSpec {
        return AnnotationSpec.builder(navEntryComponentGetterKey)
            .addMember("%T::class", data.scope)
            .build()
    }

    private fun contributesMultibindingAnnotation(): AnnotationSpec {
        return AnnotationSpec.builder(ContributesMultibinding::class)
            .addMember("%T::class", data.destinationScope)
            .addMember("%T::class", navEntryComponentGetter)
            .build()
    }

    private fun ctor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addAnnotation(inject)
            .build()
    }

    private fun retrieveFunction(): FunSpec {
        return FunSpec.builder("retrieve")
            .addModifiers(OVERRIDE)
            .addAnnotation(optInAnnotation(internalWhetstoneApi, internalNavigatorApi))
            .addParameter("findEntry", LambdaTypeName.get(parameters = arrayOf(INT), returnType = navBackStackEntry))
            .addParameter("context", context)
            .returns(ANY)
            .addStatement("val entry = findEntry(%T::class.%M())", data.route, destinationId)
            .beginControlFlow("val viewModelProvider = %M<%T>(entry, context, %T::class) { parentComponent, handle -> ",
                navEntryViewModelProvider, navEntryParentComponentClassName, data.parentScope)
            // arguments: external method
            .addStatement("val route: %T = entry.arguments!!.%M()", data.route, toRoute)
            .addStatement("%T(parentComponent.%L(), handle, route)", viewModelClassName, navEntryParentComponentGetterName)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("return viewModel.%L", viewModelComponentName)
            .build()
    }
}
