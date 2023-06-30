package com.freeletics.khonshu.codegen.codegen.fragment

import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.codegen.common.composableName
import com.freeletics.khonshu.codegen.codegen.common.retainedComponentClassName
import com.freeletics.khonshu.codegen.codegen.util.composeView
import com.freeletics.khonshu.codegen.codegen.util.disposeOnLifecycleDestroyed
import com.freeletics.khonshu.codegen.codegen.util.propertyName
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
