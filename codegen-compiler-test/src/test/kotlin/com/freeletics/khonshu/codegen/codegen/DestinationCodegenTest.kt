@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.DestinationData
import com.freeletics.khonshu.codegen.Navigation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.test.TestOverlayRoute
import dev.zacsweers.metro.AppScope
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class DestinationCodegenTest {
    private val navigation = Navigation(
        route = ClassName("com.test", "TestRoute"),
        parentScopeIsRoute = true,
        overlay = false,
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = DestinationData(
        baseName = "Test",
        packageName = "com.test",
        scope = navigation.route,
        parentScope = ClassName("com.test.parent", "TestParentRoute"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = navigation,
        composableParameter = emptyList(),
        stateMachineClass = ClassName("com.freeletics.khonshu.statemachine", "StateMachine"),
        stateParameter = ComposableParameter("state", ClassName("com.test", "TestState")),
        sendActionParameter = ComposableParameter(
            "sendAction",
            LambdaTypeName.get(null, ClassName("com.test", "TestAction"), returnType = UNIT),
        ),
    )

    @Test
    fun `generates code for ComposeScreenData`() {
        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.DestinationGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraphFromParentRoute
            import com.freeletics.khonshu.navigation.DestinationNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.IntoSet
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestRoute::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              @ForScope(TestRoute::class)
              public val destinationNavigator: DestinationNavigator

              @ForScope(TestRoute::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentRoute::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle, @Provides testRoute: TestRoute): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestGraphProvider : DestinationGraphProvider<TestRoute, KhonshuTestGraph> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: HostGraphProvider,
              ): KhonshuTestGraph = getGraphFromParentRoute(entry, snapshot, provider, TestParentRoute::class) { factory: KhonshuTestGraph.Factory ->
                factory.createKhonshuTestGraph(entry.savedStateHandle, entry.route)
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalHostGraphProvider.current
              val graph = remember(entry, snapshot, provider) {
                KhonshuTestGraphProvider.provide(entry, snapshot, provider)
              }

              ActivityNavigatorEffect(graph.destinationNavigator)

              KhonshuTest(graph)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph) {
              val stateMachine = remember { graph.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @ContributesTo(TestDestinationScope::class)
            public interface KhonshuTestNavDestinationGraph {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideTestNavDestination(): NavDestination<*> = ScreenDestination<TestRoute, TestParentRoute>(KhonshuTestGraphProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

            """.trimIndent()

        test(data, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with default values`() {
        val navigation = navigation.copy(
            parentScopeIsRoute = false,
            destinationScope = AppScope::class.asClassName(),
        )
        val withDefaultValues = data.copy(
            scope = navigation.route,
            parentScope = ActivityScope::class.asClassName(),
            navigation = navigation,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination

            @NavDestination(
              route = TestRoute::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.`internal`.DestinationGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraph
            import com.freeletics.khonshu.navigation.DestinationNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect
            import dev.zacsweers.metro.AppScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.IntoSet
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestRoute::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              @ForScope(TestRoute::class)
              public val destinationNavigator: DestinationNavigator

              @ForScope(TestRoute::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(ActivityScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle, @Provides testRoute: TestRoute): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestGraphProvider : DestinationGraphProvider<TestRoute, KhonshuTestGraph> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: HostGraphProvider,
              ): KhonshuTestGraph = getGraph(entry, provider, ActivityScope::class) { factory: KhonshuTestGraph.Factory ->
                factory.createKhonshuTestGraph(entry.savedStateHandle, entry.route)
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalHostGraphProvider.current
              val graph = remember(entry, snapshot, provider) {
                KhonshuTestGraphProvider.provide(entry, snapshot, provider)
              }

              ActivityNavigatorEffect(graph.destinationNavigator)

              KhonshuTest(graph)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph) {
              val stateMachine = remember { graph.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @ContributesTo(AppScope::class)
            public interface KhonshuTestNavDestinationGraph {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideTestNavDestination(): NavDestination<*> = ScreenDestination<TestRoute>(KhonshuTestGraphProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

            """.trimIndent()

        test(withDefaultValues, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with overlay route`() {
        val navigation = navigation.copy(
            route = TestOverlayRoute::class.asClassName(),
            overlay = true,
        )
        val dialogData = data.copy(
            scope = navigation.route,
            navigation = navigation,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestOverlayRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.DestinationGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraphFromParentRoute
            import com.freeletics.khonshu.navigation.DestinationNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.OverlayDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.IntoSet
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestOverlayRoute::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              @ForScope(TestOverlayRoute::class)
              public val destinationNavigator: DestinationNavigator

              @ForScope(TestOverlayRoute::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestOverlayRoute::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentRoute::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestOverlayRoute::class) savedStateHandle: SavedStateHandle, @Provides testOverlayRoute: TestOverlayRoute): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestGraphProvider : DestinationGraphProvider<TestOverlayRoute, KhonshuTestGraph> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestOverlayRoute>,
                snapshot: StackSnapshot,
                provider: HostGraphProvider,
              ): KhonshuTestGraph = getGraphFromParentRoute(entry, snapshot, provider, TestParentRoute::class) { factory: KhonshuTestGraph.Factory ->
                factory.createKhonshuTestGraph(entry.savedStateHandle, entry.route)
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestOverlayRoute>) {
              val provider = LocalHostGraphProvider.current
              val graph = remember(entry, snapshot, provider) {
                KhonshuTestGraphProvider.provide(entry, snapshot, provider)
              }

              ActivityNavigatorEffect(graph.destinationNavigator)

              KhonshuTest(graph)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph) {
              val stateMachine = remember { graph.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @ContributesTo(TestDestinationScope::class)
            public interface KhonshuTestNavDestinationGraph {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideTestNavDestination(): NavDestination<*> = OverlayDestination<TestOverlayRoute, TestParentRoute>(KhonshuTestGraphProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

            """.trimIndent()

        test(dialogData, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with Composable Dependencies`() {
        val withInjectedParameters = data.copy(
            baseName = "Test2",
            composableParameter = listOf(
                ComposableParameter(
                    name = "testClass",
                    typeName = ClassName("com.test", "TestClass"),
                ),
                ComposableParameter(
                    name = "jvmTest",
                    typeName = ClassName("com.test.other", "TestClass2"),
                ),
                ComposableParameter(
                    name = "testSet",
                    typeName = SET.parameterizedBy(STRING),
                ),
                ComposableParameter(
                    name = "testMap",
                    typeName = MAP.parameterizedBy(STRING, INT),
                ),
            ),
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.other.TestClass2
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test2(
                state: TestState,
                sendAction: (TestAction) -> Unit,
                testClass: TestClass,
                jvmTest: TestClass2,
                testSet: Set<String>,
                testMap: Map<String, Int>,
            ) {}
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.DestinationGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraphFromParentRoute
            import com.freeletics.khonshu.navigation.DestinationNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect
            import com.test.destination.TestDestinationScope
            import com.test.other.TestClass2
            import com.test.parent.TestParentRoute
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.IntoSet
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import kotlin.AutoCloseable
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.String
            import kotlin.Unit
            import kotlin.collections.Map
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestRoute::class)
            public interface KhonshuTest2Graph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              @ForScope(TestRoute::class)
              public val destinationNavigator: DestinationNavigator

              public val testClass: TestClass

              public val jvmTest: TestClass2

              public val testSet: Set<String>

              public val testMap: Map<String, Int>

              @ForScope(TestRoute::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentRoute::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTest2Graph(@Provides @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle, @Provides testRoute: TestRoute): KhonshuTest2Graph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTest2GraphProvider : DestinationGraphProvider<TestRoute, KhonshuTest2Graph> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: HostGraphProvider,
              ): KhonshuTest2Graph = getGraphFromParentRoute(entry, snapshot, provider, TestParentRoute::class) { factory: KhonshuTest2Graph.Factory ->
                factory.createKhonshuTest2Graph(entry.savedStateHandle, entry.route)
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest2(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalHostGraphProvider.current
              val graph = remember(entry, snapshot, provider) {
                KhonshuTest2GraphProvider.provide(entry, snapshot, provider)
              }

              ActivityNavigatorEffect(graph.destinationNavigator)

              KhonshuTest2(graph)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest2(graph: KhonshuTest2Graph) {
              val testClass = remember { graph.testClass }
              val jvmTest = remember { graph.jvmTest }
              val testSet = remember { graph.testSet }
              val testMap = remember { graph.testMap }
              val stateMachine = remember { graph.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test2(
                  testClass = testClass,
                  jvmTest = jvmTest,
                  testSet = testSet,
                  testMap = testMap,
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @ContributesTo(TestDestinationScope::class)
            public interface KhonshuTest2NavDestinationGraph {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideTest2NavDestination(): NavDestination<*> = ScreenDestination<TestRoute, TestParentRoute>(KhonshuTest2GraphProvider) { snapshot, route ->
                KhonshuTest2(snapshot, route)
              }
            }

            """.trimIndent()

        test(withInjectedParameters, "com/test/Test2.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData without sendAction`() {
        val withoutSendAction = data.copy(
            sendActionParameter = null,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
            ) {}
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.DestinationGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraphFromParentRoute
            import com.freeletics.khonshu.navigation.DestinationNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.IntoSet
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestRoute::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              @ForScope(TestRoute::class)
              public val destinationNavigator: DestinationNavigator

              @ForScope(TestRoute::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentRoute::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle, @Provides testRoute: TestRoute): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestGraphProvider : DestinationGraphProvider<TestRoute, KhonshuTestGraph> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: HostGraphProvider,
              ): KhonshuTestGraph = getGraphFromParentRoute(entry, snapshot, provider, TestParentRoute::class) { factory: KhonshuTestGraph.Factory ->
                factory.createKhonshuTestGraph(entry.savedStateHandle, entry.route)
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalHostGraphProvider.current
              val graph = remember(entry, snapshot, provider) {
                KhonshuTestGraphProvider.provide(entry, snapshot, provider)
              }

              ActivityNavigatorEffect(graph.destinationNavigator)

              KhonshuTest(graph)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph) {
              val stateMachine = remember { graph.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @ContributesTo(TestDestinationScope::class)
            public interface KhonshuTestNavDestinationGraph {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideTestNavDestination(): NavDestination<*> = ScreenDestination<TestRoute, TestParentRoute>(KhonshuTestGraphProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

            """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData without state`() {
        val withoutSendAction = data.copy(
            stateParameter = null,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              sendAction: (TestAction) -> Unit
            ) {}
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.DestinationGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraphFromParentRoute
            import com.freeletics.khonshu.navigation.DestinationNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.IntoSet
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestRoute::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              @ForScope(TestRoute::class)
              public val destinationNavigator: DestinationNavigator

              @ForScope(TestRoute::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentRoute::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle, @Provides testRoute: TestRoute): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestGraphProvider : DestinationGraphProvider<TestRoute, KhonshuTestGraph> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: HostGraphProvider,
              ): KhonshuTestGraph = getGraphFromParentRoute(entry, snapshot, provider, TestParentRoute::class) { factory: KhonshuTestGraph.Factory ->
                factory.createKhonshuTestGraph(entry.savedStateHandle, entry.route)
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalHostGraphProvider.current
              val graph = remember(entry, snapshot, provider) {
                KhonshuTestGraphProvider.provide(entry, snapshot, provider)
              }

              ActivityNavigatorEffect(graph.destinationNavigator)

              KhonshuTest(graph)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph) {
              val stateMachine = remember { graph.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @ContributesTo(TestDestinationScope::class)
            public interface KhonshuTestNavDestinationGraph {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideTestNavDestination(): NavDestination<*> = ScreenDestination<TestRoute, TestParentRoute>(KhonshuTestGraphProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

            """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with FlowReduxStateMachineFactory`() {
        val withFactory = data.copy(
            stateMachine = ClassName("com.test", "TestStateMachineFactory"),
            stateMachineClass = ClassName("com.freeletics.flowredux2", "FlowReduxStateMachineFactory"),
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachineFactory::class,
              destinationScope = TestDestinationScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.flowredux2.produceStateMachine
            import com.freeletics.khonshu.codegen.`internal`.DestinationGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.getGraphFromParentRoute
            import com.freeletics.khonshu.navigation.DestinationNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.IntoSet
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.ExperimentalCoroutinesApi

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestRoute::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachineFactory: TestStateMachineFactory

              @ForScope(TestRoute::class)
              public val destinationNavigator: DestinationNavigator

              @ForScope(TestRoute::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentRoute::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle, @Provides testRoute: TestRoute): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestGraphProvider : DestinationGraphProvider<TestRoute, KhonshuTestGraph> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: HostGraphProvider,
              ): KhonshuTestGraph = getGraphFromParentRoute(entry, snapshot, provider, TestParentRoute::class) { factory: KhonshuTestGraph.Factory ->
                factory.createKhonshuTestGraph(entry.savedStateHandle, entry.route)
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalHostGraphProvider.current
              val graph = remember(entry, snapshot, provider) {
                KhonshuTestGraphProvider.provide(entry, snapshot, provider)
              }

              ActivityNavigatorEffect(graph.destinationNavigator)

              KhonshuTest(graph)
            }

            @Composable
            @OptIn(ExperimentalCoroutinesApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph) {
              val stateMachineFactory = remember { graph.testStateMachineFactory }
              val stateMachine = stateMachineFactory.produceStateMachine()
              val currentState = stateMachine.state.value
              val sendAction = stateMachine.dispatchAction
              Test(
                state = currentState,
                sendAction = sendAction,
              )
            }

            @OptIn(InternalCodegenApi::class)
            @ContributesTo(TestDestinationScope::class)
            public interface KhonshuTestNavDestinationGraph {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideTestNavDestination(): NavDestination<*> = ScreenDestination<TestRoute, TestParentRoute>(KhonshuTestGraphProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

            """.trimIndent()

        test(withFactory, "com/test/Test.kt", source, expected)
    }
}
