package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.ComposableParameter
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.Navigation
import com.squareup.kotlinpoet.ClassName
import org.junit.Test

internal class FileGeneratorTestCompose {

    private val navigation = Navigation.Compose(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "NONE",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = ComposeScreenData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
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
    fun `generates code for ComposeScreenData`() {
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.CloseableComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberComponent
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
            public interface WhetstoneTestComponent : CloseableComponent {
              public val testStateMachine: TestStateMachine

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

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun WhetstoneTest(arguments: Bundle): Unit {
              val component = rememberComponent(TestParentScope::class, arguments) { parentComponent:
                  WhetstoneTestComponent.ParentComponent, savedStateHandle, arguments ->
                parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, arguments)
              }

              WhetstoneTest(component)
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

        test(data, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with navigation`() {
        val withNavigation = data.copy(navigation = navigation)

        val expected = """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.`internal`.CloseableComponent
            import com.freeletics.mad.navigator.compose.NavigationSetup
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberComponent
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.destination.TestDestinationScope
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
            public interface WhetstoneTestComponent : CloseableComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

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
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun WhetstoneTest(testRoute: TestRoute): Unit {
              val component = rememberComponent(TestParentScope::class, TestDestinationScope::class, testRoute)
                  { parentComponent: WhetstoneTestComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)
              }

              NavigationSetup(component.navEventNavigator)

              WhetstoneTest(component)
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

        test(withNavigation, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with navigation and destination`() {
        val withDestination = data.copy(navigation = navigation.copy(destinationType = "SCREEN"))

        val expected = """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.`internal`.CloseableComponent
            import com.freeletics.mad.navigator.compose.NavDestination
            import com.freeletics.mad.navigator.compose.NavigationSetup
            import com.freeletics.mad.navigator.compose.ScreenDestination
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberComponent
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
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent : CloseableComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

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
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun WhetstoneTest(testRoute: TestRoute): Unit {
              val component = rememberComponent(TestParentScope::class, TestDestinationScope::class, testRoute)
                  { parentComponent: WhetstoneTestComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)
              }

              NavigationSetup(component.navEventNavigator)

              WhetstoneTest(component)
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
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute> {
                WhetstoneTest(it)
              }
            }
            
        """.trimIndent()

        test(withDestination, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with navigation, destination and navEntry`() {
        val withDestination = data.copy(
            navigation = navigation.copy(destinationType = "SCREEN"),
            navEntryData = navEntryData
        )

        val expected = """
            package com.test

            import android.content.Context
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.`internal`.CloseableComponent
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.NavigationExecutor
            import com.freeletics.mad.navigator.compose.NavDestination
            import com.freeletics.mad.navigator.compose.NavigationSetup
            import com.freeletics.mad.navigator.compose.ScreenDestination
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.`internal`.navEntryComponent
            import com.freeletics.mad.whetstone.compose.`internal`.rememberComponent
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
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent : CloseableComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

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
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun WhetstoneTest(testRoute: TestRoute): Unit {
              val component = rememberComponent(TestParentScope::class, TestDestinationScope::class, testRoute)
                  { parentComponent: WhetstoneTestComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)
              }

              NavigationSetup(component.navEventNavigator)

              WhetstoneTest(component)
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
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute> {
                WhetstoneTest(it)
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestScreenNavEntryComponent : CloseableComponent {
              @get:NavEntry(TestScreen::class)
              public override val closeables: Set<Closeable>

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

        test(withDestination, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with Composable Dependencies`() {
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

        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.CloseableComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberComponent
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
            public interface WhetstoneTest2Component : CloseableComponent {
              public val testStateMachine: TestStateMachine

              public val testClass: TestClass

              public val testClass2: TestClass2

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

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun WhetstoneTest2(arguments: Bundle): Unit {
              val component = rememberComponent(TestParentScope::class, arguments) { parentComponent:
                  WhetstoneTest2Component.ParentComponent, savedStateHandle, arguments ->
                parentComponent.whetstoneTest2ComponentFactory().create(savedStateHandle, arguments)
              }

              WhetstoneTest2(component)
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

        test(withInjectedParameters, expected)
    }
}
