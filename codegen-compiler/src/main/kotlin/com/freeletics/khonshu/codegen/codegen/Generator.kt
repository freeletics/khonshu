package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.squareup.kotlinpoet.ClassName

internal abstract class Generator<T : BaseData> {
    abstract val data: T

    fun ClassName(name: String): ClassName {
        return ClassName(data.packageName, name)
    }
}
