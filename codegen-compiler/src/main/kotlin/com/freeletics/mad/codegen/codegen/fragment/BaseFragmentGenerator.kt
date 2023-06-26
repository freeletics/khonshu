package com.freeletics.mad.codegen.codegen.fragment

import com.freeletics.mad.codegen.BaseData
import com.freeletics.mad.codegen.FragmentData
import com.freeletics.mad.codegen.codegen.Generator
import com.freeletics.mad.codegen.codegen.common.retainedComponentClassName
import com.freeletics.mad.codegen.codegen.common.retainedComponentFactoryCreateName
import com.freeletics.mad.codegen.codegen.common.retainedParentComponentClassName
import com.freeletics.mad.codegen.codegen.common.retainedParentComponentGetterName
import com.freeletics.mad.codegen.codegen.util.asParameter
import com.freeletics.mad.codegen.codegen.util.bundle
import com.freeletics.mad.codegen.codegen.util.fragmentComponent
import com.freeletics.mad.codegen.codegen.util.fragmentNavigationHandler
import com.freeletics.mad.codegen.codegen.util.lateinitPropertySpec
import com.freeletics.mad.codegen.codegen.util.layoutInflater
import com.freeletics.mad.codegen.codegen.util.navEventNavigator
import com.freeletics.mad.codegen.codegen.util.optInAnnotation
import com.freeletics.mad.codegen.codegen.util.propertyName
import com.freeletics.mad.codegen.codegen.util.requireArguments
import com.freeletics.mad.codegen.codegen.util.view
import com.freeletics.mad.codegen.codegen.util.viewGroup
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.fragmentName
    get() = "Mad${data.baseName}Fragment"

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
                    it.beginControlFlow(
                        "%L = %M(%T::class, %T::class, %N) { parentComponent: %T, savedStateHandle, %L ->",
                        retainedComponentClassName.propertyName,
                        fragmentComponent,
                        data.parentScope,
                        data.navigation!!.destinationScope,
                        argumentsParameter,
                        retainedParentComponentClassName,
                        innerParameterName,
                    )
                } else {
                    it.beginControlFlow(
                        "%L = %M(%T::class, %N) { parentComponent: %T, savedStateHandle, %L ->",
                        retainedComponentClassName.propertyName,
                        fragmentComponent,
                        data.parentScope,
                        argumentsParameter,
                        retainedParentComponentClassName,
                        innerParameterName,
                    )
                }
            }
            .addStatement(
                "parentComponent.%L().%L(savedStateHandle, %L)",
                retainedParentComponentGetterName,
                retainedComponentFactoryCreateName,
                innerParameterName,
            )
            .endControlFlow()
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
