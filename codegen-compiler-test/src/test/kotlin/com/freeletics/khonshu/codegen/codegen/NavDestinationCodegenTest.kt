@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.Navigation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.test.TestOverlayRoute
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class NavDestinationCodegenTest {

    private val navigation = Navigation(
        route = ClassName("com.test", "TestRoute"),
        parentScopeIsRoute = true,
        overlay = false,
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = NavDestinationData(
        baseName = "Test",
        packageName = "com.test",
        scope = navigation.route,
        parentScope = ClassName("com.test.parent", "TestParentRoute"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        navigation = navigation,
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

    @Test
    fun `generates code for ComposeScreenData`() {
        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
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
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.ActivityResultNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavigationSetup
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val activityResultNavigator: ActivityResultNavigator

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
                    @BindsInstance testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestComponentProvider : ComponentProvider<TestRoute, KhonshuTestComponent> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: ActivityComponentProvider,
              ): KhonshuTestComponent = componentFromParentRoute(entry, snapshot, provider,
                  TestParentRoute::class) { parentComponent: KhonshuTestComponent.ParentComponent ->
                parentComponent.khonshuTestComponentFactory().create(entry.savedStateHandle, entry.route)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalActivityComponentProvider.current
              val component = remember(entry, snapshot, provider) {
                KhonshuTestComponentProvider.provide(entry, snapshot, provider)
              }

              NavigationSetup(component.activityResultNavigator)

              KhonshuTest(component)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideNavDestination(): NavDestination =
                  ScreenDestination<TestRoute>(KhonshuTestComponentProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

        """.trimIndent()

        test(data, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with default values`() {
        val navigation = navigation.copy(
            parentScopeIsRoute = false,
            destinationScope = AppScope::class.asClassName(),
        )
        val withDefaultValues = data.copy(
            scope = navigation.route,
            parentScope = ActivityScope::class.asClassName(),
            navigation = navigation,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination

            @NavDestination(
              route = TestRoute::class,
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

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.AppScope
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.ActivityResultNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavigationSetup
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = ActivityScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val activityResultNavigator: ActivityResultNavigator

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
                    @BindsInstance testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(ActivityScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestComponentProvider : ComponentProvider<TestRoute, KhonshuTestComponent> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: ActivityComponentProvider,
              ): KhonshuTestComponent = component(entry, provider, ActivityScope::class) { parentComponent:
                  KhonshuTestComponent.ParentComponent ->
                parentComponent.khonshuTestComponentFactory().create(entry.savedStateHandle, entry.route)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalActivityComponentProvider.current
              val component = remember(entry, snapshot, provider) {
                KhonshuTestComponentProvider.provide(entry, snapshot, provider)
              }

              NavigationSetup(component.activityResultNavigator)

              KhonshuTest(component)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(AppScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideNavDestination(): NavDestination =
                  ScreenDestination<TestRoute>(KhonshuTestComponentProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

        """.trimIndent()

        test(withDefaultValues, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for ComposeScreenData with overlay route`() {
        val navigation = navigation.copy(
            route = TestOverlayRoute::class.asClassName(),
            overlay = true,
        )
        val dialogData = data.copy(
            scope = navigation.route,
            navigation = navigation,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestOverlayRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
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
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.ActivityResultNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavigationSetup
            import com.freeletics.khonshu.navigation.OverlayDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestOverlayRoute::class)
            @ContributesSubcomponent(
              scope = TestOverlayRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestOverlayRoute::class)
              public val activityResultNavigator: ActivityResultNavigator

              @get:ForScope(TestOverlayRoute::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(TestOverlayRoute::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance testOverlayRoute: TestOverlayRoute):
                    KhonshuTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestComponentProvider :
                ComponentProvider<TestOverlayRoute, KhonshuTestComponent> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestOverlayRoute>,
                snapshot: StackSnapshot,
                provider: ActivityComponentProvider,
              ): KhonshuTestComponent = componentFromParentRoute(entry, snapshot, provider,
                  TestParentRoute::class) { parentComponent: KhonshuTestComponent.ParentComponent ->
                parentComponent.khonshuTestComponentFactory().create(entry.savedStateHandle, entry.route)
              }
            }

            @Module
            @ContributesTo(TestOverlayRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestOverlayRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestOverlayRoute>) {
              val provider = LocalActivityComponentProvider.current
              val component = remember(entry, snapshot, provider) {
                KhonshuTestComponentProvider.provide(entry, snapshot, provider)
              }

              NavigationSetup(component.activityResultNavigator)

              KhonshuTest(component)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideNavDestination(): NavDestination =
                  OverlayDestination<TestOverlayRoute>(KhonshuTestComponentProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

        """.trimIndent()

        test(dialogData, "com/test/Test.kt", source, expected)
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
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.other.TestClass2
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
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

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.ActivityResultNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavigationSetup
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.destination.TestDestinationScope
            import com.test.other.TestClass2
            import com.test.parent.TestParentRoute
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.String
            import kotlin.Unit
            import kotlin.collections.Map
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuTest2Component : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val activityResultNavigator: ActivityResultNavigator

              public val testClass: TestClass

              public val test: TestClass2

              public val testSet: Set<String>

              public val testMap: Map<String, Int>

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
                    @BindsInstance testRoute: TestRoute): KhonshuTest2Component
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuTest2ComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTest2ComponentProvider : ComponentProvider<TestRoute, KhonshuTest2Component> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: ActivityComponentProvider,
              ): KhonshuTest2Component = componentFromParentRoute(entry, snapshot, provider,
                  TestParentRoute::class) { parentComponent: KhonshuTest2Component.ParentComponent ->
                parentComponent.khonshuTest2ComponentFactory().create(entry.savedStateHandle, entry.route)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTest2Module {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest2(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalActivityComponentProvider.current
              val component = remember(entry, snapshot, provider) {
                KhonshuTest2ComponentProvider.provide(entry, snapshot, provider)
              }

              NavigationSetup(component.activityResultNavigator)

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
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test2(
                  testClass = testClass,
                  test = test,
                  testSet = testSet,
                  testMap = testMap,
                  state = currentState,
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTest2NavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideNavDestination(): NavDestination =
                  ScreenDestination<TestRoute>(KhonshuTest2ComponentProvider) { snapshot, route ->
                KhonshuTest2(snapshot, route)
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
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
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

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.ActivityResultNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavigationSetup
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
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
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val activityResultNavigator: ActivityResultNavigator

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
                    @BindsInstance testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestComponentProvider : ComponentProvider<TestRoute, KhonshuTestComponent> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: ActivityComponentProvider,
              ): KhonshuTestComponent = componentFromParentRoute(entry, snapshot, provider,
                  TestParentRoute::class) { parentComponent: KhonshuTestComponent.ParentComponent ->
                parentComponent.khonshuTestComponentFactory().create(entry.savedStateHandle, entry.route)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalActivityComponentProvider.current
              val component = remember(entry, snapshot, provider) {
                KhonshuTestComponentProvider.provide(entry, snapshot, provider)
              }

              NavigationSetup(component.activityResultNavigator)

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

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideNavDestination(): NavDestination =
                  ScreenDestination<TestRoute>(KhonshuTestComponentProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
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
            import com.freeletics.khonshu.codegen.NavDestination
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentRoute

            @NavDestination(
              route = TestRoute::class,
              parentScope = TestParentRoute::class,
              stateMachine = TestStateMachine::class,
              destinationScope = TestDestinationScope::class,
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

            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.ComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.componentFromParentRoute
            import com.freeletics.khonshu.navigation.ActivityResultNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavigationSetup
            import com.freeletics.khonshu.navigation.ScreenDestination
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntry
            import com.freeletics.khonshu.navigation.`internal`.StackSnapshot
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestRoute::class)
            @ContributesSubcomponent(
              scope = TestRoute::class,
              parentScope = TestParentRoute::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              @get:ForScope(TestRoute::class)
              public val activityResultNavigator: ActivityResultNavigator

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
                    @BindsInstance testRoute: TestRoute): KhonshuTestComponent
              }

              @ContributesTo(TestParentRoute::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public object KhonshuTestComponentProvider : ComponentProvider<TestRoute, KhonshuTestComponent> {
              @OptIn(InternalNavigationCodegenApi::class)
              override fun provide(
                entry: StackEntry<TestRoute>,
                snapshot: StackSnapshot,
                provider: ActivityComponentProvider,
              ): KhonshuTestComponent = componentFromParentRoute(entry, snapshot, provider,
                  TestParentRoute::class) { parentComponent: KhonshuTestComponent.ParentComponent ->
                parentComponent.khonshuTestComponentFactory().create(entry.savedStateHandle, entry.route)
              }
            }

            @Module
            @ContributesTo(TestRoute::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestRoute::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Composable
            @OptIn(InternalCodegenApi::class, InternalNavigationCodegenApi::class)
            public fun KhonshuTest(snapshot: StackSnapshot, entry: StackEntry<TestRoute>) {
              val provider = LocalActivityComponentProvider.current
              val component = remember(entry, snapshot, provider) {
                KhonshuTestComponentProvider.provide(entry, snapshot, provider)
              }

              NavigationSetup(component.activityResultNavigator)

              KhonshuTest(component)
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent) {
              val stateMachine = remember { component.testStateMachine }
              val scope = rememberCoroutineScope()
              val sendAction: (TestAction) -> Unit = remember(stateMachine, scope) {
                { scope.launch { stateMachine.dispatch(it) } }
              }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  sendAction = sendAction,
                )
              }
            }

            @OptIn(InternalCodegenApi::class)
            @Module
            @ContributesTo(TestDestinationScope::class)
            public object KhonshuTestNavDestinationModule {
              @Provides
              @IntoSet
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideNavDestination(): NavDestination =
                  ScreenDestination<TestRoute>(KhonshuTestComponentProvider) { snapshot, route ->
                KhonshuTest(snapshot, route)
              }
            }

        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }
}
