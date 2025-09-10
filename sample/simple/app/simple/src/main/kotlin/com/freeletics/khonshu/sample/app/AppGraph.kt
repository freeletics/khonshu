package com.freeletics.khonshu.sample.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(scope = AppScope::class)
interface AppGraph {
    @DependencyGraph.Factory
    interface Factory {
        fun create(): AppGraph
    }
}
