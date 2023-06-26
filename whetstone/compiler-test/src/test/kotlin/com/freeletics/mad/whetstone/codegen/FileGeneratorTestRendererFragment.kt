package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.AppScope
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.RendererFragmentData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import org.junit.Test

internal class FileGeneratorTestRendererFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "SCREEN",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = RendererFragmentData(
        baseName = "TestRenderer",
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
        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.mad.whetstone.fragment.RendererFragment
            import com.gabrielittner.renderer.ViewRenderer
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            
            @RendererFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            public class TestRenderer(view: View) : ViewRenderer<TestState, TestAction>(view) {
              override fun renderToView(state: TestState) {}
            
              public abstract class Factory : ViewRenderer.Factory<TestBinding, TestRenderer>({ _, _, _ -> TestBinding() })
            }
        """.trimIndent()

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.component
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val testRendererFactory: TestRenderer.Factory

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): WhetstoneTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestRendererFragment : Fragment() {
              private lateinit var whetstoneTestRendererComponent: WhetstoneTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestRendererComponent.isInitialized) {
                  val arguments = requireArguments()
                  whetstoneTestRendererComponent = component(TestParentScope::class, arguments) {
                      parentComponent: WhetstoneTestRendererComponent.ParentComponent, savedStateHandle,
                      argumentsForComponent ->
                    parentComponent.whetstoneTestRendererComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }
            
                val renderer = whetstoneTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
        """.trimIndent()

        test(data, "com/test/TestRenderer.kt", source, expected)
    }

    @Test
    fun `generates code for RendererFragmentData with navigation`() {
        val withNavigation = data.copy(
            scope = navigation.route,
            navigation = navigation,
        )

        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.mad.whetstone.fragment.RendererDestination
            import com.freeletics.mad.whetstone.fragment.DestinationType
            import com.gabrielittner.renderer.ViewRenderer
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            
            @RendererDestination(
              route = TestRoute::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
              destinationScope = TestDestinationScope::class,
            )
            public class TestRenderer(view: View) : ViewRenderer<TestState, TestAction>(view) {
              override fun renderToView(state: TestState) {}
            
              public abstract class Factory : ViewRenderer.Factory<TestBinding, TestRenderer>({ _, _, _ -> TestBinding() })
            }
        """.trimIndent()

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.component
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
            import kotlin.collections.Set

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val testRendererFactory: TestRenderer.Factory

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface WhetstoneTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestRendererFragment : Fragment() {
              private lateinit var whetstoneTestRendererComponent: WhetstoneTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  whetstoneTestRendererComponent = component(TestParentScope::class,
                      TestDestinationScope::class, testRoute) { parentComponent:
                      WhetstoneTestRendererComponent.ParentComponent, savedStateHandle, testRouteForComponent ->
                    parentComponent.whetstoneTestRendererComponentFactory().create(savedStateHandle,
                        testRouteForComponent)
                  }
            
                  handleNavigation(this, whetstoneTestRendererComponent.navEventNavigator)
                }
            
                val renderer = whetstoneTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object WhetstoneTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  WhetstoneTestRendererFragment>()
            }
            
        """.trimIndent()

        test(withNavigation, "com/test/TestRenderer.kt", source, expected)
    }

    @Test
    fun `generates code for RendererFragmentData with navigation and navEntry`() {
        val withDestination = data.copy(
            scope = navigation.route,
            navigation = navigation,
            navEntryData = navEntryData,
        )

        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.mad.whetstone.fragment.RendererDestination
            import com.freeletics.mad.whetstone.fragment.DestinationType
            import com.freeletics.mad.whetstone.NavEntryComponent
            import com.gabrielittner.renderer.ViewRenderer
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            
            @RendererDestination(
              route = TestRoute::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
              destinationScope = TestDestinationScope::class,
            )
            @NavEntryComponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public class TestRenderer(view: View) : ViewRenderer<TestState, TestAction>(view) {
              override fun renderToView(state: TestState) {}
            
              public abstract class Factory : ViewRenderer.Factory<TestBinding, TestRenderer>({ _, _, _ -> TestBinding() })
            }
        """.trimIndent()

        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.NavigationExecutor
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavDestinationComponent
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.navEntryComponent
            import com.freeletics.mad.whetstone.fragment.`internal`.component
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
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val testRendererFactory: TestRenderer.Factory

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface WhetstoneTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestRendererFragment : Fragment() {
              private lateinit var whetstoneTestRendererComponent: WhetstoneTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  whetstoneTestRendererComponent = component(TestParentScope::class,
                      TestDestinationScope::class, testRoute) { parentComponent:
                      WhetstoneTestRendererComponent.ParentComponent, savedStateHandle, testRouteForComponent ->
                    parentComponent.whetstoneTestRendererComponentFactory().create(savedStateHandle,
                        testRouteForComponent)
                  }

                  handleNavigation(this, whetstoneTestRendererComponent.navEventNavigator)
                }

                val renderer = whetstoneTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

            @Module
            @ContributesTo(TestDestinationScope::class)
            public object WhetstoneTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  WhetstoneTestRendererFragment>()
            }

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestScreenNavEntryComponent : Closeable {
              @get:NavEntry(TestScreen::class)
              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

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

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              override fun retrieve(executor: NavigationExecutor, context: Context): Any =
                  navEntryComponent(TestRoute::class, executor, context, TestParentScope::class,
                  TestDestinationScope::class) { parentComponent:
                  WhetstoneTestScreenNavEntryComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.whetstoneTestScreenNavEntryComponentFactory().create(savedStateHandle,
                    testRoute)
              }
            }

            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface WhetstoneTestScreenNavEntryNavDestinationComponent : NavDestinationComponent
            
        """.trimIndent()

        test(withDestination, "com/test/TestRenderer.kt", source, expected)
    }

    @Test
    fun `generates code for RendererFragmentData with default values`() {
        val navigation = navigation.copy(destinationScope = AppScope::class.asClassName())
        val withDefaultValues = data.copy(
            scope = navigation.route,
            parentScope = AppScope::class.asClassName(),
            navigation = navigation,
            navEntryData = navEntryData.copy(
                parentScope = AppScope::class.asClassName(),
                navigation = navigation,
            ),
        )

        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.mad.whetstone.fragment.RendererDestination
            import com.freeletics.mad.whetstone.fragment.DestinationType
            import com.freeletics.mad.whetstone.NavEntryComponent
            import com.gabrielittner.renderer.ViewRenderer
            
            @RendererDestination(
              route = TestRoute::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
            )
            @NavEntryComponent(
              scope = TestScreen::class,
            )
            public class TestRenderer(view: View) : ViewRenderer<TestState, TestAction>(view) {
              override fun renderToView(state: TestState) {}
            
              public abstract class Factory : ViewRenderer.Factory<TestBinding, TestRenderer>({ _, _, _ -> TestBinding() })
            }
        """.trimIndent()

        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.navigator.NavEventNavigator
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.NavigationExecutor
            import com.freeletics.mad.navigator.fragment.NavDestination
            import com.freeletics.mad.navigator.fragment.ScreenDestination
            import com.freeletics.mad.navigator.fragment.handleNavigation
            import com.freeletics.mad.navigator.fragment.requireRoute
            import com.freeletics.mad.whetstone.AppScope
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavDestinationComponent
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.navEntryComponent
            import com.freeletics.mad.whetstone.fragment.`internal`.component
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.ContributesMultibinding
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = AppScope::class,
            )
            public interface WhetstoneTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val testRendererFactory: TestRenderer.Factory

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): WhetstoneTestRendererComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun whetstoneTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface WhetstoneTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestRendererFragment : Fragment() {
              private lateinit var whetstoneTestRendererComponent: WhetstoneTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  whetstoneTestRendererComponent = component(AppScope::class, AppScope::class, testRoute) {
                      parentComponent: WhetstoneTestRendererComponent.ParentComponent, savedStateHandle,
                      testRouteForComponent ->
                    parentComponent.whetstoneTestRendererComponentFactory().create(savedStateHandle,
                        testRouteForComponent)
                  }

                  handleNavigation(this, whetstoneTestRendererComponent.navEventNavigator)
                }

                val renderer = whetstoneTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

            @Module
            @ContributesTo(AppScope::class)
            public object WhetstoneTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  WhetstoneTestRendererFragment>()
            }

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = AppScope::class,
            )
            public interface WhetstoneTestScreenNavEntryComponent : Closeable {
              @get:NavEntry(TestScreen::class)
              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestScreen::class)
                    testRoute: TestRoute): WhetstoneTestScreenNavEntryComponent
              }

              @ContributesTo(AppScope::class)
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

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              AppScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              override fun retrieve(executor: NavigationExecutor, context: Context): Any =
                  navEntryComponent(TestRoute::class, executor, context, AppScope::class, AppScope::class) {
                  parentComponent: WhetstoneTestScreenNavEntryComponent.ParentComponent, savedStateHandle,
                  testRoute ->
                parentComponent.whetstoneTestScreenNavEntryComponentFactory().create(savedStateHandle,
                    testRoute)
              }
            }

            @ContributesTo(AppScope::class)
            @OptIn(InternalWhetstoneApi::class)
            public interface WhetstoneTestScreenNavEntryNavDestinationComponent : NavDestinationComponent
            
        """.trimIndent()

        test(withDefaultValues, "com/test/TestRenderer.kt", source, expected)
    }

    @Test
    fun `generates code for RendererFragmentData, dialog fragment`() {
        val dialogFragment = data.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment"),
        )
        val source = """
            package com.test
            
            import android.view.View
            import androidx.fragment.app.DialogFragment
            import com.freeletics.mad.whetstone.fragment.RendererFragment
            import com.freeletics.mad.whetstone.fragment.DestinationType
            import com.gabrielittner.renderer.ViewRenderer
            import com.test.parent.TestParentScope
            
            @RendererFragment(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              fragmentBaseClass = DialogFragment::class,
            )
            public class TestRenderer(view: View) : ViewRenderer<TestState, TestAction>(view) {
              override fun renderToView(state: TestState) {}
            
              public abstract class Factory : ViewRenderer.Factory<TestBinding, TestRenderer>({ _, _, _ -> TestBinding() })
            }
        """.trimIndent()

        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.fragment.`internal`.component
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val testRendererFactory: TestRenderer.Factory

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): WhetstoneTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface WhetstoneTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalWhetstoneApi::class)
            public class WhetstoneTestRendererFragment : DialogFragment() {
              private lateinit var whetstoneTestRendererComponent: WhetstoneTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::whetstoneTestRendererComponent.isInitialized) {
                  val arguments = requireArguments()
                  whetstoneTestRendererComponent = component(TestParentScope::class, arguments) {
                      parentComponent: WhetstoneTestRendererComponent.ParentComponent, savedStateHandle,
                      argumentsForComponent ->
                    parentComponent.whetstoneTestRendererComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }
            
                val renderer = whetstoneTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, whetstoneTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

        """.trimIndent()

        test(dialogFragment, "com/test/TestRenderer.kt", source, expected)
    }
}
