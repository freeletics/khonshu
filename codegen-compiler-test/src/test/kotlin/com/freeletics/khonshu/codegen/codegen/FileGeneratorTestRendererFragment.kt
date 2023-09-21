@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.RendererFragmentData
import com.freeletics.khonshu.codegen.fragment.DestinationType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class FileGeneratorTestRendererFragment {

    private val navigation = Navigation.Fragment(
        route = ClassName("com.test", "TestRoute"),
        parentScopeIsRoute = true,
        destinationType = DestinationType.SCREEN,
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
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val testRendererFactory: TestRenderer.Factory

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(TestScreen::class)
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
              @ForScope(TestScreen::class)
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
            parentScope = ClassName("com.test.parent", "TestParentRoute"),
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
            import com.test.parent.TestParentRoute

            @RendererDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
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
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
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
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val navEventNavigator: NavEventNavigator

              public val testRendererFactory: TestRenderer.Factory

              @get:ForScope(TestRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle,
                    @BindsInstance testRoute: TestRoute): KhonshuTestRendererComponent
              }

              @ContributesTo(TestParentRoute::class)
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
              ): KhonshuTestRendererComponent = componentFromParentRoute(route.destinationId, route, executor,
                  context, TestParentRoute::class) { parentComponent:
                  KhonshuTestRendererComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.khonshuTestRendererComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestRendererModule {
              @Multibinds
              @ForScope(TestRoute::class)
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

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuTestRendererFragment>(KhonshuTestRendererComponentProvider)
            }

        """.trimIndent()

        test(withNavigation, "com/test/TestRenderer.kt", source, expected)
    }

    @Test
    fun `generates code for RendererFragmentData with default values`() {
        val navigation = navigation.copy(
            parentScopeIsRoute = false,
            destinationScope = AppScope::class.asClassName(),
        )
        val withDefaultValues = data.copy(
            scope = navigation.route,
            parentScope = AppScope::class.asClassName(),
            navigation = navigation,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import android.view.View
            import com.freeletics.khonshu.codegen.fragment.RendererDestination
            import com.freeletics.khonshu.codegen.fragment.DestinationType
            import com.gabrielittner.renderer.ViewRenderer

            @RendererDestination(
              route = TestRoute::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
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
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = AppScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val navEventNavigator: NavEventNavigator

              public val testRendererFactory: TestRenderer.Factory

              @get:ForScope(TestRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestRoute::class) savedStateHandle: SavedStateHandle,
                    @BindsInstance testRoute: TestRoute): KhonshuTestRendererComponent
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
                  AppScope::class) { parentComponent: KhonshuTestRendererComponent.ParentComponent,
                  savedStateHandle, testRoute ->
                parentComponent.khonshuTestRendererComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestRendererModule {
              @Multibinds
              @ForScope(TestRoute::class)
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

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(AppScope::class)
            public object KhonshuTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationApi::class)
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  KhonshuTestRendererFragment>(KhonshuTestRendererComponentProvider)
            }

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
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.gabrielittner.renderer.connect.connect
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestRendererComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val testRendererFactory: TestRenderer.Factory

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestScreen::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(TestScreen::class)
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
              @ForScope(TestScreen::class)
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
