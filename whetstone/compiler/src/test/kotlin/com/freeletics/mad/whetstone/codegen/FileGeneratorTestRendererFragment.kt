package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.RendererFragmentData
import com.squareup.kotlinpoet.ClassName
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class FileGeneratorTestRendererFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "NONE",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
        navEntryData = null,
    )

    private val full = RendererFragmentData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        dependencies = ClassName("com.test", "TestDependencies"),
        fragmentBaseClass = ClassName("androidx.fragment.app", "Fragment"),
        factory = ClassName("com.test", "RendererFactory"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = navigation,
        coroutinesEnabled = true,
        rxJavaEnabled = true,
    )

    @Test
    fun `generates code for RendererFragmentData`() {
        val actual = FileGenerator().generate(full).toString()

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
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

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
            
              public val closeables: Set<Closeable>

              public val rendererFactory: RendererFactory

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
              @IntoSet
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::TestViewModel)
                  retainedTestComponent = viewModel.component
            
                  handleNavigation(this, retainedTestComponent.navEventNavigator)
                }
            
                val renderer = retainedTestComponent.rendererFactory.inflate(inflater, container)
                connect(renderer, retainedTestComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface NavEntryTestDestinationComponent : DestinationComponent
            
        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates code for RendererFragmentData with destination`() {
        val withDestination = full.copy(navigation = navigation.copy(destinationType = "SCREEN"))
        val actual = FileGenerator().generate(withDestination).toString()

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
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

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
            
              public val closeables: Set<Closeable>

              public val rendererFactory: RendererFactory

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
              @IntoSet
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::TestViewModel)
                  retainedTestComponent = viewModel.component
            
                  handleNavigation(this, retainedTestComponent.navEventNavigator)
                }
            
                val renderer = retainedTestComponent.rendererFactory.inflate(inflater, container)
                connect(renderer, retainedTestComponent.testStateMachine)
                return renderer.rootView
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

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates code for RendererFragmentData, no navigation`() {
        val noNavigation = full.copy(navigation = null)
        val actual = FileGenerator().generate(noNavigation).toString()

        val expected = """
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
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
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

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine
            
              public val closeables: Set<Closeable>

              public val rendererFactory: RendererFactory

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
              @IntoSet
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : Fragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  val viewModel = viewModel(TestParentScope::class, arguments, ::TestViewModel)
                  retainedTestComponent = viewModel.component
                }
            
                val renderer = retainedTestComponent.rendererFactory.inflate(inflater, container)
                connect(renderer, retainedTestComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates code for RendererFragmentData, dialog fragment`() {
        val dialogFragment = full.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment")
        )
        val actual = FileGenerator().generate(dialogFragment).toString()

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
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

            @InternalWhetstoneApi
            @ScopeTo(TestScreen::class)
            @MergeComponent(
              scope = TestScreen::class,
              dependencies = [TestDependencies::class],
            )
            internal interface RetainedTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
            
              public val closeables: Set<Closeable>

              public val rendererFactory: RendererFactory

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
              @IntoSet
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
            
            @OptIn(InternalWhetstoneApi::class)
            public class TestFragment : DialogFragment() {
              private lateinit var retainedTestComponent: RetainedTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::retainedTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::TestViewModel)
                  retainedTestComponent = viewModel.component
            
                  handleNavigation(this, retainedTestComponent.navEventNavigator)
                }
            
                val renderer = retainedTestComponent.rendererFactory.inflate(inflater, container)
                connect(renderer, retainedTestComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface NavEntryTestDestinationComponent : DestinationComponent
            
        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }
}
