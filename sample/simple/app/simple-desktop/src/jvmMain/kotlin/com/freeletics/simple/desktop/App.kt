package com.freeletics.simple.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.freeletics.khonshu.codegen.GlobalGraphProvider
import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
import com.freeletics.sample.main.KhonshuMainScreenWindow
import com.freeletics.simple.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.createGraphFactory
import kotlin.reflect.KClass

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val graph = remember { createGraphFactory<AppGraph.Factory>().create() }
    val graphProvider = remember {
        object : GlobalGraphProvider {
            @Suppress("UNCHECKED_CAST")
            override fun <T> getGraph(scope: KClass<*>): T {
                if (scope == AppScope::class) {
                    return graph as T
                }
                throw IllegalArgumentException("Unknown scope")
            }
        }
    }

    val controller = remember { KhonshuMainScreenWindow(graphProvider, LaunchInfo(null)) }
    controller.Show(onCloseRequest = ::exitApplication)
}
