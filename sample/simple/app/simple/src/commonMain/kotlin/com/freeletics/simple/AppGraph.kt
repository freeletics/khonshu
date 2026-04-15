package com.freeletics.simple

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph(scope = AppScope::class)
interface AppGraph {
    @DependencyGraph.Factory
    interface Factory {
        fun create(): AppGraph
    }
}
