package com.freeletics.khonshu.sample.app

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph(scope = AppScope::class)
interface AppGraph {
    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @Provides context: Context,
        ): AppGraph
    }
}
