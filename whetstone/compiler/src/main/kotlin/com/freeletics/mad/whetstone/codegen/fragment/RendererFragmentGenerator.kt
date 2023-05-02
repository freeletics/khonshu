package com.freeletics.mad.whetstone.codegen.fragment

import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.common.retainedComponentClassName
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.rendererConnect
import com.squareup.kotlinpoet.CodeBlock

internal class RendererFragmentGenerator(
    override val data: RendererFragmentData,
) : BaseFragmentGenerator<RendererFragmentData>() {

    override fun createViewCode(): CodeBlock {
        return CodeBlock.Builder()
            // inflate: external method
            .addStatement(
                "val renderer = %L.%L.inflate(inflater, container)",
                retainedComponentClassName.propertyName,
                data.factory.propertyName,
            )
            .addStatement(
                "%M(renderer, %L.%L)",
                rendererConnect,
                retainedComponentClassName.propertyName,
                data.stateMachine.propertyName,
            )
            .addStatement("return renderer.rootView")
            .build()
    }
}
