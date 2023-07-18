@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.NavEntryData
import com.freeletics.khonshu.codegen.Navigation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class FileGeneratorTestCompose {

    private val navigation = Navigation.Compose(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "SCREEN",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = ComposeScreenData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = null,
        navEntryData = null,
        composableParameter = emptyList(),
        stateParameter = ComposableParameter("state", ClassName("com.test", "TestState")),
        sendActionParameter = ComposableParameter(
            "sendAction",
            Function1::class.asClassName().parameterizedBy(
                ClassName("com.test", "TestAction"),
                UNIT,
            ),
        ),
    )

    private val navEntryData = NavEntryData(
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        navigation = navigation,
    )

    @Test
    fun `generates code for ComposeScreenData`() {
        @Language("kotlin")
        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.ComposeScreen
            import com.test.parent.TestParentScope
            
            @ComposeScreen(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.LocalContext
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            public fun KhonshuTest(arguments: Bundle) {
              val context = LocalContext.current
              val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
              val component = remember(viewModelStoreOwner, context, arguments) {
                component(viewModelStoreOwner, context, TestParentScope::class, arguments) { parentComponent:
                    KhonshuTestComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                  parentComponent.khonshuTestComponentFactory().create(savedStateHandle, argumentsForComponent)
                }
              }

              KhonshuTest(component)
            }
            
            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }
            
        """.trimIndent()

        test(data, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with navigation`() {
        val withNavigation = data.copy(
            scope = navigation.route,
            navigation = navigation,
        )

        @Language("kotlin")
        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.ComposeDestination
            import com.freeletics.khonshu.codegen.compose.DestinationType
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            
            @ComposeDestination(
              route = TestRoute::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
              destinationScope = TestDestinationScope::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.LocalContext
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.compose.LocalNavigationExecutor
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavigationSetup
            import com.freeletics.khonshu.navigation.compose.ScreenDestination
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
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationApi::class)
            public fun KhonshuTest(testRoute: TestRoute) {
              val context = LocalContext.current
              val executor = LocalNavigationExecutor.current
              val component = remember(context, executor, testRoute) {
                component(testRoute.destinationId, testRoute, executor, context, TestParentScope::class,
                    TestDestinationScope::class) { parentComponent: KhonshuTestComponent.ParentComponent,
                    savedStateHandle, testRouteForComponent ->
                  parentComponent.khonshuTestComponentFactory().create(savedStateHandle, testRouteForComponent)
                }
              }

              NavigationSetup(component.navEventNavigator)

              KhonshuTest(component)
            }
            
            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }
            
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute> {
                KhonshuTest(it)
              }
            }
            
        """.trimIndent()

        test(withNavigation, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with navigation, destination and navEntry`() {
        val withNavEntry = data.copy(
            scope = navigation.route,
            navigation = navigation,
            navEntryData = navEntryData,
        )

        @Language("kotlin")
        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.ComposeDestination
            import com.freeletics.khonshu.codegen.compose.DestinationType
            import com.freeletics.khonshu.codegen.NavEntryComponent
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            
            @ComposeDestination(
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
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Context
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.LocalContext
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.NavEntry
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.NavDestinationComponent
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.codegen.`internal`.navEntryComponent
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.compose.LocalNavigationExecutor
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavigationSetup
            import com.freeletics.khonshu.navigation.compose.ScreenDestination
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
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationApi::class)
            public fun KhonshuTest(testRoute: TestRoute) {
              val context = LocalContext.current
              val executor = LocalNavigationExecutor.current
              val component = remember(context, executor, testRoute) {
                component(testRoute.destinationId, testRoute, executor, context, TestParentScope::class,
                    TestDestinationScope::class) { parentComponent: KhonshuTestComponent.ParentComponent,
                    savedStateHandle, testRouteForComponent ->
                  parentComponent.khonshuTestComponentFactory().create(savedStateHandle, testRouteForComponent)
                }
              }

              NavigationSetup(component.navEventNavigator)

              KhonshuTest(component)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }

            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute> {
                KhonshuTest(it)
              }
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

        test(withNavEntry, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with default values`() {
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
            
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.ComposeDestination
            import com.freeletics.khonshu.codegen.compose.DestinationType
            import com.freeletics.khonshu.codegen.NavEntryComponent
            
            @ComposeDestination(
              route = TestRoute::class,
              stateMachine = TestStateMachine::class,
              destinationType = DestinationType.SCREEN,
            )
            @NavEntryComponent(
              scope = TestScreen::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Context
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.LocalContext
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.AppScope
            import com.freeletics.khonshu.codegen.NavEntry
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.NavDestinationComponent
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetter
            import com.freeletics.khonshu.codegen.`internal`.NavEntryComponentGetterKey
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.codegen.`internal`.navEntryComponent
            import com.freeletics.khonshu.navigation.NavEventNavigator
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationApi
            import com.freeletics.khonshu.navigation.`internal`.NavigationExecutor
            import com.freeletics.khonshu.navigation.`internal`.destinationId
            import com.freeletics.khonshu.navigation.compose.LocalNavigationExecutor
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavigationSetup
            import com.freeletics.khonshu.navigation.compose.ScreenDestination
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
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = AppScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val navEventNavigator: NavEventNavigator

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationApi::class)
            public fun KhonshuTest(testRoute: TestRoute) {
              val context = LocalContext.current
              val executor = LocalNavigationExecutor.current
              val component = remember(context, executor, testRoute) {
                component(testRoute.destinationId, testRoute, executor, context, AppScope::class,
                    AppScope::class) { parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle,
                    testRouteForComponent ->
                  parentComponent.khonshuTestComponentFactory().create(savedStateHandle, testRouteForComponent)
                }
              }

              NavigationSetup(component.navEventNavigator)

              KhonshuTest(component)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }

            @Module
            @ContributesTo(AppScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              public fun provideNavDestination(): NavDestination = ScreenDestination<TestRoute> {
                KhonshuTest(it)
              }
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

        test(withDefaultValues, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with Composable Dependencies`() {
        val withInjectedParameters = data.copy(
            baseName = "Test2",
            composableParameter = listOf(
                ComposableParameter(
                    name = "testClass",
                    typeName = ClassName("com.test", "TestClass"),
                ),
                ComposableParameter(
                    name = "test",
                    typeName = ClassName("com.test.other", "TestClass2"),
                ),
                ComposableParameter(
                    name = "testSet",
                    typeName = SET.parameterizedBy(STRING),
                ),
                ComposableParameter(
                    name = "testMap",
                    typeName = MAP.parameterizedBy(STRING, INT),
                ),
            ),
        )

        @Language("kotlin")
        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.ComposeScreen
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            
            @ComposeScreen(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test2(
                state: TestState,
                sendAction: (TestAction) -> Unit,
                testClass: TestClass,
                test: TestClass2,
                testSet: Set<String>,
                testMap: Map<String, Int>,
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.LocalContext
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.String
            import kotlin.collections.Map
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTest2Component : Closeable {
              public val testStateMachine: TestStateMachine

              public val testClass: TestClass

              public val test: TestClass2
            
              public val testSet: Set<String>

              public val testMap: Map<String, Int>

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): KhonshuTest2Component
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTest2ComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTest2Module {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            public fun KhonshuTest2(arguments: Bundle) {
              val context = LocalContext.current
              val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
              val component = remember(viewModelStoreOwner, context, arguments) {
                component(viewModelStoreOwner, context, TestParentScope::class, arguments) { parentComponent:
                    KhonshuTest2Component.ParentComponent, savedStateHandle, argumentsForComponent ->
                  parentComponent.khonshuTest2ComponentFactory().create(savedStateHandle, argumentsForComponent)
                }
              }

              KhonshuTest2(component)
            }
            
            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest2(component: KhonshuTest2Component) {
              val testClass = remember { component.testClass }
              val test = remember { component.test }
              val testSet = remember { component.testSet }
              val testMap = remember { component.testMap }
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test2(
                  testClass = testClass,
                  test = test,
                  testSet = testSet,
                  testMap = testMap,
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }
            
        """.trimIndent()

        test(withInjectedParameters, "com/test/Test2.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData without sendAction`() {
        val withoutSendAction = data.copy(
            sendActionParameter = null,
        )

        @Language("kotlin")
        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.ComposeScreen
            import com.test.parent.TestParentScope
            
            @ComposeScreen(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.ui.platform.LocalContext
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
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
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            public fun KhonshuTest(arguments: Bundle) {
              val context = LocalContext.current
              val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
              val component = remember(viewModelStoreOwner, context, arguments) {
                component(viewModelStoreOwner, context, TestParentScope::class, arguments) { parentComponent:
                    KhonshuTestComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                  parentComponent.khonshuTestComponentFactory().create(savedStateHandle, argumentsForComponent)
                }
              }

              KhonshuTest(component)
            }
            
            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                )
              }
            }
            
        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData without state`() {
        val withoutSendAction = data.copy(
            stateParameter = null,
        )

        @Language("kotlin")
        val source = """
            package com.test
            
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.ComposeScreen
            import com.test.parent.TestParentScope
            
            @ComposeScreen(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              sendAction: (TestAction) -> Unit
            ) {}
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.platform.LocalContext
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
            import com.freeletics.khonshu.codegen.ScopeTo
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @ScopeTo(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val closeables: Set<Closeable>
    
              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance savedStateHandle: SavedStateHandle, @BindsInstance
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            public fun KhonshuTest(arguments: Bundle) {
              val context = LocalContext.current
              val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
              val component = remember(viewModelStoreOwner, context, arguments) {
                component(viewModelStoreOwner, context, TestParentScope::class, arguments) { parentComponent:
                    KhonshuTestComponent.ParentComponent, savedStateHandle, argumentsForComponent ->
                  parentComponent.khonshuTestComponentFactory().create(savedStateHandle, argumentsForComponent)
                }
              }

              KhonshuTest(component)
            }
            
            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                )
              }
            }
            
        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }
}
