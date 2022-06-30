package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.Navigation
import com.squareup.kotlinpoet.ClassName
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class FileGeneratorTestCompose {

    private val navigation = Navigation.Compose(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "NONE",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
        navEntryData = null,
    )

    private val data = ComposeScreenData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = null,
    )

    @Test
    fun `generates code for ComposeScreenData`() {
        val actual = FileGenerator().generate(data).toString()

        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModel
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
              modules = [ComposeProviderValueModule::class],
            )
            public interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): RetainedTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun retainedTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              parentComponent: RetainedTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  parentComponent.retainedTestComponentFactory().create(savedStateHandle, arguments)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(arguments: Bundle): Unit {
              val viewModel = rememberViewModel(TestParentScope::class, arguments, ::TestViewModel)
              val component = viewModel.component

              TestScreen(component)
            }
            
            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun TestScreen(component: RetainedTestComponent): Unit {
              val providedValues = component.providedValues
              CompositionLocalProvider(*providedValues.toTypedArray()) {
                val stateMachine = component.testStateMachine
                val state = stateMachine.asComposeState()
                val currentState = state.value
                if (currentState != null) {
                  val scope = rememberCoroutineScope()
                  Test(currentState) { action ->
                    scope.launch { stateMachine.dispatch(action) }
                  }
                }
              }
            }
            
        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates code for ComposeScreenData with navigation`() {
        val withNavigation = data.copy(navigation = navigation)
        val actual = FileGenerator().generate(withNavigation).toString()

        val expected = """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.compose.NavigationSetup
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModel
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
              modules = [ComposeProviderValueModule::class],
            )
            public interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): RetainedTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun retainedTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              parentComponent: RetainedTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  parentComponent.retainedTestComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(testRoute: TestRoute): Unit {
              val viewModel = rememberViewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                  ::TestViewModel)
              val component = viewModel.component

              NavigationSetup(component.navEventNavigator)

              TestScreen(component)
            }
            
            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun TestScreen(component: RetainedTestComponent): Unit {
              val providedValues = component.providedValues
              CompositionLocalProvider(*providedValues.toTypedArray()) {
                val stateMachine = component.testStateMachine
                val state = stateMachine.asComposeState()
                val currentState = state.value
                if (currentState != null) {
                  val scope = rememberCoroutineScope()
                  Test(currentState) { action ->
                    scope.launch { stateMachine.dispatch(action) }
                  }
                }
              }
            }
            
            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface NavEntryTestDestinationComponent : DestinationComponent
            
        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates code for ComposeScreenData with navigationd and destination`() {
        val withDestination = data.copy(navigation = navigation.copy(destinationType = "SCREEN"))
        val actual = FileGenerator().generate(withDestination).toString()

        val expected = """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.compose.NavDestination
            import com.freeletics.mad.navigator.compose.NavigationSetup
            import com.freeletics.mad.navigator.compose.ScreenDestination
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModel
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
              modules = [ComposeProviderValueModule::class],
            )
            public interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): RetainedTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun retainedTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              parentComponent: RetainedTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  parentComponent.retainedTestComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(testRoute: TestRoute): Unit {
              val viewModel = rememberViewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                  ::TestViewModel)
              val component = viewModel.component

              NavigationSetup(component.navEventNavigator)

              TestScreen(component)
            }
            
            @Composable
            @OptIn(InternalWhetstoneApi::class)
            private fun TestScreen(component: RetainedTestComponent): Unit {
              val providedValues = component.providedValues
              CompositionLocalProvider(*providedValues.toTypedArray()) {
                val stateMachine = component.testStateMachine
                val state = stateMachine.asComposeState()
                val currentState = state.value
                if (currentState != null) {
                  val scope = rememberCoroutineScope()
                  Test(currentState) { action ->
                    scope.launch { stateMachine.dispatch(action) }
                  }
                }
              }
            }
            
            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface NavEntryTestDestinationComponent : DestinationComponent
            
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object WhetstoneTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute> {
                TestScreen(it)
              }
            }
            
        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }

}
