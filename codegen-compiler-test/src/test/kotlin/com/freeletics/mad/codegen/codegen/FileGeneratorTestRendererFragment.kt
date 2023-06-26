package com.freeletics.mad.codegen.codegen

import com.freeletics.mad.codegen.AppScope
import com.freeletics.mad.codegen.NavEntryData
import com.freeletics.mad.codegen.Navigation
import com.freeletics.mad.codegen.RendererFragmentData
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
            import com.freeletics.mad.codegen.fragment.RendererFragment
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.fragment.`internal`.component
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
            public interface MadTestRendererComponent : Closeable {
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
                    arguments: Bundle): MadTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestRendererFragment : Fragment() {
              private lateinit var madTestRendererComponent: MadTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestRendererComponent.isInitialized) {
                  val arguments = requireArguments()
                  madTestRendererComponent = component(TestParentScope::class, arguments) { parentComponent:
                      MadTestRendererComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                    parentComponent.madTestRendererComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }
            
                val renderer = madTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, madTestRendererComponent.testStateMachine)
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
            import com.freeletics.mad.codegen.fragment.RendererDestination
            import com.freeletics.mad.codegen.fragment.DestinationType
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.freeletics.mad.navigation.NavEventNavigator
            import com.freeletics.mad.navigation.fragment.NavDestination
            import com.freeletics.mad.navigation.fragment.ScreenDestination
            import com.freeletics.mad.navigation.fragment.handleNavigation
            import com.freeletics.mad.navigation.fragment.requireRoute
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
            public interface MadTestRendererComponent : Closeable {
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
                    testRoute: TestRoute): MadTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface MadTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestRendererFragment : Fragment() {
              private lateinit var madTestRendererComponent: MadTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  madTestRendererComponent = component(TestParentScope::class, TestDestinationScope::class,
                      testRoute) { parentComponent: MadTestRendererComponent.ParentComponent, savedStateHandle,
                      testRouteForComponent ->
                    parentComponent.madTestRendererComponentFactory().create(savedStateHandle,
                        testRouteForComponent)
                  }
            
                  handleNavigation(this, madTestRendererComponent.navEventNavigator)
                }
            
                val renderer = madTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, madTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }
            
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object MadTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  MadTestRendererFragment>()
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
            import com.freeletics.mad.codegen.fragment.RendererDestination
            import com.freeletics.mad.codegen.fragment.DestinationType
            import com.freeletics.mad.codegen.NavEntryComponent
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
            import com.freeletics.mad.codegen.NavEntry
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.NavDestinationComponent
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.codegen.`internal`.navEntryComponent
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.freeletics.mad.navigation.NavEventNavigator
            import com.freeletics.mad.navigation.`internal`.InternalNavigationApi
            import com.freeletics.mad.navigation.`internal`.NavigationExecutor
            import com.freeletics.mad.navigation.fragment.NavDestination
            import com.freeletics.mad.navigation.fragment.ScreenDestination
            import com.freeletics.mad.navigation.fragment.handleNavigation
            import com.freeletics.mad.navigation.fragment.requireRoute
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
            public interface MadTestRendererComponent : Closeable {
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
                    testRoute: TestRoute): MadTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface MadTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class MadTestRendererFragment : Fragment() {
              private lateinit var madTestRendererComponent: MadTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  madTestRendererComponent = component(TestParentScope::class, TestDestinationScope::class,
                      testRoute) { parentComponent: MadTestRendererComponent.ParentComponent, savedStateHandle,
                      testRouteForComponent ->
                    parentComponent.madTestRendererComponentFactory().create(savedStateHandle,
                        testRouteForComponent)
                  }

                  handleNavigation(this, madTestRendererComponent.navEventNavigator)
                }

                val renderer = madTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, madTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

            @Module
            @ContributesTo(TestDestinationScope::class)
            public object MadTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  MadTestRendererFragment>()
            }

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface MadTestScreenNavEntryComponent : Closeable {
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
                    testRoute: TestRoute): MadTestScreenNavEntryComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestScreenNavEntryModule {
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
                  MadTestScreenNavEntryComponent.ParentComponent, savedStateHandle, testRoute ->
                parentComponent.madTestScreenNavEntryComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @ContributesTo(TestDestinationScope::class)
            @OptIn(InternalCodegenApi::class)
            public interface MadTestScreenNavEntryNavDestinationComponent : NavDestinationComponent
            
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
            import com.freeletics.mad.codegen.fragment.RendererDestination
            import com.freeletics.mad.codegen.fragment.DestinationType
            import com.freeletics.mad.codegen.NavEntryComponent
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
            import com.freeletics.mad.codegen.AppScope
            import com.freeletics.mad.codegen.NavEntry
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.`internal`.NavDestinationComponent
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.codegen.`internal`.navEntryComponent
            import com.freeletics.mad.codegen.fragment.`internal`.component
            import com.freeletics.mad.navigation.NavEventNavigator
            import com.freeletics.mad.navigation.`internal`.InternalNavigationApi
            import com.freeletics.mad.navigation.`internal`.NavigationExecutor
            import com.freeletics.mad.navigation.fragment.NavDestination
            import com.freeletics.mad.navigation.fragment.ScreenDestination
            import com.freeletics.mad.navigation.fragment.handleNavigation
            import com.freeletics.mad.navigation.fragment.requireRoute
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
            public interface MadTestRendererComponent : Closeable {
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
                    testRoute: TestRoute): MadTestRendererComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun madTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface MadTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class MadTestRendererFragment : Fragment() {
              private lateinit var madTestRendererComponent: MadTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestRendererComponent.isInitialized) {
                  val testRoute = requireRoute<TestRoute>()
                  madTestRendererComponent = component(AppScope::class, AppScope::class, testRoute) {
                      parentComponent: MadTestRendererComponent.ParentComponent, savedStateHandle,
                      testRouteForComponent ->
                    parentComponent.madTestRendererComponentFactory().create(savedStateHandle,
                        testRouteForComponent)
                  }

                  handleNavigation(this, madTestRendererComponent.navEventNavigator)
                }

                val renderer = madTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, madTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

            @Module
            @ContributesTo(AppScope::class)
            public object MadTestRendererNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute,
                  MadTestRendererFragment>()
            }

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = AppScope::class,
            )
            public interface MadTestScreenNavEntryComponent : Closeable {
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
                    testRoute: TestRoute): MadTestScreenNavEntryComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun madTestScreenNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestScreenNavEntryModule {
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
                  parentComponent: MadTestScreenNavEntryComponent.ParentComponent, savedStateHandle,
                  testRoute ->
                parentComponent.madTestScreenNavEntryComponentFactory().create(savedStateHandle, testRoute)
              }
            }

            @ContributesTo(AppScope::class)
            @OptIn(InternalCodegenApi::class)
            public interface MadTestScreenNavEntryNavDestinationComponent : NavDestinationComponent
            
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
            import com.freeletics.mad.codegen.fragment.RendererFragment
            import com.freeletics.mad.codegen.fragment.DestinationType
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
            import com.freeletics.mad.codegen.ScopeTo
            import com.freeletics.mad.codegen.`internal`.InternalCodegenApi
            import com.freeletics.mad.codegen.fragment.`internal`.component
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
            public interface MadTestRendererComponent : Closeable {
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
                    arguments: Bundle): MadTestRendererComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun madTestRendererComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface MadTestRendererModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }
            
            @OptIn(InternalCodegenApi::class)
            public class MadTestRendererFragment : DialogFragment() {
              private lateinit var madTestRendererComponent: MadTestRendererComponent

              override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?,
              ): View {
                if (!::madTestRendererComponent.isInitialized) {
                  val arguments = requireArguments()
                  madTestRendererComponent = component(TestParentScope::class, arguments) { parentComponent:
                      MadTestRendererComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                    parentComponent.madTestRendererComponentFactory().create(savedStateHandle,
                        argumentsForComponent)
                  }
                }
            
                val renderer = madTestRendererComponent.testRendererFactory.inflate(inflater, container)
                connect(renderer, madTestRendererComponent.testStateMachine)
                return renderer.rootView
              }
            }

        """.trimIndent()

        test(dialogFragment, "com/test/TestRenderer.kt", source, expected)
    }
}
