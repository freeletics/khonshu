package com.freeletics.khonshu.sample.app

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@DependencyGraph(scope = AppScope::class)
interface AppGraph {
    @DependencyGraph.Factory
    interface Factory {
        fun create(): AppGraph
    }
}
