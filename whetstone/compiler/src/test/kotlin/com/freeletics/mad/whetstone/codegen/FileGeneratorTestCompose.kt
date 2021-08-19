package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
import org.junit.Test

class FileGeneratorTestCompose {

    private val full = ComposeScreenData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        dependencies = ClassName("com.test", "TestDependencies"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = CommonData.Navigation(
            navigator = ClassName("com.test", "TestNavigator"),
            navigationHandler = ClassName("com.test.navigation", "TestNavigationHandler"),
        ),
        coroutinesEnabled = true,
        rxJavaEnabled = true,
    )

    @Test
    fun `generates code for ComposeScreenData`() {
        FileGenerator().generate(full).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
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

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

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
            public fun TestScreen(navController: NavController): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                val arguments = navController.currentBackStackEntry!!.arguments ?: Bundle.EMPTY
                TestViewModel(dependencies, handle, arguments)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
              val component = viewModel.component

              val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
              LaunchedEffect(navController, onBackPressedDispatcher, component) {
                val handler = component.testNavigationHandler
                val navigator = component.testNavigator
                handler.handle(this, navController, onBackPressedDispatcher, navigator)
              }

              val providedValues = component.providedValues
              CompositionLocalProvider(*providedValues.toTypedArray()) {
                val stateMachine = component.testStateMachine
                val state = stateMachine.state.collectAsState()
                val scope = rememberCoroutineScope()
                Test(state.value) { action ->
                  scope.launch { stateMachine.dispatch(action) }
                }
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
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.rememberViewModelProvider
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
            public fun TestScreen(navController: NavController): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                val arguments = navController.currentBackStackEntry!!.arguments ?: Bundle.EMPTY
                TestViewModel(dependencies, handle, arguments)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
              val component = viewModel.component

              val providedValues = component.providedValues
              CompositionLocalProvider(*providedValues.toTypedArray()) {
                val stateMachine = component.testStateMachine
                val state = stateMachine.state.collectAsState()
                val scope = rememberCoroutineScope()
                Test(state.value) { action ->
                  scope.launch { stateMachine.dispatch(action) }
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

            import android.os.Bundle
            import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
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

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance compositeDisposable: CompositeDisposable
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

              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, arguments,
                  disposable)

              public override fun onCleared(): Unit {
                disposable.clear()
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(navController: NavController): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                val arguments = navController.currentBackStackEntry!!.arguments ?: Bundle.EMPTY
                TestViewModel(dependencies, handle, arguments)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
              val component = viewModel.component

              val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
              LaunchedEffect(navController, onBackPressedDispatcher, component) {
                val handler = component.testNavigationHandler
                val navigator = component.testNavigator
                handler.handle(this, navController, onBackPressedDispatcher, navigator)
              }

              val providedValues = component.providedValues
              CompositionLocalProvider(*providedValues.toTypedArray()) {
                val stateMachine = component.testStateMachine
                val state = stateMachine.state.collectAsState()
                val scope = rememberCoroutineScope()
                Test(state.value) { action ->
                  scope.launch { stateMachine.dispatch(action) }
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

            import android.os.Bundle
            import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
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

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

              public val providedValues: Set<ProvidedValue<*>>

              @Component.Factory
              public interface Factory {
                public fun create(
                  dependencies: TestDependencies,
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
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
              private val scope: CoroutineScope = MainScope()

              public val component: RetainedTestComponent =
                  DaggerRetainedTestComponent.factory().create(dependencies, savedStateHandle, arguments, scope)

              public override fun onCleared(): Unit {
                scope.cancel()
              }
            }

            @Composable
            @OptIn(InternalWhetstoneApi::class)
            public fun TestScreen(navController: NavController): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                val arguments = navController.currentBackStackEntry!!.arguments ?: Bundle.EMPTY
                TestViewModel(dependencies, handle, arguments)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
              val component = viewModel.component

              val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
              LaunchedEffect(navController, onBackPressedDispatcher, component) {
                val handler = component.testNavigationHandler
                val navigator = component.testNavigator
                handler.handle(this, navController, onBackPressedDispatcher, navigator)
              }

              val providedValues = component.providedValues
              CompositionLocalProvider(*providedValues.toTypedArray()) {
                val stateMachine = component.testStateMachine
                val state = stateMachine.state.collectAsState()
                val scope = rememberCoroutineScope()
                Test(state.value) { action ->
                  scope.launch { stateMachine.dispatch(action) }
                }
              }
            }
            
        """.trimIndent()
    }

}
