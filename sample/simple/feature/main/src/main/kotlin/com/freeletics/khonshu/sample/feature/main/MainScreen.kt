package com.freeletics.khonshu.sample.feature.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.NavHostActivity
import com.freeletics.khonshu.codegen.SimpleNavHost
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.sample.feature.root.nav.RootRoute
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@NavHostActivity(
    stateMachine = MainStateMachine::class,
    activityBaseClass = ComponentActivity::class,
)
@Composable
internal fun MainScreen(
    navHost: SimpleNavHost,
) {
    navHost(RootRoute, Modifier.fillMaxSize()) { _, _ -> }
}

@ContributesTo(ActivityScope::class)
@Module
object MainModule {
    @Provides
    fun provideRoot(): NavRoot = RootRoute
}
