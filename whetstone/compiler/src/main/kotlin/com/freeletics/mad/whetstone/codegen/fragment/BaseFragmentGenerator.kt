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
import com.freeletics.mad.whetstone.codegen.util.fragmentViewModelProvider
import com.freeletics.mad.whetstone.codegen.util.lateinitPropertySpec
import com.freeletics.mad.whetstone.codegen.util.layoutInflater
import com.freeletics.mad.whetstone.codegen.util.navEventNavigator
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.view
import com.freeletics.mad.whetstone.codegen.util.viewGroup
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
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
            .addFunction(injectFun())
            .build()
    }

    private fun onCreateViewFun(): FunSpec {
        return FunSpec.builder("onCreateView")
            .addModifiers(OVERRIDE)
            .addParameter("inflater", layoutInflater)
            .addParameter("container", viewGroup.copy(nullable = true))
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .returns(view)
            .beginControlFlow("if (!::%L.isInitialized)", retainedComponentClassName.propertyName)
            .addStatement("%L()", fragmentInjectName)
            .addCode(navigationCode())
            .endControlFlow()
            .addCode("\n")
            .addCode(createViewCode())
            .build()
    }

    protected abstract fun createViewCode(): CodeBlock

    private val fragmentInjectName = "inject"

    private fun injectFun(): FunSpec {
        val argumentsParameter = data.navigation.asParameter()
        return FunSpec.builder(fragmentInjectName)
            .addModifiers(PRIVATE)
            .addCode("val %N = ", argumentsParameter)
            .addCode(data.navigation.requireArguments())
            .addCode("\n")
            .beginControlFlow("val viewModelProvider = %M<%T>(this, %T::class) { dependencies, handle -> ",
                fragmentViewModelProvider, data.dependencies, data.parentScope)
            .addStatement("%T(dependencies, handle, %N)", viewModelClassName, argumentsParameter)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("%L = viewModel.%L", retainedComponentClassName.propertyName, viewModelComponentName)
            .build()
    }

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
