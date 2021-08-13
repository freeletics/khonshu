package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Extra
import io.kotest.matchers.shouldBe
import org.junit.Test

class FileGeneratorTestComposeFragment {


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
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.navigation.TestNavigationHandler
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Component
            import io.reactivex.disposables.CompositeDisposable
            import kotlin.Boolean
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
              private var navigationSetup: Boolean = false
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!navigationSetup) {
                  navigationSetup = true
                  setupNavigation()
                }
            
                val navController = findNavController()
                val composeView = ComposeView(requireContext())
                composeView.setContent {
                  TestScreen(navController)
                }
                return composeView
              }
            
              private fun setupNavigation(): Unit {
                val viewModelProvider = viewModelProvider<TestDependencies>(this, TestParentScope::class) {
                    dependencies, handle -> 
                  val arguments = arguments ?: Bundle.EMPTY
                  TestViewModel(dependencies, handle, arguments)
                }
                val viewModel = viewModelProvider[TestViewModel::class.java]
                val component = viewModel.component
            
                val handler = component.testNavigationHandler
                val navigator = component.testNavigator
                handler.handle(this, navigator)
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
}
