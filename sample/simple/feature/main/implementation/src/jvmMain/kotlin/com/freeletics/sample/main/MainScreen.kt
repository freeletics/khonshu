package com.freeletics.sample.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.NavHostWindow
import com.freeletics.khonshu.codegen.SimpleNavHost
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.sample.root.nav.RootRoute
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@NavHostWindow(
    stateMachine = MainStateMachine::class,
)
@Composable
internal fun MainScreen(
    navHost: SimpleNavHost,
) {
    navHost(Modifier.fillMaxSize()) { _, _ -> }
}

@ContributesTo(ActivityScope::class)
interface MainModule {
    @Provides
    fun provideRoot(): NavRoot = RootRoute
}
