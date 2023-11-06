@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.Navigation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.test.TestOverlayRoute
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class FileGeneratorTestComposeFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        parentScopeIsRoute = true,
        overlay = false,
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = ComposeFragmentData(
        baseName = "FragmentTest",
        packageName = "com.test",
        scope = navigation.route,
        parentScope = ClassName("com.test.parent", "TestParentRoute"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        fragmentBaseClass = ClassName("androidx.fragment.app", "Fragment"),
        navigation = navigation,
        composableParameter = emptyList(),
        stateParameter = ComposableParameter("state", ClassName("com.test", "TestState")),
        sendActionParameter = ComposableParameter(
            "sendAction",
            Function1::class.asClassName().parameterizedBy(
                ClassName("com.test", "TestAction"),
                UNIT,
            ),
        ),
    )

    @Test
    fun `generates code for ComposeFragmentData`() {
        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.fragment.ComposeFragmentDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @ComposeFragmentDestination(
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
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuFragmentTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val navEventNavigator: NavEventNavigator

              @get:ForScope(TestRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle,
                    @BindsInstance testRoute: TestRoute): KhonshuFragmentTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuFragmentTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuFragmentTestComponentProvider :
                ComponentProvider<TestRoute, KhonshuFragmentTestComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuFragmentTestComponent = componentFromParentRoute(route.destinationId, route, executor,
                  context, TestParentRoute::class) { parentComponent:
                  KhonshuFragmentTestComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuFragmentTestComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuFragmentTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuFragmentTestFragment : Fragment() {
              private lateinit var khonshuFragmentTestComponent: KhonshuFragmentTestComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuFragmentTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuFragmentTestComponent = KhonshuFragmentTestComponentProvider.provide(testRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuFragmentTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuFragmentTest(khonshuFragmentTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuFragmentTest(component: KhonshuFragmentTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuFragmentTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuFragmentTestFragment>(KhonshuFragmentTestComponentProvider)
            }

        """.trimIndent()

        test(data, "com/test/FragmentTest.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData with default values`() {
        val navigation = navigation.copy(
            parentScopeIsRoute = false,
            destinationScope = AppScope::class.asClassName(),
        )
        val withDefaultValues = data.copy(
            scope = navigation.route,
            parentScope = AppScope::class.asClassName(),
            navigation = navigation,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.fragment.ComposeFragmentDestination

            @ComposeFragmentDestination(
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
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.AppScope
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = AppScope::class,
            )
            public interface KhonshuFragmentTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val navEventNavigator: NavEventNavigator

              @get:ForScope(TestRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle,
                    @BindsInstance testRoute: TestRoute): KhonshuFragmentTestComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun khonshuFragmentTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuFragmentTestComponentProvider :
                ComponentProvider<TestRoute, KhonshuFragmentTestComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuFragmentTestComponent = component(route.destinationId, route, executor, context,
                  AppScope::class) { parentComponent: KhonshuFragmentTestComponent.ParentComponent,
                  savedStateHandle, testRoute ->
                parentComponent.khonshuFragmentTestComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuFragmentTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuFragmentTestFragment : Fragment() {
              private lateinit var khonshuFragmentTestComponent: KhonshuFragmentTestComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuFragmentTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuFragmentTestComponent = KhonshuFragmentTestComponentProvider.provide(testRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuFragmentTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuFragmentTest(khonshuFragmentTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuFragmentTest(component: KhonshuFragmentTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(AppScope::class)
            public object KhonshuFragmentTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuFragmentTestFragment>(KhonshuFragmentTestComponentProvider)
            }

        """.trimIndent()

        test(withDefaultValues, "com/test/FragmentTest.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData, dialog fragment`() {
        val navigation = navigation.copy(
            route = TestOverlayRoute::class.asClassName(),
            overlay = true,
        )
        val dialogFragment = data.copy(
            scope = navigation.route,
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment"),
            navigation = navigation,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.fragment.app.DialogFragment
            import com.freeletics.khonshu.codegen.fragment.ComposeFragmentDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @ComposeFragmentDestination(
              route = TestOverlayRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
              fragmentBaseClass = DialogFragment::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.DialogDestination
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestOverlayRoute::class)
            @ContributesSubcomponent(
              scope = TestOverlayRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuFragmentTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestOverlayRoute::class)
              public val navEventNavigator: NavEventNavigator

              @get:ForScope(TestOverlayRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestOverlayRoute::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance testOverlayRoute: TestOverlayRoute):
                    KhonshuFragmentTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuFragmentTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuFragmentTestComponentProvider :
                ComponentProvider<TestOverlayRoute, KhonshuFragmentTestComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestOverlayRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuFragmentTestComponent = componentFromParentRoute(route.destinationId, route, executor,
                  context, TestParentRoute::class) { parentComponent:
                  KhonshuFragmentTestComponent.ParentComponent, savedStateHandle, testOverlayRoute ->
                parentComponent.khonshuFragmentTestComponentFactory().create(savedStateHandle, testOverlayRoute)
              }
            }

            @Module
            @ContributesTo(TestOverlayRoute::class)
            public interface KhonshuFragmentTestModule {
              @Multibinds
              @ForScope(TestOverlayRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuFragmentTestFragment : DialogFragment() {
              private lateinit var khonshuFragmentTestComponent: KhonshuFragmentTestComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuFragmentTestComponent.isInitialized) {
                  val testOverlayRoute = requireRoute<TestOverlayRoute>()
                  val executor = findNavigationExecutor()
                  khonshuFragmentTestComponent = KhonshuFragmentTestComponentProvider.provide(testOverlayRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuFragmentTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuFragmentTest(khonshuFragmentTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuFragmentTest(component: KhonshuFragmentTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuFragmentTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = DialogDestination<TestOverlayRoute,
                  KhonshuFragmentTestFragment>(KhonshuFragmentTestComponentProvider)
            }

        """.trimIndent()

        test(dialogFragment, "com/test/FragmentTest.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData with Composable Depedencies`() {
        val withInjectedParameters = data.copy(
            baseName = "FragmentTest2",
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
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.fragment.ComposeFragmentDestination
            import com.test.destination.TestDestinationScope
            import com.test.other.TestClass2
            import com.test.parent.TestParentRoute

            @ComposeFragmentDestination(
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
              test: TestClass2,
              testSet: Set<String>,
              testMap: Map<String, Int>,
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.destination.TestDestinationScope
            import com.test.other.TestClass2
            import com.test.parent.TestParentRoute
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.String
            import kotlin.collections.Map
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuFragmentTest2Component : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val navEventNavigator: NavEventNavigator

              public val testClass: TestClass

              public val test: TestClass2

              public val testSet: Set<String>

              public val testMap: Map<String, Int>

              @get:ForScope(TestRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle,
                    @BindsInstance testRoute: TestRoute): KhonshuFragmentTest2Component
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuFragmentTest2ComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuFragmentTest2ComponentProvider :
                ComponentProvider<TestRoute, KhonshuFragmentTest2Component> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuFragmentTest2Component = componentFromParentRoute(route.destinationId, route, executor,
                  context, TestParentRoute::class) { parentComponent:
                  KhonshuFragmentTest2Component.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuFragmentTest2ComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuFragmentTest2Module {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuFragmentTest2Fragment : Fragment() {
              private lateinit var khonshuFragmentTest2Component: KhonshuFragmentTest2Component

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuFragmentTest2Component.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuFragmentTest2Component = KhonshuFragmentTest2ComponentProvider.provide(testRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuFragmentTest2Component.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuFragmentTest2(khonshuFragmentTest2Component)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuFragmentTest2(component: KhonshuFragmentTest2Component) {
              val testClass = remember { component.testClass }
              val test = remember { component.test }
              val testSet = remember { component.testSet }
              val testMap = remember { component.testMap }
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test2(
                  testClass = testClass,
                  test = test,
                  testSet = testSet,
                  testMap = testMap,
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuFragmentTest2NavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuFragmentTest2Fragment>(KhonshuFragmentTest2ComponentProvider)
            }

        """.trimIndent()

        test(withInjectedParameters, "com/test/FragmentTest2.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData without sendAction`() {
        val withoutSendAction = data.copy(
            sendActionParameter = null,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.fragment.ComposeFragmentDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @ComposeFragmentDestination(
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
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuFragmentTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val navEventNavigator: NavEventNavigator

              @get:ForScope(TestRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle,
                    @BindsInstance testRoute: TestRoute): KhonshuFragmentTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuFragmentTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuFragmentTestComponentProvider :
                ComponentProvider<TestRoute, KhonshuFragmentTestComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuFragmentTestComponent = componentFromParentRoute(route.destinationId, route, executor,
                  context, TestParentRoute::class) { parentComponent:
                  KhonshuFragmentTestComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuFragmentTestComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuFragmentTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuFragmentTestFragment : Fragment() {
              private lateinit var khonshuFragmentTestComponent: KhonshuFragmentTestComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuFragmentTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuFragmentTestComponent = KhonshuFragmentTestComponentProvider.provide(testRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuFragmentTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuFragmentTest(khonshuFragmentTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuFragmentTest(component: KhonshuFragmentTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuFragmentTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuFragmentTestFragment>(KhonshuFragmentTestComponentProvider)
            }

        """.trimIndent()

        test(withoutSendAction, "com/test/FragmentTest.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData without state`() {
        val withoutSendAction = data.copy(
            stateParameter = null,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.fragment.ComposeFragmentDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @ComposeFragmentDestination(
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
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuFragmentTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val navEventNavigator: NavEventNavigator

              @get:ForScope(TestRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle,
                    @BindsInstance testRoute: TestRoute): KhonshuFragmentTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuFragmentTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuFragmentTestComponentProvider :
                ComponentProvider<TestRoute, KhonshuFragmentTestComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuFragmentTestComponent = componentFromParentRoute(route.destinationId, route, executor,
                  context, TestParentRoute::class) { parentComponent:
                  KhonshuFragmentTestComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuFragmentTestComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuFragmentTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuFragmentTestFragment : Fragment() {
              private lateinit var khonshuFragmentTestComponent: KhonshuFragmentTestComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuFragmentTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuFragmentTestComponent = KhonshuFragmentTestComponentProvider.provide(testRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuFragmentTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuFragmentTest(khonshuFragmentTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuFragmentTest(component: KhonshuFragmentTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuFragmentTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuFragmentTestFragment>(KhonshuFragmentTestComponentProvider)
            }

        """.trimIndent()

        test(withoutSendAction, "com/test/FragmentTest.kt", source, expected)
    }
}
