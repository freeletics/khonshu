@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.NavEntryData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.RendererFragmentData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import org.intellij.lang.annotations.Language
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
        @Language("kotlin")
        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.khonshu.codegen.fragment.RendererFragment
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

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.component
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

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
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
                    arguments: Bundle): KhonshuTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestRendererFragment : Fragment() {
              private lateinit var khonshuTestRendererComponent: KhonshuTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestRendererComponent.isInitialized) {
                  val arguments = requireArguments()
                  khonshuTestRendererComponent = component(this, requireContext(), TestParentScope::class,
                      arguments) { parentComponent: KhonshuTestRendererComponent.ParentComponent,
                      savedStateHandle, argumentsForComponent ->
                    parentComponent.khonshuTestRendererComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }
            
                val renderer = khonshuTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, khonshuTestRendererComponent.testStateMachine)
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

        @Language("kotlin")
        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.khonshu.codegen.fragment.RendererDestination
            import com.freeletics.khonshu.codegen.fragment.DestinationType
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

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
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

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
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
                    testRoute: TestRoute): KhonshuTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestRendererComponentFactory(): Factory
              }
            }
            
            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestRendererComponentProvider :
                ComponentProvider<TestRoute, KhonshuTestRendererComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuTestRendererComponent = component(route.destinationId, route, executor, context,
                  TestParentScope::class, TestDestinationScope::class) { parentComponent:
                  KhonshuTestRendererComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuTestRendererComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestRendererFragment : Fragment() {
              private lateinit var khonshuTestRendererComponent: KhonshuTestRendererComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuTestRendererComponent = KhonshuTestRendererComponentProvider.provide(testRoute,
                      executor, requireContext())
            
                  handleNavigation(this, khonshuTestRendererComponent.navEventNavigator)
                }
            
                val renderer = khonshuTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, khonshuTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuTestRendererFragment>()
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

        @Language("kotlin")
        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.khonshu.codegen.fragment.RendererDestination
            import com.freeletics.khonshu.codegen.fragment.DestinationType
            import com.freeletics.khonshu.codegen.NavEntryComponent
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

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.NavEntry
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.NavDestinationComponent
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.codegen.`internal`.navEntryComponent
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
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

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
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
                    testRoute: TestRoute): KhonshuTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestRendererComponentFactory(): Factory
              }
            }
            
            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestRendererComponentProvider :
                ComponentProvider<TestRoute, KhonshuTestRendererComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuTestRendererComponent = component(route.destinationId, route, executor, context,
                  TestParentScope::class, TestDestinationScope::class) { parentComponent:
                  KhonshuTestRendererComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuTestRendererComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestRendererFragment : Fragment() {
              private lateinit var khonshuTestRendererComponent: KhonshuTestRendererComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuTestRendererComponent = KhonshuTestRendererComponentProvider.provide(testRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuTestRendererComponent.navEventNavigator)
                }

                val renderer = khonshuTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, khonshuTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuTestRendererFragment>()
            }

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestScreenNavEntryComponent : Closeable {
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
                    testRoute: TestRoute): KhonshuTestScreenNavEntryComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestScreenNavEntryModule {
              @Multibinds
              @NavEntry(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalCodegenApi::class, InternalNavigationApi::class)
              override fun retrieve(executor: NavigationExecutor, context: Context): Any =
                  navEntryComponent(TestRoute::class, executor, context, TestParentScope::class,
                  TestDestinationScope::class) { parentComponent:
                  KhonshuTestScreenNavEntryComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuTestScreenNavEntryComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalCodegenApi::class)
            public interface KhonshuTestScreenNavEntryNavDestinationComponent : NavDestinationComponent
            
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

        @Language("kotlin")
        val source = """
            package com.test
            
            import android.view.View
            import com.freeletics.khonshu.codegen.fragment.RendererDestination
            import com.freeletics.khonshu.codegen.fragment.DestinationType
            import com.freeletics.khonshu.codegen.NavEntryComponent
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

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.AppScope
            import com.freeletics.khonshu.codegen.NavEntry
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.NavDestinationComponent
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.codegen.`internal`.navEntryComponent
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.fragment.NavDestination
            import com.freeletics.khonshu.navigation.fragment.ScreenDestination
            import com.freeletics.khonshu.navigation.fragment.findNavigationExecutor
            import com.freeletics.khonshu.navigation.fragment.handleNavigation
            import com.freeletics.khonshu.navigation.fragment.requireRoute
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

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = AppScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
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
                    testRoute: TestRoute): KhonshuTestRendererComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun khonshuTestRendererComponentFactory(): Factory
              }
            }
            
            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestRendererComponentProvider :
                ComponentProvider<TestRoute, KhonshuTestRendererComponent> {
              @OptIn(InternalNavigationApi::class)
              override fun provide(
                route: TestRoute,
                executor: NavigationExecutor,
                context: Context,
              ): KhonshuTestRendererComponent = component(route.destinationId, route, executor, context,
                  AppScope::class, AppScope::class) { parentComponent:
                  KhonshuTestRendererComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuTestRendererComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestRendererFragment : Fragment() {
              private lateinit var khonshuTestRendererComponent: KhonshuTestRendererComponent

              @OptIn(InternalNavigationApi::class)
              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  val executor = findNavigationExecutor()
                  khonshuTestRendererComponent = KhonshuTestRendererComponentProvider.provide(testRoute,
                      executor, requireContext())

                  handleNavigation(this, khonshuTestRendererComponent.navEventNavigator)
                }

                val renderer = khonshuTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, khonshuTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

            @Module
            @ContributesTo(AppScope::class)
            public object KhonshuTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuTestRendererFragment>()
            }

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = AppScope::class,
            )
            public interface KhonshuTestScreenNavEntryComponent : Closeable {
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
                    testRoute: TestRoute): KhonshuTestScreenNavEntryComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun khonshuTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestScreenNavEntryModule {
              @Multibinds
              @NavEntry(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            @NavEntryComponentGetterKey(TestScreen::class)
            @ContributesMultibinding(
              AppScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestScreenNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalCodegenApi::class, InternalNavigationApi::class)
              override fun retrieve(executor: NavigationExecutor, context: Context): Any =
                  navEntryComponent(TestRoute::class, executor, context, AppScope::class, AppScope::class) {
                  parentComponent: KhonshuTestScreenNavEntryComponent.ParentComponent, savedStateHandle,
                  testRoute ->
                parentComponent.khonshuTestScreenNavEntryComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @ContributesTo(AppScope::class)
            @OptIn(InternalCodegenApi::class)
            public interface KhonshuTestScreenNavEntryNavDestinationComponent : NavDestinationComponent
            
        """.trimIndent()

        test(withDefaultValues, "com/test/TestRenderer.kt", source, expected)
    }

    @Test
    fun `generates code for RendererFragmentData, dialog fragment`() {
        val dialogFragment = data.copy(
            fragmentBaseClass = ClassName("androidx.fragment.app", "DialogFragment"),
        )

        @Language("kotlin")
        val source = """
            package com.test
            
            import android.view.View
            import androidx.fragment.app.DialogFragment
            import com.freeletics.khonshu.codegen.fragment.RendererFragment
            import com.freeletics.khonshu.codegen.fragment.DestinationType
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

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.DialogFragment
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.component
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

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
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
                    arguments: Bundle): KhonshuTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestRendererFragment : DialogFragment() {
              private lateinit var khonshuTestRendererComponent: KhonshuTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::khonshuTestRendererComponent.isInitialized) {
                  val arguments = requireArguments()
                  khonshuTestRendererComponent = component(this, requireContext(), TestParentScope::class,
                      arguments) { parentComponent: KhonshuTestRendererComponent.ParentComponent,
                      savedStateHandle, argumentsForComponent ->
                    parentComponent.khonshuTestRendererComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }
            
                val renderer = khonshuTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, khonshuTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

        """.trimIndent()

        test(dialogFragment, "com/test/TestRenderer.kt", source, expected)
    }
}
