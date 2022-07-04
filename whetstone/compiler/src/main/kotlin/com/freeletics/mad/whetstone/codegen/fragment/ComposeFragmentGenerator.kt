package com.freeletics.mad.whetstone.codegen.fragment

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.codegen.common.composableName
import com.freeletics.mad.whetstone.codegen.common.retainedComponentClassName
import com.freeletics.mad.whetstone.codegen.util.composeView
import com.freeletics.mad.whetstone.codegen.util.disposeOnLifecycleDestroyed
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.squareup.kotlinpoet.CodeBlock

internal class ComposeFragmentGenerator(
    override val data: ComposeFragmentData,
) : BaseFragmentGenerator<ComposeFragmentData>() {

    override fun createViewCode(): CodeBlock {
        return CodeBlock.builder()
            // requireContext: external method
            .beginControlFlow("return %T(requireContext()).apply {", composeView)
            // setViewCompositionStrategy: external method
            // viewLifecycleOwner: external method
            .addStatement("setViewCompositionStrategy(%T(viewLifecycleOwner))", disposeOnLifecycleDestroyed)
            .add("\n")
            // setContent: external method
            .beginControlFlow("setContent {")
            .addStatement("%L(%L)", composableName, retainedComponentClassName.propertyName)
            .endControlFlow()
            .endControlFlow()
            .build()
    }
}
