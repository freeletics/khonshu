package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.ComposableParameter
import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.Navigation
import com.squareup.kotlinpoet.ClassName
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
        composableParameter = emptyList()
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
            import com.freeletics.mad.whetstone.fragment.ComposeFragment
            import com.test.parent.TestParentScope
            
            @ComposeFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
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
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              public override fun close(): Unit {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : Fragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  whetstoneTestComponent = component(TestParentScope::class, arguments) { parentComponent:
                      WhetstoneTestComponent.ParentComponent, savedStateHandle, arguments ->
                    parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, arguments)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  setContent {
                    WhetstoneTest(whetstoneTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun WhetstoneTest(component: WhetstoneTestComponent): Unit {
              val stateMachine = component.testStateMachine
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
            import com.freeletics.mad.whetstone.fragment.ComposeDestination
            import com.freeletics.mad.whetstone.fragment.DestinationType
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
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.component
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              public override fun close(): Unit {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : Fragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  whetstoneTestComponent = component(TestParentScope::class, TestDestinationScope::class,
                      testRoute) { parentComponent: WhetstoneTestComponent.ParentComponent, savedStateHandle,
                      testRoute ->
                    parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)
                  }
            
                  handleNavigation(this, whetstoneTestComponent.navEventNavigator)
                }
            
                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  setContent {
                    WhetstoneTest(whetstoneTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun WhetstoneTest(component: WhetstoneTestComponent): Unit {
              val stateMachine = component.testStateMachine
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
            public object WhetstoneTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  WhetstoneTestFragment>()
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
            import com.freeletics.mad.whetstone.fragment.ComposeDestination
            import com.freeletics.mad.whetstone.fragment.DestinationType
            import com.freeletics.mad.whetstone.NavEntryComponent
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
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.NavigationExecutor
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.`internal`.navEntryComponent
            import com.freeletics.mad.whetstone.fragment.`internal`.component
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              public override fun close(): Unit {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : Fragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  whetstoneTestComponent = component(TestParentScope::class, TestDestinationScope::class,
                      testRoute) { parentComponent: WhetstoneTestComponent.ParentComponent, savedStateHandle,
                      testRoute ->
                    parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)
                  }

                  handleNavigation(this, whetstoneTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  setContent {
                    WhetstoneTest(whetstoneTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun WhetstoneTest(component: WhetstoneTestComponent): Unit {
              val stateMachine = component.testStateMachine
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
            public object WhetstoneTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  WhetstoneTestFragment>()
            }

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestScreenNavEntryComponent : Closeable {
              @get:NavEntry(TestScreen::class)
              public val closeables: Set<Closeable>
    
              public override fun close(): Unit {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestScreen::class)
                    testRoute: TestRoute): WhetstoneTestScreenNavEntryComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestScreenNavEntryModule {
              @Multibinds
              @NavEntry(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              public override fun retrieve(executor: NavigationExecutor, context: Context): Any =
                  navEntryComponent(TestRoute::class, executor, context, TestParentScope::class,
                  TestDestinationScope::class) { parentComponent:
                  WhetstoneTestScreenNavEntryComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.whetstoneTestScreenNavEntryComponentFactory().create(savedStateHandle,
                    testRoute)
              }
            }

            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface WhetstoneTestScreenNavEntryDestinationComponent : DestinationComponent

        """.trimIndent()

        test(withNavEntry, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeFragmentData, dialog fragment`() {
        val dialogFragment = data.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment")
        )

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import androidx.fragment.app.DialogFragment
            import com.freeletics.mad.whetstone.fragment.ComposeFragment
            import com.test.parent.TestParentScope
            
            @ComposeFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              fragmentBaseClass = DialogFragment::class,
            )
            @Composable
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
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              public override fun close(): Unit {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : DialogFragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  whetstoneTestComponent = component(TestParentScope::class, arguments) { parentComponent:
                      WhetstoneTestComponent.ParentComponent, savedStateHandle, arguments ->
                    parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, arguments)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  setContent {
                    WhetstoneTest(whetstoneTestComponent)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun WhetstoneTest(component: WhetstoneTestComponent): Unit {
              val stateMachine = component.testStateMachine
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
                    className = ClassName("com.test", "TestClass"),
                ),
                ComposableParameter(
                    name = "test",
                    className = ClassName("com.test.other", "TestClass2"),
                )
            )
        )

        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.mad.whetstone.fragment.ComposeFragment
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            
            @ComposeFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            public fun Test2(
              state: TestState,
              sendAction: (TestAction) -> Unit,
                testClass: TestClass,
                test: TestClass2,
            ) {}
        """.trimIndent()

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTest2Component : Closeable {
              public val testStateMachine: TestStateMachine

              public val testClass: TestClass

              public val testClass2: TestClass2

              public val closeables: Set<Closeable>
    
              public override fun close(): Unit {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): WhetstoneTest2Component
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTest2ComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTest2Module {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTest2Fragment : Fragment() {
              private lateinit var whetstoneTest2Component: WhetstoneTest2Component
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTest2Component.isInitialized) {
                  val arguments = requireArguments()
                  whetstoneTest2Component = component(TestParentScope::class, arguments) { parentComponent:
                      WhetstoneTest2Component.ParentComponent, savedStateHandle, arguments ->
                    parentComponent.whetstoneTest2ComponentFactory().create(savedStateHandle, arguments)
                  }
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  setContent {
                    WhetstoneTest2(whetstoneTest2Component)
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun WhetstoneTest2(component: WhetstoneTest2Component): Unit {
              val testClass = component.testClass
              val testClass2 = component.testClass2
              val stateMachine = component.testStateMachine
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test2(
                  testClass = testClass,
                  test = testClass2,
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }
            
        """.trimIndent()

        test(withInjectedParameters, "com/test/Test2.kt", source, expected)
    }
}
