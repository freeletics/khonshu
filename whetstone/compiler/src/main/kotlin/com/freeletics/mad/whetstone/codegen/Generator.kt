package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Data
import com.squareup.kotlinpoet.ClassName

internal abstract class Generator {
    abstract val data: Data

    fun ClassName(name: String): ClassName {
        return ClassName(data.packageName, name)
    }
}
