package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.RendererFragmentData
import com.squareup.kotlinpoet.ClassName
import org.junit.Test

internal class FileGeneratorTestRendererFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "NONE",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = RendererFragmentData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        factory = ClassName("com.test", "TestRenderer").nestedClass("Factory"),
        fragmentBaseClass = ClassName("androidx.fragment.app", "Fragment"),
        navigation = null,
        navEntryData = null,
    )

    private val navEntryData = NavEntryData(
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        navigation = navigation,
    )

    @Test
    fun `generates code for RendererFragmentData`() {
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

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent {
              public val testStateMachine: TestStateMachine
            
              public val closeables: Set<Closeable>

              public val testRendererFactory: TestRenderer.Factory

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class WhetstoneTestViewModel(
              parentComponent: WhetstoneTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle,
            ) : ViewModel() {
              public val component: WhetstoneTestComponent =
                  parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, arguments)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : Fragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  val viewModel = viewModel(TestParentScope::class, arguments, ::WhetstoneTestViewModel)
                  whetstoneTestComponent = viewModel.component
                }
            
                val renderer = whetstoneTestComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
        """.trimIndent()

        test(data, expected)
    }

    @Test
    fun `generates code for RendererFragmentData with navigation`() {
        val withNavigation = data.copy(navigation = navigation)

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
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
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

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
            
              public val closeables: Set<Closeable>

              public val testRendererFactory: TestRenderer.Factory

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class WhetstoneTestViewModel(
              parentComponent: WhetstoneTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: WhetstoneTestComponent =
                  parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : Fragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::WhetstoneTestViewModel)
                  whetstoneTestComponent = viewModel.component
            
                  handleNavigation(this, whetstoneTestComponent.navEventNavigator)
                }
            
                val renderer = whetstoneTestComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
        """.trimIndent()

        test(withNavigation, expected)
    }

    @Test
    fun `generates code for RendererFragmentData with navigation and destination`() {
        val withDestination = data.copy(navigation = navigation.copy(destinationType = "SCREEN"))

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
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
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

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator
            
              public val closeables: Set<Closeable>

              public val testRendererFactory: TestRenderer.Factory

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class WhetstoneTestViewModel(
              parentComponent: WhetstoneTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: WhetstoneTestComponent =
                  parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : Fragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::WhetstoneTestViewModel)
                  whetstoneTestComponent = viewModel.component
            
                  handleNavigation(this, whetstoneTestComponent.navEventNavigator)
                }
            
                val renderer = whetstoneTestComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object WhetstoneTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  WhetstoneTestFragment>()
            }
            
        """.trimIndent()

        test(withDestination, expected)
    }

    @Test
    fun `generates code for RendererFragmentData with navigation, destination and navEntry`() {
        val withDestination = data.copy(
            navigation = navigation.copy(destinationType = "SCREEN"),
            navEntryData = navEntryData
        )

        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.destinationId
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.DestinationComponent
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.ContributesMultibinding
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
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import com.freeletics.mad.navigator.`internal`.requireRoute as bundleRequireRoute

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>

              public val testRendererFactory: TestRenderer.Factory

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class WhetstoneTestViewModel(
              parentComponent: WhetstoneTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: WhetstoneTestComponent =
                  parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : Fragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val viewModel = viewModel(TestParentScope::class, TestDestinationScope::class, testRoute,
                      ::WhetstoneTestViewModel)
                  whetstoneTestComponent = viewModel.component

                  handleNavigation(this, whetstoneTestComponent.navEventNavigator)
                }

                val renderer = whetstoneTestComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestComponent.testStateMachine)
                return renderer.rootView
              }
            }

            @Module
            @ContributesTo(TestDestinationScope::class)
            public object WhetstoneTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  WhetstoneTestFragment>()
            }

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestScreenNavEntryComponent {
              @get:NavEntry(TestScreen::class)
              public val closeables: Set<Closeable>

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestScreen::class)
                    testRoute: TestRoute): WhetstoneTestScreenNavEntryComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestScreenNavEntryModule {
              @Multibinds
              @NavEntry(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class WhetstoneTestScreenNavEntryViewModel(
              parentComponent: WhetstoneTestScreenNavEntryComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: WhetstoneTestScreenNavEntryComponent =
                  parentComponent.whetstoneTestScreenNavEntryComponentFactory().create(savedStateHandle,
                  testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(TestRoute::class.destinationId())
                val route: TestRoute = entry.arguments.bundleRequireRoute()
                val viewModel = com.freeletics.mad.whetstone.`internal`.viewModel(entry, context,
                    TestParentScope::class, TestDestinationScope::class, route, findEntry,
                    ::WhetstoneTestScreenNavEntryViewModel)
                return viewModel.component
              }
            }

            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface WhetstoneTestScreenNavEntryDestinationComponent : DestinationComponent
            
        """.trimIndent()

        test(withDestination, expected)
    }

    @Test
    fun `generates code for RendererFragmentData, dialog fragment`() {
        val dialogFragment = data.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment")
        )

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.viewModel
            import com.gabrielittner.renderer.connect.connect
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

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestComponent {
              public val testStateMachine: TestStateMachine
            
              public val closeables: Set<Closeable>

              public val testRendererFactory: TestRenderer.Factory

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): WhetstoneTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class WhetstoneTestViewModel(
              parentComponent: WhetstoneTestComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle,
            ) : ViewModel() {
              public val component: WhetstoneTestComponent =
                  parentComponent.whetstoneTestComponentFactory().create(savedStateHandle, arguments)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestFragment : DialogFragment() {
              private lateinit var whetstoneTestComponent: WhetstoneTestComponent

              public override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestComponent.isInitialized) {
                  val arguments = requireArguments()
                  val viewModel = viewModel(TestParentScope::class, arguments, ::WhetstoneTestViewModel)
                  whetstoneTestComponent = viewModel.component
                }
            
                val renderer = whetstoneTestComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestComponent.testStateMachine)
                return renderer.rootView
              }
            }

        """.trimIndent()

        test(dialogFragment, expected)
    }
}
