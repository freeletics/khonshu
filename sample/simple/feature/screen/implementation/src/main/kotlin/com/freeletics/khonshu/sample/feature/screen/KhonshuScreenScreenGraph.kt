package com.freeletics.khonshu.sample.feature.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.`internal`.ActivityGraphProvider
import com.freeletics.khonshu.codegen.`internal`.GraphProvider
import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
import com.freeletics.khonshu.codegen.`internal`.LocalActivityGraphProvider
import com.freeletics.khonshu.codegen.`internal`.asComposeState
import com.freeletics.khonshu.codegen.`internal`.getGraph
import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavigationSetup
import com.freeletics.khonshu.navigation.ScreenDestination
import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.`internal`.StackEntry
import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesGraphExtension
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.AutoCloseable
import kotlin.OptIn
import kotlin.Unit
import kotlin.collections.Set
import kotlinx.coroutines.launch

@OptIn(InternalCodegenApi::class)
@SingleIn(ScreenRoute::class)
@ContributesGraphExtension(
    ScreenRoute::class,
    isExtendable = true,
)
public interface KhonshuScreenScreenGraph : AutoCloseable {
    public val screenStateMachine: ScreenStateMachine

    @ForScope(ScreenRoute::class)
    public val activityNavigator: ActivityNavigator

    @ForScope(ScreenRoute::class)
    public val closeables: Set<AutoCloseable>

    @Multibinds(allowEmpty = true)
    @ForScope(ScreenRoute::class)
    public fun bindCloseables(): Set<AutoCloseable>

    override fun close() {
        closeables.forEach {
            it.close()
        }
    }

    @ContributesGraphExtension.Factory(ActivityScope::class)
    public interface Factory {
        public fun createKhonshuScreenScreenGraph(
            @Provides @ForScope(ScreenRoute::class) savedStateHandle: SavedStateHandle,
            @Provides screenRoute: ScreenRoute,
        ): KhonshuScreenScreenGraph
    }
}

@OptIn(InternalCodegenApi::class)
public object KhonshuScreenScreenGraphProvider : GraphProvider<ScreenRoute, KhonshuScreenScreenGraph> {
    @OptIn(InternalNavigationCodegenApi::class)
    override fun provide(
        entry: StackEntry<ScreenRoute>,
        snapshot: StackSnapshot,
        provider: ActivityGraphProvider,
    ): KhonshuScreenScreenGraph = getGraph(
        entry,
        provider,
        ActivityScope::class,
    ) { factory: KhonshuScreenScreenGraph.Factory ->
        factory.createKhonshuScreenScreenGraph(entry.savedStateHandle, entry.route)
    }
}

@Composable
@OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
public fun KhonshuScreenScreen(snapshot: StackSnapshot, entry: StackEntry<ScreenRoute>) {
    val provider = LocalActivityGraphProvider.current
    val graph = remember(entry, snapshot, provider) {
        KhonshuScreenScreenGraphProvider.provide(entry, snapshot, provider)
    }

    NavigationSetup(graph.activityNavigator)

    KhonshuScreenScreen(graph)
}

@Composable
@OptIn(InternalCodegenApi::class)
private fun KhonshuScreenScreen(graph: KhonshuScreenScreenGraph) {
    val stateMachine = remember { graph.screenStateMachine }
    val scope = rememberCoroutineScope()
    val sendAction: (ScreenAction) -> Unit = remember(stateMachine, scope) {
        { scope.launch { stateMachine.dispatch(it) } }
    }
    val state = remember { mutableStateOf(ScreenState(0)) }
    ScreenScreen(
        state = state.value,
        sendAction = sendAction,
    )
}

@OptIn(InternalCodegenApi::class)
@ContributesTo(AppScope::class)
public interface KhonshuScreenScreenNavDestinationGraph {
    @Provides
    @IntoSet
    @OptIn(InternalNavigationCodegenApi::class)
    public fun provideScreenScreenNavDestination(): NavDestination = ScreenDestination<ScreenRoute>(
        KhonshuScreenScreenGraphProvider,
    ) {
        snapshot,
        route,
        ->
        KhonshuScreenScreen(snapshot, route)
    }
}
