package com.freeletics.mad.codegen.codegen

import com.freeletics.mad.codegen.AppScope
import com.freeletics.mad.codegen.ComposableParameter
import com.freeletics.mad.codegen.ComposeFragmentData
import com.freeletics.mad.codegen.NavEntryData
import com.freeletics.mad.codegen.Navigation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import org.junit.Test

internal class FileGeneratorTestComposeFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "SCREEN",
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
        navEntryData = null,
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

    private val navEntryData = NavEntryData(
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        navigation = navigation,
    )

    @Test
    fun `generates code for ComposeFragmentData`() {
        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.codegen.fragment.ComposeFragment
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): MadTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestFragment : Fragment() {
              private lateinit var madTestComponent: MadTestComponent
            
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  madTestComponent = component(TestParentScope::class, arguments) { parentComponent:
                      MadTestComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                    parentComponent.madTestComponentFactory().create(savedStateHandle, argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest(madTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest(component: MadTestComponent) {
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
            navigation = navigation,
        )

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.codegen.fragment.ComposeDestination
            import com.freeletics.mad.codegen.fragment.DestinationType
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            
            @ComposeDestination(
              route = TestRoute::class,
              parentScope = TestParentScope::class,
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.freeletics.mad.navigation.NavEventNavigator
            import com.freeletics.mad.navigation.fragment.NavDestination
            import com.freeletics.mad.navigation.fragment.ScreenDestination
            import com.freeletics.mad.navigation.fragment.handleNavigation
            import com.freeletics.mad.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
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
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): MadTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface MadTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestFragment : Fragment() {
              private lateinit var madTestComponent: MadTestComponent
            
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  madTestComponent = component(TestParentScope::class, TestDestinationScope::class, testRoute) {
                      parentComponent: MadTestComponent.ParentComponent, savedStateHandle,
                      testRouteForComponent ->
                    parentComponent.madTestComponentFactory().create(savedStateHandle, testRouteForComponent)
                  }
            
                  handleNavigation(this, madTestComponent.navEventNavigator)
                }
            
                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest(madTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest(component: MadTestComponent) {
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
            
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object MadTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  MadTestFragment>()
            }
            
        """.trimIndent()

        test(withNavigation, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData, with navigation and navEntry`() {
        val withNavEntry = data.copy(
            scope = navigation.route,
            navigation = navigation,
            navEntryData = navEntryData,
        )

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.codegen.fragment.ComposeDestination
            import com.freeletics.mad.codegen.fragment.DestinationType
            import com.freeletics.mad.codegen.NavEntryComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            
            @ComposeDestination(
              route = TestRoute::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
              destinationScope = TestDestinationScope::class,
            )
            @NavEntryComponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

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
            import com.freeletics.mad.codegen.NavEntry
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.NavDestinationComponent
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.`internal`.navEntryComponent
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.freeletics.mad.navigation.NavEventNavigator
            import com.freeletics.mad.navigation.`internal`.InternalNavigationApi
            import com.freeletics.mad.navigation.`internal`.NavigationExecutor
            import com.freeletics.mad.navigation.fragment.NavDestination
            import com.freeletics.mad.navigation.fragment.ScreenDestination
            import com.freeletics.mad.navigation.fragment.handleNavigation
            import com.freeletics.mad.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesMultibinding
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): MadTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface MadTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class MadTestFragment : Fragment() {
              private lateinit var madTestComponent: MadTestComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  madTestComponent = component(TestParentScope::class, TestDestinationScope::class, testRoute) {
                      parentComponent: MadTestComponent.ParentComponent, savedStateHandle,
                      testRouteForComponent ->
                    parentComponent.madTestComponentFactory().create(savedStateHandle, testRouteForComponent)
                  }

                  handleNavigation(this, madTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest(madTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest(component: MadTestComponent) {
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

            @Module
            @ContributesTo(TestDestinationScope::class)
            public object MadTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  MadTestFragment>()
            }

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestScreenNavEntryComponent : Closeable {
              @get:NavEntry(TestScreen::class)
              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestScreen::class)
                    testRoute: TestRoute): MadTestScreenNavEntryComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestScreenNavEntryModule {
              @Multibinds
              @NavEntry(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalCodegenApi::class, InternalNavigationApi::class)
              override fun retrieve(executor: NavigationExecutor, context: Context): Any =
                  navEntryComponent(TestRoute::class, executor, context, TestParentScope::class,
                  TestDestinationScope::class) { parentComponent:
                  MadTestScreenNavEntryComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.madTestScreenNavEntryComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalCodegenApi::class)
            public interface MadTestScreenNavEntryNavDestinationComponent : NavDestinationComponent

        """.trimIndent()

        test(withNavEntry, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData with default values`() {
        val navigation = navigation.copy(destinationScope = AppScope::class.asClassName())
        val withDefaultValues = data.copy(
            scope = navigation.route,
            parentScope = AppScope::class.asClassName(),
            navigation = navigation,
            navEntryData = navEntryData.copy(
                parentScope = AppScope::class.asClassName(),
                navigation = navigation,
            ),
        )

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.codegen.fragment.ComposeDestination
            import com.freeletics.mad.codegen.fragment.DestinationType
            import com.freeletics.mad.codegen.NavEntryComponent
            
            @ComposeDestination(
              route = TestRoute::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
            )
            @NavEntryComponent(
              scope = TestScreen::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

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
            import com.freeletics.mad.codegen.AppScope
            import com.freeletics.mad.codegen.NavEntry
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.NavDestinationComponent
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.`internal`.navEntryComponent
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.freeletics.mad.navigation.NavEventNavigator
            import com.freeletics.mad.navigation.`internal`.InternalNavigationApi
            import com.freeletics.mad.navigation.`internal`.NavigationExecutor
            import com.freeletics.mad.navigation.fragment.NavDestination
            import com.freeletics.mad.navigation.fragment.ScreenDestination
            import com.freeletics.mad.navigation.fragment.handleNavigation
            import com.freeletics.mad.navigation.fragment.requireRoute
            import com.squareup.anvil.annotations.ContributesMultibinding
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = AppScope::class,
            )
            public interface MadTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): MadTestComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun madTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface MadTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class MadTestFragment : Fragment() {
              private lateinit var madTestComponent: MadTestComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  madTestComponent = component(AppScope::class, AppScope::class, testRoute) { parentComponent:
                      MadTestComponent.ParentComponent, savedStateHandle, testRouteForComponent ->
                    parentComponent.madTestComponentFactory().create(savedStateHandle, testRouteForComponent)
                  }

                  handleNavigation(this, madTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest(madTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest(component: MadTestComponent) {
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

            @Module
            @ContributesTo(AppScope::class)
            public object MadTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  MadTestFragment>()
            }

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = AppScope::class,
            )
            public interface MadTestScreenNavEntryComponent : Closeable {
              @get:NavEntry(TestScreen::class)
              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestScreen::class)
                    testRoute: TestRoute): MadTestScreenNavEntryComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun madTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestScreenNavEntryModule {
              @Multibinds
              @NavEntry(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              AppScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalCodegenApi::class, InternalNavigationApi::class)
              override fun retrieve(executor: NavigationExecutor, context: Context): Any =
                  navEntryComponent(TestRoute::class, executor, context, AppScope::class, AppScope::class) {
                  parentComponent: MadTestScreenNavEntryComponent.ParentComponent, savedStateHandle,
                  testRoute ->
                parentComponent.madTestScreenNavEntryComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @ContributesTo(AppScope::class)
            @OptIn(InternalCodegenApi::class)
            public interface MadTestScreenNavEntryNavDestinationComponent : NavDestinationComponent

        """.trimIndent()

        test(withDefaultValues, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData, dialog fragment`() {
        val dialogFragment = data.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment"),
        )

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import androidx.fragment.app.DialogFragment
            import com.freeletics.mad.codegen.fragment.ComposeFragment
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): MadTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestFragment : DialogFragment() {
              private lateinit var madTestComponent: MadTestComponent
            
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  madTestComponent = component(TestParentScope::class, arguments) { parentComponent:
                      MadTestComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                    parentComponent.madTestComponentFactory().create(savedStateHandle, argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest(madTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest(component: MadTestComponent) {
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

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.codegen.fragment.ComposeFragment
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
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
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTest2Component : Closeable {
              public val testStateMachine: TestStateMachine

              public val testClass: TestClass

              public val test: TestClass2
            
              public val testSet: Set<String>

              public val testMap: Map<String, Int>

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): MadTest2Component
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTest2ComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTest2Module {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTest2Fragment : Fragment() {
              private lateinit var madTest2Component: MadTest2Component
            
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTest2Component.isInitialized) {
                  val arguments = requireArguments()
                  madTest2Component = component(TestParentScope::class, arguments) { parentComponent:
                      MadTest2Component.ParentComponent, savedStateHandle, argumentsForComponent ->
                    parentComponent.madTest2ComponentFactory().create(savedStateHandle, argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest2(madTest2Component)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest2(component: MadTest2Component) {
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

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.codegen.fragment.ComposeFragment
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): MadTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestFragment : Fragment() {
              private lateinit var madTestComponent: MadTestComponent
            
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  madTestComponent = component(TestParentScope::class, arguments) { parentComponent:
                      MadTestComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                    parentComponent.madTestComponentFactory().create(savedStateHandle, argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest(madTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest(component: MadTestComponent) {
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

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.codegen.fragment.ComposeFragment
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.asComposeState
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): MadTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestFragment : Fragment() {
              private lateinit var madTestComponent: MadTestComponent
            
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  madTestComponent = component(TestParentScope::class, arguments) { parentComponent:
                      MadTestComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                    parentComponent.madTestComponentFactory().create(savedStateHandle, argumentsForComponent)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                  setContent {
                    MadTest(madTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun MadTest(component: MadTestComponent) {
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
