package com.freeletics.mad.whetstone.codegen.nav

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.util.context
import com.freeletics.mad.whetstone.codegen.util.destinationId
import com.freeletics.mad.whetstone.codegen.util.inject
import com.freeletics.mad.whetstone.codegen.util.internalNavigatorApi
import com.freeletics.mad.whetstone.codegen.util.internalWhetstoneApi
import com.freeletics.mad.whetstone.codegen.util.navBackStackEntry
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetter
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentGetterKey
import com.freeletics.mad.whetstone.codegen.util.navEntryViewModel
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.toRoute
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.LambdaTypeName
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
            .addMember("%T::class", data.navigation.destinationScope)
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
            .addStatement("val entry = findEntry(%T::class.%M())", data.navigation.route, destinationId)
            .addStatement("val route: %T = entry.arguments!!.%M()", data.navigation.route, toRoute)
            .addStatement("val viewModel = %M(entry, context, %T::class, %T::class, route, findEntry, ::%T)",
                navEntryViewModel, data.parentScope, data.navigation.destinationScope, viewModelClassName)
            .addStatement("return viewModel.%L", viewModelComponentName)
            .build()
    }
}
