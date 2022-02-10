package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.util.fragmentNavEventNavigator
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
import org.junit.Test

internal class FileGeneratorTestRendererFragment {

    private val full = RendererFragmentData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        dependencies = ClassName("com.test", "TestDependencies"),
        fragmentBaseClass = ClassName("androidx.fragment.app", "Fragment"),
        factory = ClassName("com.test", "RendererFactory"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = CommonData.Navigation(
            fragmentNavEventNavigator,
            ClassName("com.test", "TestRoute"),
            null,
        ),
        coroutinesEnabled = true,
        rxJavaEnabled = true,
    )

    @Test
    fun `generates code for RendererFragmentData`() {
        FileGenerator().generate(full).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.fragment.FragmentNavEventNavigator
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireNavRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModelProvider
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

              public val fragmentNavEventNavigator: FragmentNavEventNavigator
            
              public val rendererFactory: RendererFactory

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
              private lateinit var rendererFactory: RendererFactory

              private lateinit var testStateMachine: TestStateMachine

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::testStateMachine.isInitialized) {
                  val testRoute = requireNavRoute<TestRoute>()
                  inject(testRoute)
                }
            
                val renderer = rendererFactory.inflate(inflater, container)
                connect(renderer, testStateMachine)
                return renderer.rootView
              }

              private fun inject(testRoute: TestRoute): Unit {
                val viewModelProvider = viewModelProvider<TestDependencies>(this, TestParentScope::class) {
                    dependencies, handle -> 
                  TestViewModel(dependencies, handle, testRoute)
                }
                val viewModel = viewModelProvider[TestViewModel::class.java]
                val component = viewModel.component

                rendererFactory = component.rendererFactory
                testStateMachine = component.testStateMachine

                val navigator = component.fragmentNavEventNavigator
                handleNavigation(this, navigator)
              }
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for RendererFragmentData, no navigation`() {
        val noNavigation = full.copy(navigation = null)

        FileGenerator().generate(noNavigation).toString() shouldBe """
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
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModelProvider
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
                  val arguments = requireArguments()
                  inject(arguments)
                }
            
                val renderer = rendererFactory.inflate(inflater, container)
                connect(renderer, testStateMachine)
                return renderer.rootView
              }

              private fun inject(arguments: Bundle): Unit {
                val viewModelProvider = viewModelProvider<TestDependencies>(this, TestParentScope::class) {
                    dependencies, handle -> 
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

    @Test
    fun `generates code for RendererFragmentData, dialog fragment`() {
        val dialogFragment = full.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment")
        )

        FileGenerator().generate(dialogFragment).toString() shouldBe """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.fragment.FragmentNavEventNavigator
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireNavRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModelProvider
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

              public val fragmentNavEventNavigator: FragmentNavEventNavigator
            
              public val rendererFactory: RendererFactory

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
              private lateinit var rendererFactory: RendererFactory

              private lateinit var testStateMachine: TestStateMachine

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
              ): View {
                if (!::testStateMachine.isInitialized) {
                  val testRoute = requireNavRoute<TestRoute>()
                  inject(testRoute)
                }
            
                val renderer = rendererFactory.inflate(inflater, container)
                connect(renderer, testStateMachine)
                return renderer.rootView
              }

              private fun inject(testRoute: TestRoute): Unit {
                val viewModelProvider = viewModelProvider<TestDependencies>(this, TestParentScope::class) {
                    dependencies, handle -> 
                  TestViewModel(dependencies, handle, testRoute)
                }
                val viewModel = viewModelProvider[TestViewModel::class.java]
                val component = viewModel.component

                rendererFactory = component.rendererFactory
                testStateMachine = component.testStateMachine

                val navigator = component.fragmentNavEventNavigator
                handleNavigation(this, navigator)
              }
            }
            
        """.trimIndent()
    }
}
