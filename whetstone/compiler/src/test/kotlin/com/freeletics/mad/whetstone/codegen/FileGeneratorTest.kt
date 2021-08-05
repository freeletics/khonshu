package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Extra
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.Data
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
import org.junit.Test

class FileGeneratorTest {

    private val full = Data(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        dependencies = ClassName("com.test", "TestDependencies"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = Navigation(
            navigator = ClassName("com.test", "TestNavigator"),
            navigationHandler = ClassName("com.test.navigation", "TestNavigationHandler"),
        ),
        coroutinesEnabled = true,
        rxJavaEnabled = true,
        extra = null
    )

    @Test
    fun `generates code for full ScreenData`() {
        val generator = FileGenerator(full)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

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
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData without navigation`() {
        val withoutNavigation = full.copy(navigation = null)
        val generator = FileGenerator(withoutNavigation)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.squareup.anvil.annotations.MergeComponent
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

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
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData without coroutines`() {
        val withoutCoroutines = full.copy(coroutinesEnabled = false)
        val generator = FileGenerator(withoutCoroutines)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.Unit

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

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
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData without rxjava`() {
        val withoutRxJava = full.copy(rxJavaEnabled = false)
        val generator = FileGenerator(withoutRxJava)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
            import dagger.BindsInstance
            import dagger.Component
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

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
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData with compose`() {
        val without = full.copy(
            extra = Extra.Compose(withFragment = false)
        )
        val generator = FileGenerator(without)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import androidx.activity.OnBackPressedDispatcher
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import com.freeletics.mad.whetstone.ScopeTo
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
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

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
            public fun TestScreen(navController: NavController,
                onBackPressedDispatcher: OnBackPressedDispatcher): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                val arguments = navController.currentBackStackEntry!!.arguments ?: Bundle.EMPTY
                TestViewModel(dependencies, handle, arguments)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
              val component = viewModel.component

              LaunchedEffect(navController, onBackPressedDispatcher, component) {
                val handler = component.testNavigationHandler
                val navigator = component.testNavigator
                handler.handle(this, navController, onBackPressedDispatcher, navigator)
              }

              val stateMachine = component.testStateMachine
              val state = stateMachine.state.collectAsState()
              val scope = rememberCoroutineScope()
              Test(state.value) { action ->
                scope.launch { stateMachine.dispatch(action) }
              }
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData with compose, no navigation`() {
        val without = full.copy(
            navigation = null,
            extra = Extra.Compose(withFragment = false)
        )
        val generator = FileGenerator(without)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

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

              val stateMachine = component.testStateMachine
              val state = stateMachine.state.collectAsState()
              val scope = rememberCoroutineScope()
              Test(state.value) { action ->
                scope.launch { stateMachine.dispatch(action) }
              }
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData with compose fragment`() {
        val withComposeFragment = full.copy(
            extra = Extra.Compose(withFragment = true)
        )
        val generator = FileGenerator(withComposeFragment)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import androidx.navigation.fragment.findNavController
            import com.freeletics.mad.whetstone.ScopeTo
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
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler

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
            public fun TestScreen(navController: NavController, fragment: Fragment): Unit {
              val viewModelProvider = rememberViewModelProvider<TestDependencies>(TestParentScope::class) {
                  dependencies, handle -> 
                val arguments = navController.currentBackStackEntry!!.arguments ?: Bundle.EMPTY
                TestViewModel(dependencies, handle, arguments)
              }
              val viewModel = viewModelProvider[TestViewModel::class.java]
              val component = viewModel.component

              LaunchedEffect(fragment, component) {
                val handler = component.testNavigationHandler
                val navigator = component.testNavigator
                handler.handle(this, fragment, navigator)
              }

              val stateMachine = component.testStateMachine
              val state = stateMachine.state.collectAsState()
              val scope = rememberCoroutineScope()
              Test(state.value) { action ->
                scope.launch { stateMachine.dispatch(action) }
              }
            }
            
            public class TestFragment : Fragment() {
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                val navController = findNavController()
                val composeView = ComposeView(requireContext())
                composeView.setContent {
                  TestScreen(navController, this)
                }
                return composeView
              }
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData with compose fragment, no navigation`() {
        val withComposeFragmentNoNavigation = full.copy(
            navigation = null,
            extra = Extra.Compose(withFragment = true)
        )
        val generator = FileGenerator(withComposeFragmentNoNavigation)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.collectAsState
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavController
            import androidx.navigation.fragment.findNavController
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.rememberViewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel
            import kotlinx.coroutines.launch

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

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

              val stateMachine = component.testStateMachine
              val state = stateMachine.state.collectAsState()
              val scope = rememberCoroutineScope()
              Test(state.value) { action ->
                scope.launch { stateMachine.dispatch(action) }
              }
            }
            
            public class TestFragment : Fragment() {
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                val navController = findNavController()
                val composeView = ComposeView(requireContext())
                composeView.setContent {
                  TestScreen(navController)
                }
                return composeView
              }
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData with renderer`() {
        val withRenderer = full.copy(
            extra = Extra.Renderer(factory = ClassName("com.test", "RendererFactory"))
        )
        val generator = FileGenerator(withRenderer)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.coroutineScope
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val testNavigator: TestNavigator

              public val testNavigationHandler: TestNavigationHandler
            
              public val rendererFactory: RendererFactory

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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var rendererFactory: RendererFactory

              private lateinit var testStateMachine: TestStateMachine

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::testStateMachine.isInitialized) {
                  inject()
                }
                val renderer = rendererFactory.inflate(inflater, container)
                connect(renderer, testStateMachine)
                return renderer.rootView
              }

              private fun inject(): Unit {
                val viewModelProvider = viewModelProvider<TestDependencies>(this, TestParentScope::class) {
                    dependencies, handle -> 
                  val arguments = arguments ?: Bundle.EMPTY
                  TestViewModel(dependencies, handle, arguments)
                }
                val viewModel = viewModelProvider[TestViewModel::class.java]
                val component = viewModel.component

                rendererFactory = component.rendererFactory
                testStateMachine = component.testStateMachine

                val handler = component.testNavigationHandler
                val navigator = component.testNavigator
                val scope = lifecycle.coroutineScope
                handler.handle(scope, this, navigator)
              }
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ScreenData with renderer, no navigation`() {
        val withRendererNoNavigation = full.copy(
            navigation = null,
            extra = Extra.Renderer(factory = ClassName("com.test", "RendererFactory"))
        )
        val generator = FileGenerator(withRendererNoNavigation)

        generator.generate().toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class]
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine
            
              public val rendererFactory: RendererFactory

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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var rendererFactory: RendererFactory

              private lateinit var testStateMachine: TestStateMachine

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::testStateMachine.isInitialized) {
                  inject()
                }
                val renderer = rendererFactory.inflate(inflater, container)
                connect(renderer, testStateMachine)
                return renderer.rootView
              }

              private fun inject(): Unit {
                val viewModelProvider = viewModelProvider<TestDependencies>(this, TestParentScope::class) {
                    dependencies, handle -> 
                  val arguments = arguments ?: Bundle.EMPTY
                  TestViewModel(dependencies, handle, arguments)
                }
                val viewModel = viewModelProvider[TestViewModel::class.java]
                val component = viewModel.component

                rendererFactory = component.rendererFactory
                testStateMachine = component.testStateMachine
              }
            }
            
        """.trimIndent()
    }
}
