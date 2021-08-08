package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.squareup.kotlinpoet.ClassName

internal abstract class NavEntryGenerator {
    abstract val data: NavEntryData

    fun ClassName(name: String): ClassName {
        return ClassName(data.packageName, name)
    }
}
