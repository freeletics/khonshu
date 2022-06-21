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

    private val full = ComposeScreenData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        dependencies = ClassName("com.test", "TestDependencies"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = navigation,
        coroutinesEnabled = true,
        rxJavaEnabled = true,
    )

    @Test
    fun `generates code for ComposeScreenData`() {
        val actual = FileGenerator().generate(full).toString()

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
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import io.reactivex.disposables.CompositeDisposable
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
              modules = [ComposeProviderValueModule::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                ): RetainedTestComponent
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

                @Provides
                @IntoSet
                public fun bindCompositeDisposable(compositeDisposable: CompositeDisposable): Closeable =
                    Closeable {
                  compositeDisposable.clear()
                }
            
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCoroutineScope(): CoroutineScope = MainScope()

                @Provides
                @IntoSet
                public fun bindCoroutineScope(coroutineScope: CoroutineScope): Closeable = Closeable {
                  coroutineScope.cancel()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute)

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
    fun `generates code for ComposeScreenData with destination`() {
        val withDestination = full.copy(navigation = navigation.copy(destinationType = "SCREEN"))
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
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import io.reactivex.disposables.CompositeDisposable
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
              modules = [ComposeProviderValueModule::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                ): RetainedTestComponent
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

                @Provides
                @IntoSet
                public fun bindCompositeDisposable(compositeDisposable: CompositeDisposable): Closeable =
                    Closeable {
                  compositeDisposable.clear()
                }
            
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCoroutineScope(): CoroutineScope = MainScope()

                @Provides
                @IntoSet
                public fun bindCoroutineScope(coroutineScope: CoroutineScope): Closeable = Closeable {
                  coroutineScope.cancel()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute)

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

    @Test
    fun `generates code for ComposeScreenData, no navigation`() {
        val noNavigation = full.copy(navigation = null)
        val actual = FileGenerator().generate(noNavigation).toString()

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
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import io.reactivex.disposables.CompositeDisposable
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
              modules = [ComposeProviderValueModule::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                ): RetainedTestComponent
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

                @Provides
                @IntoSet
                public fun bindCompositeDisposable(compositeDisposable: CompositeDisposable): Closeable =
                    Closeable {
                  compositeDisposable.clear()
                }
            
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCoroutineScope(): CoroutineScope = MainScope()

                @Provides
                @IntoSet
                public fun bindCoroutineScope(coroutineScope: CoroutineScope): Closeable = Closeable {
                  coroutineScope.cancel()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, arguments)

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
    fun `generates code for ComposeScreenData, without coroutines`() {
        val withoutCoroutines = full.copy(coroutinesEnabled = false)
        val actual = FileGenerator().generate(withoutCoroutines).toString()

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
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import io.reactivex.disposables.CompositeDisposable
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
              modules = [ComposeProviderValueModule::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                ): RetainedTestComponent
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

                @Provides
                @IntoSet
                public fun bindCompositeDisposable(compositeDisposable: CompositeDisposable): Closeable =
                    Closeable {
                  compositeDisposable.clear()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute)

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
    fun `generates code for ComposeScreenData, without rxjava`() {
        val withoutRxJava = full.copy(rxJavaEnabled = false)
        val actual = FileGenerator().generate(withoutRxJava).toString()

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
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
              modules = [ComposeProviderValueModule::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                ): RetainedTestComponent
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface RetainedTestModule {
              @Multibinds
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestScreen::class)
                public fun provideCoroutineScope(): CoroutineScope = MainScope()

                @Provides
                @IntoSet
                public fun bindCoroutineScope(coroutineScope: CoroutineScope): Closeable = Closeable {
                  coroutineScope.cancel()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute)

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
    fun `generates code for ComposeScreenData, without rxjava and coroutines`() {
        val withoutRxJava = full.copy(rxJavaEnabled = false, coroutinesEnabled = false)
        val actual = FileGenerator().generate(withoutRxJava).toString()

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
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
              modules = [ComposeProviderValueModule::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
    
              public val closeables: Set<Closeable>

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                ): RetainedTestComponent
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
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute)

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

}
