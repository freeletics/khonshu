package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.Navigation
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
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
        FileGenerator().generate(full).toString() shouldBe """
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
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
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
              modules = [ComposeProviderValueModule::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                  @BindsInstance compositeDisposable: CompositeDisposable,
                  @BindsInstance coroutineScope: CoroutineScope
                ): RetainedTestComponent
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              private val scope: CoroutineScope = MainScope()

              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute,
                  disposable, scope)

              public override fun onCleared(): Unit {
                disposable.clear()
                scope.cancel()
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(testRoute: TestRoute): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                TestViewModel(dependencies, handle, testRoute)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
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
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ComposeScreenData with destination`() {
        val withDestination = full.copy(navigation = navigation.copy(destinationType = "SCREEN"))

        FileGenerator().generate(withDestination).toString() shouldBe """
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
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import io.reactivex.disposables.CompositeDisposable
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
              modules = [ComposeProviderValueModule::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                  @BindsInstance compositeDisposable: CompositeDisposable,
                  @BindsInstance coroutineScope: CoroutineScope
                ): RetainedTestComponent
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              private val scope: CoroutineScope = MainScope()

              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute,
                  disposable, scope)

              public override fun onCleared(): Unit {
                disposable.clear()
                scope.cancel()
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(testRoute: TestRoute): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                TestViewModel(dependencies, handle, testRoute)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
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
    }

    @Test
    fun `generates code for ComposeScreenData, no navigation`() {
        val noNavigation = full.copy(navigation = null)

        FileGenerator().generate(noNavigation).toString() shouldBe """
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
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
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
              modules = [ComposeProviderValueModule::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance compositeDisposable: CompositeDisposable,
                  @BindsInstance coroutineScope: CoroutineScope
                ): RetainedTestComponent
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              private val scope: CoroutineScope = MainScope()

              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, arguments,
                  disposable, scope)

              public override fun onCleared(): Unit {
                disposable.clear()
                scope.cancel()
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(arguments: Bundle): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                TestViewModel(dependencies, handle, arguments)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
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
    }

    @Test
    fun `generates code for ComposeScreenData, without coroutines`() {
        val withoutCoroutines = full.copy(coroutinesEnabled = false)

        FileGenerator().generate(withoutCoroutines).toString() shouldBe """
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
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
              modules = [ComposeProviderValueModule::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                  @BindsInstance compositeDisposable: CompositeDisposable
                ): RetainedTestComponent
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute,
                  disposable)

              public override fun onCleared(): Unit {
                disposable.clear()
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(testRoute: TestRoute): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                TestViewModel(dependencies, handle, testRoute)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
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
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ComposeScreenData, without rxjava`() {
        val withoutRxJava = full.copy(rxJavaEnabled = false)

        FileGenerator().generate(withoutRxJava).toString() shouldBe """
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
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.compose.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
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
              modules = [ComposeProviderValueModule::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                  @BindsInstance coroutineScope: CoroutineScope
                ): RetainedTestComponent
              }
            }

            @InternalWhetstoneApi
            internal class TestViewModel(
              dependencies: TestDependencies,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute
            ) : ViewModel() {
              private val scope: CoroutineScope = MainScope()

              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, testRoute, scope)

              public override fun onCleared(): Unit {
                scope.cancel()
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(testRoute: TestRoute): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                TestViewModel(dependencies, handle, testRoute)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
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
            
        """.trimIndent()
    }

}
