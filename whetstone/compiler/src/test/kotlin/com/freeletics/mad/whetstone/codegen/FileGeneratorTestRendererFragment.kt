package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.RendererFragmentData
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
import org.junit.Test

class FileGeneratorTestRendererFragment {

    @Test
    fun `generates code for RendererData`() {
        val withRenderer = RendererFragmentData(
            common = full,
            factory = ClassName("com.test", "RendererFactory")
        )

        FileGenerator().generate(withRenderer).toString() shouldBe """
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
                handler.handle(this, navigator)
              }
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for RendererData, no navigation`() {
        val withRendererNoNavigation = RendererFragmentData(
            common = full.copy(navigation = null),
            factory = ClassName("com.test", "RendererFactory")
        )

        FileGenerator().generate(withRendererNoNavigation).toString() shouldBe """
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
