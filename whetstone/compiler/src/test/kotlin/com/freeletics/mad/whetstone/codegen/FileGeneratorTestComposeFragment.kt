package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.codegen.util.dialogFragment
import com.freeletics.mad.whetstone.codegen.util.fragment
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
import org.junit.Test

internal class FileGeneratorTestComposeFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "NONE",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
        navEntryData = null,
    )

    private val full = ComposeFragmentData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        dependencies = ClassName("com.test", "TestDependencies"),
        fragmentBaseClass = fragment,
        stateMachine = ClassName("com.test", "TestStateMachine"),
        enableInsetHandling = true,
        navigation = navigation,
        coroutinesEnabled = true,
        rxJavaEnabled = true,
    )

    @Test
    fun `generates code for ComposeFragmentData`() {
        FileGenerator().generate(full).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.google.accompanist.insets.LocalWindowInsets
            import com.google.accompanist.insets.ViewWindowInsetObserver
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::TestViewModel)
                  retainedTestComponent = viewModel.component
            
                  handleNavigation(this, retainedTestComponent.navEventNavigator)
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT)
                  val observer = ViewWindowInsetObserver(this)
                  val windowInsets = observer.start()

                  setContent {
                    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                      TestScreen(retainedTestComponent)
                    }
                  }
                }
              }
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
    }

    @Test
    fun `generates code for ComposeFragmentData with destination`() {
        val withDestination = full.copy(navigation = navigation.copy(destinationType = "SCREEN"))

        FileGenerator().generate(withDestination).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.google.accompanist.insets.LocalWindowInsets
            import com.google.accompanist.insets.ViewWindowInsetObserver
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::TestViewModel)
                  retainedTestComponent = viewModel.component
            
                  handleNavigation(this, retainedTestComponent.navEventNavigator)
                }
            
                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT)
                  val observer = ViewWindowInsetObserver(this)
                  val windowInsets = observer.start()

                  setContent {
                    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                      TestScreen(retainedTestComponent)
                    }
                  }
                }
              }
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
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute, TestFragment>()
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for ComposeFragmentData, dialog fragment`() {
        val dialogFragment = full.copy(
            fragmentBaseClass = dialogFragment
        )

        FileGenerator().generate(dialogFragment).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.google.accompanist.insets.LocalWindowInsets
            import com.google.accompanist.insets.ViewWindowInsetObserver
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : DialogFragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::TestViewModel)
                  retainedTestComponent = viewModel.component
            
                  handleNavigation(this, retainedTestComponent.navEventNavigator)
                }
            
                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT)
                  val observer = ViewWindowInsetObserver(this)
                  val windowInsets = observer.start()

                  setContent {
                    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                      TestScreen(retainedTestComponent)
                    }
                  }
                }
              }
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
    }

    @Test
    fun `generates code for ComposeFragmentData, no inset handling`() {
        val noInsetHandling = full.copy(enableInsetHandling = false)

        FileGenerator().generate(noInsetHandling).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeComponent
            import com.test.destination.TestDestinationScope
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent
            
              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::TestViewModel)
                  retainedTestComponent = viewModel.component
            
                  handleNavigation(this, retainedTestComponent.navEventNavigator)
                }
            
                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  setContent {
                    TestScreen(retainedTestComponent)
                  }
                }
              }
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
    }

    @Test
    fun `generates code for ComposeFragmentData, no navigation`() {
        val noNavigation = full.copy(navigation = null)

        FileGenerator().generate(noNavigation).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.ProvidedValue
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.ComposeView
            import androidx.compose.ui.platform.ViewCompositionStrategy
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.ComposeProviderValueModule
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.asComposeState
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.google.accompanist.insets.LocalWindowInsets
            import com.google.accompanist.insets.ViewWindowInsetObserver
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  val viewModel = viewModel(TestParentScope::class, arguments, ::TestViewModel)
                  retainedTestComponent = viewModel.component
                }

                return ComposeView(requireContext()).apply {
                  setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))

                  layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT)
                  val observer = ViewWindowInsetObserver(this)
                  val windowInsets = observer.start()
  
                  setContent {
                    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                      TestScreen(retainedTestComponent)
                    }
                  }
                }
              }
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
