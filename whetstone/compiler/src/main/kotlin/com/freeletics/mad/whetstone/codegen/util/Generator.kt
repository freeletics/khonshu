package com.freeletics.mad.whetstone.codegen.util

import com.freeletics.mad.whetstone.BaseData
import com.squareup.kotlinpoet.ClassName

internal abstract class Generator<T : BaseData> {
    abstract val data: T

    fun ClassName(name: String): ClassName {
        return ClassName(data.packageName, name)
    }
}
