package com.freeletics.khonshu.codegen.codegen.fragment

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.FragmentData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.common.componentProviderClassName
import com.freeletics.khonshu.codegen.codegen.common.retainedComponentClassName
import com.freeletics.khonshu.codegen.codegen.common.retainedComponentFactoryCreateName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentClassName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentGetterName
import com.freeletics.khonshu.codegen.codegen.util.asParameter
import com.freeletics.khonshu.codegen.codegen.util.bundle
import com.freeletics.khonshu.codegen.codegen.util.fragmentFindNavigationExecutor
import com.freeletics.khonshu.codegen.codegen.util.fragmentNavigationHandler
import com.freeletics.khonshu.codegen.codegen.util.getComponent
import com.freeletics.khonshu.codegen.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.codegen.util.lateinitPropertySpec
import com.freeletics.khonshu.codegen.codegen.util.layoutInflater
import com.freeletics.khonshu.codegen.codegen.util.navEventNavigator
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.codegen.util.propertyName
import com.freeletics.khonshu.codegen.codegen.util.requireArguments
import com.freeletics.khonshu.codegen.codegen.util.view
import com.freeletics.khonshu.codegen.codegen.util.viewGroup
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.fragmentName
    get() = "Khonshu${data.baseName}Fragment"

internal abstract class BaseFragmentGenerator<T : FragmentData> : Generator<T>() {

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
        val innerParameterName = "${argumentsParameter.name}ForComponent"
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
                    it.addAnnotation(optInAnnotation(internalNavigatorApi))
                    it.addStatement("val executor = %M()", fragmentFindNavigationExecutor)
                    it.addStatement(
                        "%L = %T.provide(%N, executor, requireContext())",
                        retainedComponentClassName.propertyName,
                        componentProviderClassName,
                        argumentsParameter,
                    )
                } else {
                    it.beginControlFlow(
                        "%L = %M(this, requireContext(), %T::class, %N) { " +
                            "parentComponent: %T, savedStateHandle, %L ->",
                        retainedComponentClassName.propertyName,
                        getComponent,
                        data.parentScope,
                        argumentsParameter,
                        retainedParentComponentClassName,
                        innerParameterName,
                    )
                    it.addStatement(
                        "parentComponent.%L().%L(savedStateHandle, %L)",
                        retainedParentComponentGetterName,
                        retainedComponentFactoryCreateName,
                        innerParameterName,
                    )
                    it.endControlFlow()
                }
            }
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
            .addStatement(
                "%M(this, %L.%L)",
                fragmentNavigationHandler,
                retainedComponentClassName.propertyName,
                navEventNavigator.propertyName,
            )
            .build()
    }
}
