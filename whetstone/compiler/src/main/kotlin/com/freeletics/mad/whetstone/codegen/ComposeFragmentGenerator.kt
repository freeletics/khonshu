package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Data
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.TypeSpec

internal class ComposeFragmentGenerator(
    override val data: Data,
) : Generator() {

    private val composeFragmentClassName = ClassName("${data.baseName}Fragment")

    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(composeFragmentClassName)
            .superclass(fragment)
            .addFunction(composeOnCreateViewFun())
            .build()
    }

    private fun composeOnCreateViewFun(): FunSpec {
        return FunSpec.builder("onCreateView")
            .addModifiers(OVERRIDE)
            .addParameter("inflater", layoutInflater)
            .addParameter("container", viewGroup.copy(nullable = true))
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .returns(view)
            .addStatement("val navController = %M()", findNavController)
            // requireContext: external method
            .addStatement("val composeView = %T(requireContext())", composeView)
            // setContent: external method
            .beginControlFlow("composeView.setContent {")
            .apply {
                if (data.navigation != null) {
                    addStatement("%L(navController, this)", composableName)
                } else {
                    addStatement("%L(navController)", composableName)
                }
            }
            .endControlFlow()
            .addStatement("return composeView")
            .build()
    }
}
