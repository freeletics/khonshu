@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.NavHostActivityData
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

internal class NavHostActivityCodegenTest {
    private val data = NavHostActivityData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        activityBaseClass = ClassName("androidx.activity", "ComponentActivity"),
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
    fun `generates code for NavHostActivityData`() {
        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
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

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraph
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.freeletics.khonshu.navigation.deeplinks.asLaunchInfo
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
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestScreen::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

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
                public fun createKhonshuTestGraph(
                  @Provides viewModel: StackEntryStoreViewModel,
                  @Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @Provides launchInfo: LaunchInfo,
                ): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private final val activity: ComponentActivity,
            ) : ActivityGraphProvider {
              override fun <C> provide(scope: KClass<*>): C = getGraph(activity, scope, TestScreen::class, TestParentScope::class) { factory: KhonshuTestGraph.Factory, savedStateHandle ->
                val viewModel = ViewModelProvider(activity, SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                factory.createKhonshuTestGraph(viewModel, savedStateHandle, activity.intent.asLaunchInfo())
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestActivityGraph {
              @Provides
              public fun provideImmutableNavDestinations(destinations: Set<NavDestination<*>>): ImmutableSet<NavDestination<*>> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: ImmutableSet<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val graphProvider = remember {
                    KhonshuTestGraphProvider(this)
                  }
                  val graph = remember(graphProvider) {
                    graphProvider.provide<KhonshuTestGraph>(TestScreen::class)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityGraphProvider provides graphProvider) {
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
    fun `generates code for NavHostActivityData with default values`() {
        val withDefaultValues = data.copy(
            scope = ActivityScope::class.asClassName(),
            parentScope = AppScope::class.asClassName(),
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot

            @NavHostActivity(
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
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

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraph
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.freeletics.khonshu.navigation.deeplinks.asLaunchInfo
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
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(ActivityScope::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @ForScope(ActivityScope::class)
              public val closeables: Set<AutoCloseable>

              @Multibinds(allowEmpty = true)
              @ForScope(ActivityScope::class)
              public fun bindCloseables(): Set<AutoCloseable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesTo(AppScope::class)
              @GraphExtension.Factory
              public interface Factory {
                public fun createKhonshuTestGraph(
                  @Provides viewModel: StackEntryStoreViewModel,
                  @Provides @ForScope(ActivityScope::class) savedStateHandle: SavedStateHandle,
                  @Provides launchInfo: LaunchInfo,
                ): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private final val activity: ComponentActivity,
            ) : ActivityGraphProvider {
              override fun <C> provide(scope: KClass<*>): C = getGraph(activity, scope, ActivityScope::class, AppScope::class) { factory: KhonshuTestGraph.Factory, savedStateHandle ->
                val viewModel = ViewModelProvider(activity, SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                factory.createKhonshuTestGraph(viewModel, savedStateHandle, activity.intent.asLaunchInfo())
              }
            }

            @ContributesTo(ActivityScope::class)
            public interface KhonshuTestActivityGraph {
              @Provides
              public fun provideImmutableNavDestinations(destinations: Set<NavDestination<*>>): ImmutableSet<NavDestination<*>> = destinations.toImmutableSet()

              @Provides
              @SingleIn(ActivityScope::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: ImmutableSet<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val graphProvider = remember {
                    KhonshuTestGraphProvider(this)
                  }
                  val graph = remember(graphProvider) {
                    graphProvider.provide<KhonshuTestGraph>(ActivityScope::class)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityGraphProvider provides graphProvider) {
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
    fun `generates code for NavHostActivityData with Composable Dependencies`() {
        val withInjectedParameters = data.copy(
            baseName = "Test2",
            composableParameter = listOf(
                ComposableParameter(
                    name = "testClass",
                    typeName = ClassName("com.test", "TestClass"),
                ),
                ComposableParameter(
                    name = "test",
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

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test2(
                state: TestState,
                sendAction: (TestAction) -> Unit,
                testClass: TestClass,
                test: TestClass2,
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

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraph
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.freeletics.khonshu.navigation.deeplinks.asLaunchInfo
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
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestScreen::class)
            public interface KhonshuTest2Graph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              public val testClass: TestClass

              public val test: TestClass2

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
                public fun createKhonshuTest2Graph(
                  @Provides viewModel: StackEntryStoreViewModel,
                  @Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @Provides launchInfo: LaunchInfo,
                ): KhonshuTest2Graph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTest2GraphProvider(
              private final val activity: ComponentActivity,
            ) : ActivityGraphProvider {
              override fun <C> provide(scope: KClass<*>): C = getGraph(activity, scope, TestScreen::class, TestParentScope::class) { factory: KhonshuTest2Graph.Factory, savedStateHandle ->
                val viewModel = ViewModelProvider(activity, SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                factory.createKhonshuTest2Graph(viewModel, savedStateHandle, activity.intent.asLaunchInfo())
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTest2ActivityGraph {
              @Provides
              public fun provideImmutableNavDestinations(destinations: Set<NavDestination<*>>): ImmutableSet<NavDestination<*>> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: ImmutableSet<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTest2Activity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val graphProvider = remember {
                    KhonshuTest2GraphProvider(this)
                  }
                  val graph = remember(graphProvider) {
                    graphProvider.provide<KhonshuTest2Graph>(TestScreen::class)
                  }
                  KhonshuTest2(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityGraphProvider provides graphProvider) {
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
              val test = remember { graph.test }
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
                  test = test,
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
    fun `generates code for NavHostActivityData without sendAction`() {
        val withoutSendAction = data.copy(
            sendActionParameter = null,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
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

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraph
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.freeletics.khonshu.navigation.deeplinks.asLaunchInfo
            import com.test.parent.TestParentScope
            import dev.zacsweers.metro.ContributesTo
            import dev.zacsweers.metro.ForScope
            import dev.zacsweers.metro.GraphExtension
            import dev.zacsweers.metro.Multibinds
            import dev.zacsweers.metro.Provides
            import dev.zacsweers.metro.SingleIn
            import kotlin.AutoCloseable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlin.reflect.KClass
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestScreen::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

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
                public fun createKhonshuTestGraph(
                  @Provides viewModel: StackEntryStoreViewModel,
                  @Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @Provides launchInfo: LaunchInfo,
                ): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private final val activity: ComponentActivity,
            ) : ActivityGraphProvider {
              override fun <C> provide(scope: KClass<*>): C = getGraph(activity, scope, TestScreen::class, TestParentScope::class) { factory: KhonshuTestGraph.Factory, savedStateHandle ->
                val viewModel = ViewModelProvider(activity, SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                factory.createKhonshuTestGraph(viewModel, savedStateHandle, activity.intent.asLaunchInfo())
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestActivityGraph {
              @Provides
              public fun provideImmutableNavDestinations(destinations: Set<NavDestination<*>>): ImmutableSet<NavDestination<*>> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: ImmutableSet<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val graphProvider = remember {
                    KhonshuTestGraphProvider(this)
                  }
                  val graph = remember(graphProvider) {
                    graphProvider.provide<KhonshuTestGraph>(TestScreen::class)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityGraphProvider provides graphProvider) {
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
    fun `generates code for NavHostActivityData without state`() {
        val withoutSendAction = data.copy(
            stateParameter = null,
        )

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
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

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraph
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.freeletics.khonshu.navigation.deeplinks.asLaunchInfo
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
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestScreen::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

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
                public fun createKhonshuTestGraph(
                  @Provides viewModel: StackEntryStoreViewModel,
                  @Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @Provides launchInfo: LaunchInfo,
                ): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private final val activity: ComponentActivity,
            ) : ActivityGraphProvider {
              override fun <C> provide(scope: KClass<*>): C = getGraph(activity, scope, TestScreen::class, TestParentScope::class) { factory: KhonshuTestGraph.Factory, savedStateHandle ->
                val viewModel = ViewModelProvider(activity, SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                factory.createKhonshuTestGraph(viewModel, savedStateHandle, activity.intent.asLaunchInfo())
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestActivityGraph {
              @Provides
              public fun provideImmutableNavDestinations(destinations: Set<NavDestination<*>>): ImmutableSet<NavDestination<*>> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: ImmutableSet<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val graphProvider = remember {
                    KhonshuTestGraphProvider(this)
                  }
                  val graph = remember(graphProvider) {
                    graphProvider.provide<KhonshuTestGraph>(TestScreen::class)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityGraphProvider provides graphProvider) {
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
    fun `generates code for NavHostActivityData with lambda parameter`() {
        // nothing changes on the data side
        val withLambdaParameter = data

        @Language("kotlin")
        val source =
            """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.NavRoot
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
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

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityGraphProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.getGraph
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.createHostNavigator
            import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
            import com.freeletics.khonshu.navigation.deeplinks.asLaunchInfo
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
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @GraphExtension(TestScreen::class)
            public interface KhonshuTestGraph : AutoCloseable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

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
                public fun createKhonshuTestGraph(
                  @Provides viewModel: StackEntryStoreViewModel,
                  @Provides @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @Provides launchInfo: LaunchInfo,
                ): KhonshuTestGraph
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestGraphProvider(
              private final val activity: ComponentActivity,
            ) : ActivityGraphProvider {
              override fun <C> provide(scope: KClass<*>): C = getGraph(activity, scope, TestScreen::class, TestParentScope::class) { factory: KhonshuTestGraph.Factory, savedStateHandle ->
                val viewModel = ViewModelProvider(activity, SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                factory.createKhonshuTestGraph(viewModel, savedStateHandle, activity.intent.asLaunchInfo())
              }
            }

            @ContributesTo(TestScreen::class)
            public interface KhonshuTestActivityGraph {
              @Provides
              public fun provideImmutableNavDestinations(destinations: Set<NavDestination<*>>): ImmutableSet<NavDestination<*>> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: ImmutableSet<NavDestination<*>>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val graphProvider = remember {
                    KhonshuTestGraphProvider(this)
                  }
                  val graph = remember(graphProvider) {
                    graphProvider.provide<KhonshuTestGraph>(TestScreen::class)
                  }
                  KhonshuTest(graph) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityGraphProvider provides graphProvider) {
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
