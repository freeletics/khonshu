package com.freeletics.khonshu.sample.app

import android.app.Application
import com.freeletics.khonshu.codegen.GlobalGraphProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.createGraphFactory
import kotlin.reflect.KClass

class App : Application(), GlobalGraphProvider {
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
