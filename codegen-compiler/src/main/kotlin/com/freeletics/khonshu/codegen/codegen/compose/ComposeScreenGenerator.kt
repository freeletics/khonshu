package com.freeletics.khonshu.codegen.codegen.compose

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.common.componentProviderClassName
import com.freeletics.khonshu.codegen.codegen.common.composableName
import com.freeletics.khonshu.codegen.codegen.common.retainedComponentFactoryCreateName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentClassName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentGetterName
import com.freeletics.khonshu.codegen.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.codegen.util.asParameter
import com.freeletics.khonshu.codegen.codegen.util.composable
import com.freeletics.khonshu.codegen.codegen.util.composeLocalNavigationExecutor
import com.freeletics.khonshu.codegen.codegen.util.composeNavigationHandler
import com.freeletics.khonshu.codegen.codegen.util.getComponent
import com.freeletics.khonshu.codegen.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.codegen.util.localContext
import com.freeletics.khonshu.codegen.codegen.util.localViewModelStoreOwner
import com.freeletics.khonshu.codegen.codegen.util.navEventNavigator
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.codegen.util.propertyName
import com.freeletics.khonshu.codegen.codegen.util.remember
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

internal class ComposeScreenGenerator(
    override val data: ComposeScreenData,
) : Generator<ComposeScreenData>() {

    internal fun generate(): FunSpec {
        val parameter = data.navigation.asParameter()
        val innerParameterName = "${parameter.name}ForComponent"
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .also {
                if (data.navigation != null) {
                    it.addAnnotation(optInAnnotation(InternalCodegenApi, internalNavigatorApi))
                } else {
                    it.addAnnotation(optInAnnotation())
                }
            }
            .addParameter(parameter)
            .addStatement("val context = %M.current", localContext)
            .also {
                if (data.navigation != null) {
                    it.addStatement("val executor = %M.current", composeLocalNavigationExecutor)
                    it.beginControlFlow("val component = %M(context, executor, %N)", remember, parameter)
                    it.addStatement("%T.provide(%N, executor, context)", componentProviderClassName, parameter)
                } else {
                    it.addStatement("val viewModelStoreOwner = checkNotNull(%T.current)", localViewModelStoreOwner)
                    it.beginControlFlow("val component = %M(viewModelStoreOwner, context, arguments)", remember)
                    it.beginControlFlow(
                        "%M(viewModelStoreOwner, context, %T::class, %N) { " +
                            "parentComponent: %T, savedStateHandle, %L ->",
                        getComponent,
                        data.parentScope,
                        parameter,
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
            .endControlFlow()
            .addCode("\n")
            .addCode(composableNavigationSetup())
            .addStatement("%L(component)", composableName)
            .build()
    }

    private fun composableNavigationSetup(): CodeBlock {
        if (data.navigation == null) {
            return CodeBlock.of("")
        }

        return CodeBlock.builder()
            .addStatement("%M(component.%L)", composeNavigationHandler, navEventNavigator.propertyName)
            .add("\n")
            .build()
    }
}
