package com.freeletics.mad.whetstone.codegen.fragment

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.codegen.common.composableName
import com.freeletics.mad.whetstone.codegen.common.retainedComponentClassName
import com.freeletics.mad.whetstone.codegen.util.composeView
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.disposeOnLifecycleDestroyed
import com.freeletics.mad.whetstone.codegen.util.layoutParams
import com.freeletics.mad.whetstone.codegen.util.localWindowInsets
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.viewWindowInsetsObserver
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
            .apply {
                if (data.enableInsetHandling) {
                    addStatement("layoutParams = %1T(%1T.MATCH_PARENT, %1T.MATCH_PARENT)", layoutParams)
                    addStatement("val observer = %T(this)", viewWindowInsetsObserver)
                    // start: external method
                    addStatement("val windowInsets = observer.start()")
                    add("\n")
                }
            }
            // setContent: external method
            .beginControlFlow("setContent {")
            .apply {
                if (data.enableInsetHandling) {
                    beginControlFlow("%T(%T provides windowInsets) {", compositionLocalProvider, localWindowInsets)
                }
            }
            // requireArguments: external method
            .addStatement("%L(%L)", composableName, retainedComponentClassName.propertyName)
            .apply {
                if (data.enableInsetHandling) {
                    endControlFlow()
                }
            }
            .endControlFlow()
            .endControlFlow()
            .build()
    }
}
