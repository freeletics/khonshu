package com.freeletics.mad.codegen.codegen.fragment

import com.freeletics.mad.codegen.ComposeFragmentData
import com.freeletics.mad.codegen.codegen.common.composableName
import com.freeletics.mad.codegen.codegen.common.retainedComponentClassName
import com.freeletics.mad.codegen.codegen.util.composeView
import com.freeletics.mad.codegen.codegen.util.disposeOnLifecycleDestroyed
import com.freeletics.mad.codegen.codegen.util.propertyName
import com.squareup.kotlinpoet.CodeBlock

internal class ComposeFragmentGenerator(
    override val data: ComposeFragmentData,
) : BaseFragmentGenerator<ComposeFragmentData>() {

    override fun createViewCode(): CodeBlock {
        return CodeBlock.builder()
            // requireContext: external method
            .beginControlFlow("return %T(requireContext()).apply {", composeView)
            // setViewCompositionStrategy: external method
            .addStatement("setViewCompositionStrategy(%T)", disposeOnLifecycleDestroyed)
            .add("\n")
            // setContent: external method
            .beginControlFlow("setContent {")
            .addStatement("%L(%L)", composableName, retainedComponentClassName.propertyName)
            .endControlFlow()
            .endControlFlow()
            .build()
    }
}
