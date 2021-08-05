package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Extra
import com.freeletics.mad.whetstone.Data
import com.squareup.kotlinpoet.FileSpec

internal class FileGenerator(
    private val data: Data,
) {

    private val retainedComponentGenerator = RetainedComponentGenerator(data)
    private val viewModelGenerator = ViewModelGenerator(data)
    private val rendererFragmentGenerator = RendererFragmentGenerator(data)
    private val composeFragmentGenerator = ComposeFragmentGenerator(data)
    private val composeGenerator = ComposeGenerator(data)

    fun generate(): FileSpec {
        val builder = FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())

        if (data.extra is Extra.Compose) {
            builder.addFunction(composeGenerator.generate())
            if (data.extra.withFragment) {
                builder.addType(composeFragmentGenerator.generate())
            }
        }

        if (data.extra is Extra.Renderer) {
            builder.addType(rendererFragmentGenerator.generate())
        }

        return builder.build()
    }
}
