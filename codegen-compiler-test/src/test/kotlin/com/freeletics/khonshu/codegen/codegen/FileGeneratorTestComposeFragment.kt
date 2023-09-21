@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.fragment.DestinationType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class FileGeneratorTestComposeFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        parentScopeIsRoute = true,
        destinationType = DestinationType.SCREEN,
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = ComposeFragmentData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        fragmentBaseClass = ClassName("androidx.fragment.app", "Fragment"),
        navigation = null,
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
            import com.freeletics.khonshu.codegen.fragment.ComposeFragment
            import com.test.parent.TestParentScope

            @ComposeFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
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
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(TestScreen::class)
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestFragment : Fragment() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  khonshuTestComponent = component(this, requireContext(), TestParentScope::class, arguments) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle,
                      argumentsForComponent ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuTest(khonshuTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
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

        """.trimIndent()

        test(data, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData with navigation`() {
        val withNavigation = data.copy(
            scope = navigation.route,
            parentScope = ClassName("com.test.parent", "TestParentRoute"),
            navigation = navigation,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.fragment.ComposeDestination
            import com.freeletics.khonshu.codegen.fragment.DestinationType
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @ComposeDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
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
            public interface KhonshuTestComponent : Closeable {
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
                    @BindsInstance testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestComponentProvider : ComponentProvider<TestRoute, KhonshuTestComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuTestComponent = componentFromParentRoute(route.destinationId, route, executor, context,
                  TestParentRoute::class) { parentComponent: KhonshuTestComponent.ParentComponent,
                  savedStateHandle, testRoute ->
                parentComponent.khonshuTestComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestFragment : Fragment() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuTestComponent = KhonshuTestComponentProvider.provide(testRoute, executor,
                      requireContext())

                  handleNavigation(this, khonshuTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuTest(khonshuTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
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
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuTestFragment>(KhonshuTestComponentProvider)
            }

        """.trimIndent()

        test(withNavigation, "com/test/Test.kt", source, expected)
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
            import com.freeletics.khonshu.codegen.fragment.ComposeDestination
            import com.freeletics.khonshu.codegen.fragment.DestinationType

            @ComposeDestination(
              route = TestRoute::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
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
            public interface KhonshuTestComponent : Closeable {
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
                    @BindsInstance testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestComponentProvider : ComponentProvider<TestRoute, KhonshuTestComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuTestComponent = component(route.destinationId, route, executor, context,
                  AppScope::class) { parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle,
                  testRoute ->
                parentComponent.khonshuTestComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestFragment : Fragment() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuTestComponent = KhonshuTestComponentProvider.provide(testRoute, executor,
                      requireContext())

                  handleNavigation(this, khonshuTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuTest(khonshuTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
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
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuTestFragment>(KhonshuTestComponentProvider)
            }

        """.trimIndent()

        test(withDefaultValues, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData, dialog fragment`() {
        val dialogFragment = data.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment"),
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.fragment.app.DialogFragment
            import com.freeletics.khonshu.codegen.fragment.ComposeFragment
            import com.test.parent.TestParentScope

            @ComposeFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
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
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(TestScreen::class)
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestFragment : DialogFragment() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  khonshuTestComponent = component(this, requireContext(), TestParentScope::class, arguments) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle,
                      argumentsForComponent ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuTest(khonshuTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
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

        """.trimIndent()

        test(dialogFragment, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData with Composable Depedencies`() {
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
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.fragment.ComposeFragment
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope

            @ComposeFragment(
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
              test: TestClass2,
              testSet: Set<String>,
              testMap: Map<String, Int>,
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

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
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.String
            import kotlin.collections.Map
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTest2Component : Closeable {
              public val testStateMachine: TestStateMachine

              public val testClass: TestClass

              public val test: TestClass2

              public val testSet: Set<String>

              public val testMap: Map<String, Int>

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(TestScreen::class)
                    arguments: Bundle): KhonshuTest2Component
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTest2ComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTest2Module {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTest2Fragment : Fragment() {
              private lateinit var khonshuTest2Component: KhonshuTest2Component

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTest2Component.isInitialized) {
                  val arguments = requireArguments()
                  khonshuTest2Component = component(this, requireContext(), TestParentScope::class, arguments) {
                      parentComponent: KhonshuTest2Component.ParentComponent, savedStateHandle,
                      argumentsForComponent ->
                    parentComponent.khonshuTest2ComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuTest2(khonshuTest2Component)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest2(component: KhonshuTest2Component) {
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

        """.trimIndent()

        test(withInjectedParameters, "com/test/Test2.kt", source, expected)
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
            import com.freeletics.khonshu.codegen.fragment.ComposeFragment
            import com.test.parent.TestParentScope

            @ComposeFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
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
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(TestScreen::class)
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestFragment : Fragment() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  khonshuTestComponent = component(this, requireContext(), TestParentScope::class, arguments) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle,
                      argumentsForComponent ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuTest(khonshuTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                )
              }
            }

        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
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
            import com.freeletics.khonshu.codegen.fragment.ComposeFragment
            import com.test.parent.TestParentScope

            @ComposeFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
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
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(TestScreen::class)
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestFragment : Fragment() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  khonshuTestComponent = component(this, requireContext(), TestParentScope::class, arguments) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle,
                      argumentsForComponent ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    KhonshuTest(khonshuTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
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

        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }
}
