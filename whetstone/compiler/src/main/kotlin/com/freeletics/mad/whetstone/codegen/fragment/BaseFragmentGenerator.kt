package com.freeletics.mad.whetstone.codegen.fragment

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.FragmentCommonData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.common.retainedComponentClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.requireArguments
import com.freeletics.mad.whetstone.codegen.util.fragmentNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.fragmentViewModel
import com.freeletics.mad.whetstone.codegen.util.lateinitPropertySpec
import com.freeletics.mad.whetstone.codegen.util.layoutInflater
import com.freeletics.mad.whetstone.codegen.util.navEventNavigator
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.rememberViewModel
import com.freeletics.mad.whetstone.codegen.util.view
import com.freeletics.mad.whetstone.codegen.util.viewGroup
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out CommonData>.fragmentName
    get() = "${data.baseName}Fragment"

internal abstract class BaseFragmentGenerator<T : FragmentCommonData> : Generator<T>() {

    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(fragmentName)
            .addAnnotation(optInAnnotation())
            .superclass(data.fragmentBaseClass)
            .addProperty(lateinitPropertySpec(retainedComponentClassName))
            .addFunction(onCreateViewFun())
            .build()
    }

    private fun onCreateViewFun(): FunSpec {
        val argumentsParameter = data.navigation.asParameter()
        return FunSpec.builder("onCreateView")
            .addModifiers(OVERRIDE)
            .addParameter("inflater", layoutInflater)
            .addParameter("container", viewGroup.copy(nullable = true))
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .returns(view)
            .beginControlFlow("if (!::%L.isInitialized)", retainedComponentClassName.propertyName)
            .addCode("val %N = ", argumentsParameter)
            .addCode(data.navigation.requireArguments())
            .addCode("\n")
            .also {
                if (data.navigation != null) {
                    it.addStatement("val viewModel = %M(%T::class, %T::class, %N, ::%T)",
                        fragmentViewModel, data.parentScope, data.navigation!!.destinationScope, argumentsParameter, viewModelClassName)
                } else {
                    it.addStatement("val viewModel = %M(%T::class, %N, ::%T)",
                        fragmentViewModel, data.parentScope, argumentsParameter, viewModelClassName)
                }
            }
            .addStatement("%L = viewModel.%L", retainedComponentClassName.propertyName, viewModelComponentName)
            .addCode(navigationCode())
            .endControlFlow()
            .addCode("\n")
            .addCode(createViewCode())
            .build()
    }

    protected abstract fun createViewCode(): CodeBlock

    private fun navigationCode(): CodeBlock {
        if (data.navigation == null) {
            return CodeBlock.of("")
        }

        return CodeBlock.builder()
            .add("\n")
            .addStatement("%M(this, %L.%L)", fragmentNavigationHandler, retainedComponentClassName.propertyName, navEventNavigator.propertyName)
            .build()
    }
}
