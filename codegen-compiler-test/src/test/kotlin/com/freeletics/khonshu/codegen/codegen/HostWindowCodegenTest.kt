@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.HostWindowData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import dev.zacsweers.metro.AppScope
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class HostWindowCodegenTest {
    private val data = HostWindowData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navHostParameter = ComposableParameter(
            "navHost",
            ClassName("com.freeletics.khonshu.codegen", "SimpleNavHost"),
        ),
        stateMachineClass = ClassName("com.freeletics.khonshu.statemachine", "StateMachine"),
        stateParameter = ComposableParameter("state", ClassName("com.test", "TestState")),
        sendActionParameter = ComposableParameter(
            "sendAction",
            LambdaTypeName.get(null, ClassName("com.test", "TestAction"), returnType = UNIT),
        ),
        composableParameter = emptyList(),
    )

    @Test
    fun `generates code for NavHostWindowData`() {
        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostWindow
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostWindow(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit,
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.runtime.retain.retain
            import androidx.compose.ui.window.Window
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.GlobalGraphProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.test.parent.TestParentScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import dev.zacsweers.metro.SingleIn
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.reflect.KClass
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(
              scope = TestScreen::class,
              additionalScopes = [ActivityScope::class],
            )
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @ForScope(TestScreen::class)
              public val savedStateHandle: SavedStateHandle

              @ForScope(TestScreen::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle, @Provides launchInfo: LaunchInfo): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private val graph: KhonshuTestGraph,
              private val globalGraphProvider: GlobalGraphProvider,
            ) : HostGraphProvider {
              override fun <C> provide(scope: KClass<*>): C {
                if (scope != TestScreen::class && scope != ActivityScope::class) {
                  return globalGraphProvider.getGraph(scope)
                }
                @Suppress("UNCHECKED_CAST")
                return graph as C
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestHostGraph {
              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                startRoot: NavRoot,
                @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                destinations: Set<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(startRoot, destinations, savedStateHandle)
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestWindow(
              private val globalGraphProvider: GlobalGraphProvider,
              private val launchInfo: LaunchInfo,
            ) {
              @Composable
              public fun Show(onCloseRequest: () -> Unit) {
                Window(onCloseRequest = onCloseRequest) {
                  val graph = retain {
                    val parentGraph = globalGraphProvider.getGraph<KhonshuTestGraph.Factory>(TestParentScope::class)
                    val savedStateHandle = SavedStateHandle.createHandle(null, null)
                    parentGraph.createKhonshuTestGraph(savedStateHandle, launchInfo)
                  }
                  val graphProvider = remember(graph) {
                    KhonshuTestGraphProvider(graph, globalGraphProvider)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalHostGraphProvider provides graphProvider) {
                      NavHost(
                        navigator = remember(graph) { graph.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph, navHost: SimpleNavHost) {
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
                  navHost = navHost,
                )
              }
            }

            """.trimIndent()

        test(data, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostWindowData with default values`() {
        val withDefaultValues = data.copy(
            parentScope = AppScope::class.asClassName(),
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.NavHostWindow
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot

            @NavHostWindow(
              scope = TestScreen::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit,
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.runtime.retain.retain
            import androidx.compose.ui.window.Window
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.GlobalGraphProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import dev.zacsweers.metro.AppScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import dev.zacsweers.metro.SingleIn
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.reflect.KClass
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(
              scope = TestScreen::class,
              additionalScopes = [ActivityScope::class],
            )
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @ForScope(TestScreen::class)
              public val savedStateHandle: SavedStateHandle

              @ForScope(TestScreen::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(AppScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle, @Provides launchInfo: LaunchInfo): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private val graph: KhonshuTestGraph,
              private val globalGraphProvider: GlobalGraphProvider,
            ) : HostGraphProvider {
              override fun <C> provide(scope: KClass<*>): C {
                if (scope != TestScreen::class && scope != ActivityScope::class) {
                  return globalGraphProvider.getGraph(scope)
                }
                @Suppress("UNCHECKED_CAST")
                return graph as C
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestHostGraph {
              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                startRoot: NavRoot,
                @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                destinations: Set<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(startRoot, destinations, savedStateHandle)
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestWindow(
              private val globalGraphProvider: GlobalGraphProvider,
              private val launchInfo: LaunchInfo,
            ) {
              @Composable
              public fun Show(onCloseRequest: () -> Unit) {
                Window(onCloseRequest = onCloseRequest) {
                  val graph = retain {
                    val parentGraph = globalGraphProvider.getGraph<KhonshuTestGraph.Factory>(AppScope::class)
                    val savedStateHandle = SavedStateHandle.createHandle(null, null)
                    parentGraph.createKhonshuTestGraph(savedStateHandle, launchInfo)
                  }
                  val graphProvider = remember(graph) {
                    KhonshuTestGraphProvider(graph, globalGraphProvider)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalHostGraphProvider provides graphProvider) {
                      NavHost(
                        navigator = remember(graph) { graph.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph, navHost: SimpleNavHost) {
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
                  navHost = navHost,
                )
              }
            }

            """.trimIndent()

        test(withDefaultValues, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostWindowData with Composable Dependencies`() {
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
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostWindow
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope

            @NavHostWindow(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
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
                navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.runtime.retain.retain
            import androidx.compose.ui.window.Window
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.GlobalGraphProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import dev.zacsweers.metro.SingleIn
            import kotlin.AutoCloseable
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.String
            import kotlin.Unit
            import kotlin.collections.Map
            import kotlin.collections.Set
            import kotlin.reflect.KClass
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(
              scope = TestScreen::class,
              additionalScopes = [ActivityScope::class],
            )
            public interface KhonshuTest2Graph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @ForScope(TestScreen::class)
              public val savedStateHandle: SavedStateHandle

              public val testClass: TestClass

              public val jvmTest: TestClass2

              public val testSet: Set<String>

              public val testMap: Map<String, Int>

              @ForScope(TestScreen::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTest2Graph(@Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle, @Provides launchInfo: LaunchInfo): KhonshuTest2Graph
              }
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTest2GraphProvider(
              private val graph: KhonshuTest2Graph,
              private val globalGraphProvider: GlobalGraphProvider,
            ) : HostGraphProvider {
              override fun <C> provide(scope: KClass<*>): C {
                if (scope != TestScreen::class && scope != ActivityScope::class) {
                  return globalGraphProvider.getGraph(scope)
                }
                @Suppress("UNCHECKED_CAST")
                return graph as C
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTest2HostGraph {
              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                startRoot: NavRoot,
                @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                destinations: Set<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(startRoot, destinations, savedStateHandle)
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTest2Window(
              private val globalGraphProvider: GlobalGraphProvider,
              private val launchInfo: LaunchInfo,
            ) {
              @Composable
              public fun Show(onCloseRequest: () -> Unit) {
                Window(onCloseRequest = onCloseRequest) {
                  val graph = retain {
                    val parentGraph = globalGraphProvider.getGraph<KhonshuTest2Graph.Factory>(TestParentScope::class)
                    val savedStateHandle = SavedStateHandle.createHandle(null, null)
                    parentGraph.createKhonshuTest2Graph(savedStateHandle, launchInfo)
                  }
                  val graphProvider = remember(graph) {
                    KhonshuTest2GraphProvider(graph, globalGraphProvider)
                  }
                  KhonshuTest2(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalHostGraphProvider provides graphProvider) {
                      NavHost(
                        navigator = remember(graph) { graph.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest2(graph: KhonshuTest2Graph, navHost: SimpleNavHost) {
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
                  navHost = navHost,
                )
              }
            }

            """.trimIndent()

        test(withInjectedParameters, "com/test/Test2.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostWindowData without sendAction`() {
        val withoutSendAction = data.copy(
            sendActionParameter = null,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostWindow
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostWindow(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.retain.retain
            import androidx.compose.ui.window.Window
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.GlobalGraphProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.test.parent.TestParentScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import dev.zacsweers.metro.SingleIn
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.reflect.KClass

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(
              scope = TestScreen::class,
              additionalScopes = [ActivityScope::class],
            )
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @ForScope(TestScreen::class)
              public val savedStateHandle: SavedStateHandle

              @ForScope(TestScreen::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle, @Provides launchInfo: LaunchInfo): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private val graph: KhonshuTestGraph,
              private val globalGraphProvider: GlobalGraphProvider,
            ) : HostGraphProvider {
              override fun <C> provide(scope: KClass<*>): C {
                if (scope != TestScreen::class && scope != ActivityScope::class) {
                  return globalGraphProvider.getGraph(scope)
                }
                @Suppress("UNCHECKED_CAST")
                return graph as C
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestHostGraph {
              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                startRoot: NavRoot,
                @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                destinations: Set<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(startRoot, destinations, savedStateHandle)
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestWindow(
              private val globalGraphProvider: GlobalGraphProvider,
              private val launchInfo: LaunchInfo,
            ) {
              @Composable
              public fun Show(onCloseRequest: () -> Unit) {
                Window(onCloseRequest = onCloseRequest) {
                  val graph = retain {
                    val parentGraph = globalGraphProvider.getGraph<KhonshuTestGraph.Factory>(TestParentScope::class)
                    val savedStateHandle = SavedStateHandle.createHandle(null, null)
                    parentGraph.createKhonshuTestGraph(savedStateHandle, launchInfo)
                  }
                  val graphProvider = remember(graph) {
                    KhonshuTestGraphProvider(graph, globalGraphProvider)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalHostGraphProvider provides graphProvider) {
                      NavHost(
                        navigator = remember(graph) { graph.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph, navHost: SimpleNavHost) {
              val stateMachine = remember { graph.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  navHost = navHost,
                )
              }
            }

            """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostWindowData without state`() {
        val withoutSendAction = data.copy(
            stateParameter = null,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostWindow
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostWindow(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              sendAction: (TestAction) -> Unit,
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.runtime.retain.retain
            import androidx.compose.ui.window.Window
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.GlobalGraphProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.test.parent.TestParentScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import dev.zacsweers.metro.SingleIn
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.reflect.KClass
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(
              scope = TestScreen::class,
              additionalScopes = [ActivityScope::class],
            )
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @ForScope(TestScreen::class)
              public val savedStateHandle: SavedStateHandle

              @ForScope(TestScreen::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle, @Provides launchInfo: LaunchInfo): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private val graph: KhonshuTestGraph,
              private val globalGraphProvider: GlobalGraphProvider,
            ) : HostGraphProvider {
              override fun <C> provide(scope: KClass<*>): C {
                if (scope != TestScreen::class && scope != ActivityScope::class) {
                  return globalGraphProvider.getGraph(scope)
                }
                @Suppress("UNCHECKED_CAST")
                return graph as C
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestHostGraph {
              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                startRoot: NavRoot,
                @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                destinations: Set<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(startRoot, destinations, savedStateHandle)
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestWindow(
              private val globalGraphProvider: GlobalGraphProvider,
              private val launchInfo: LaunchInfo,
            ) {
              @Composable
              public fun Show(onCloseRequest: () -> Unit) {
                Window(onCloseRequest = onCloseRequest) {
                  val graph = retain {
                    val parentGraph = globalGraphProvider.getGraph<KhonshuTestGraph.Factory>(TestParentScope::class)
                    val savedStateHandle = SavedStateHandle.createHandle(null, null)
                    parentGraph.createKhonshuTestGraph(savedStateHandle, launchInfo)
                  }
                  val graphProvider = remember(graph) {
                    KhonshuTestGraphProvider(graph, globalGraphProvider)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalHostGraphProvider provides graphProvider) {
                      NavHost(
                        navigator = remember(graph) { graph.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph, navHost: SimpleNavHost) {
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
                  navHost = navHost,
                )
              }
            }

            """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostWindowData with lambda parameter`() {
        // nothing changes on the data side
        val withLambdaParameter = data

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostWindow
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostWindow(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit,
              navHost: @Composable (Modifier, ((NavRoot, BaseRoute) -> Unit)?) -> Unit,
            ) {
                navHost(Modifier)  { _, _ -> }
            }
            """.trimIndent()

        @Language("kotlin")
        val expected =
            """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.runtime.retain.retain
            import androidx.compose.ui.window.Window
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.GlobalGraphProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.HostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalHostGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.test.parent.TestParentScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import dev.zacsweers.metro.SingleIn
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.reflect.KClass
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(
              scope = TestScreen::class,
              additionalScopes = [ActivityScope::class],
            )
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @ForScope(TestScreen::class)
              public val savedStateHandle: SavedStateHandle

              @ForScope(TestScreen::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(TestParentScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(@Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle, @Provides launchInfo: LaunchInfo): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private val graph: KhonshuTestGraph,
              private val globalGraphProvider: GlobalGraphProvider,
            ) : HostGraphProvider {
              override fun <C> provide(scope: KClass<*>): C {
                if (scope != TestScreen::class && scope != ActivityScope::class) {
                  return globalGraphProvider.getGraph(scope)
                }
                @Suppress("UNCHECKED_CAST")
                return graph as C
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestHostGraph {
              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                startRoot: NavRoot,
                @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                destinations: Set<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(startRoot, destinations, savedStateHandle)
            }

            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public class KhonshuTestWindow(
              private val globalGraphProvider: GlobalGraphProvider,
              private val launchInfo: LaunchInfo,
            ) {
              @Composable
              public fun Show(onCloseRequest: () -> Unit) {
                Window(onCloseRequest = onCloseRequest) {
                  val graph = retain {
                    val parentGraph = globalGraphProvider.getGraph<KhonshuTestGraph.Factory>(TestParentScope::class)
                    val savedStateHandle = SavedStateHandle.createHandle(null, null)
                    parentGraph.createKhonshuTestGraph(savedStateHandle, launchInfo)
                  }
                  val graphProvider = remember(graph) {
                    KhonshuTestGraphProvider(graph, globalGraphProvider)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalHostGraphProvider provides graphProvider) {
                      NavHost(
                        navigator = remember(graph) { graph.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(graph: KhonshuTestGraph, navHost: SimpleNavHost) {
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
                  navHost = navHost,
                )
              }
            }

            """.trimIndent()

        test(withLambdaParameter, "com/test/Test.kt", source, expected)
    }
}
