package com.freeletics.simple

import com.freeletics.khonshu.codegen.GlobalGraphProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.createGraphFactory
import kotlin.reflect.KClass

object App : GlobalGraphProvider {
    private val graph by lazy {
        createGraphFactory<AppGraph.Factory>().create()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getGraph(scope: KClass<*>): T {
        if (scope == AppScope::class) {
            return graph as T
        }
        throw IllegalArgumentException("Unknown scope")
    }
}
