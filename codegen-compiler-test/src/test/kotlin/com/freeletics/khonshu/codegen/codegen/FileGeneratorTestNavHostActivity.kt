@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.navigation.NavRoot
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class FileGeneratorTestNavHostActivity {

    private val data = NavHostActivityData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        activityBaseClass = ClassName("androidx.activity", "ComponentActivity"),
        navHostParameter = ComposableParameter(
            "navHost",
            LambdaTypeName.get(
                null,
                NavRoot::class.asClassName(),
                LambdaTypeName.get(null, ClassName("com.freeletics.khonshu.navigation", "BaseRoute"), returnType = UNIT)
                    .copy(nullable = true),
                returnType = UNIT,
            ),
        ),
        stateParameter = ComposableParameter("state", ClassName("com.test", "TestState")),
        sendActionParameter = ComposableParameter(
            "sendAction",
            Function1::class.asClassName().parameterizedBy(
                ClassName("com.test", "TestAction"),
                UNIT,
            ),
        ),
        composableParameter = emptyList(),
    )

    @Test
    fun `generates code for NavHostActivityData`() {
        @Language("kotlin")
        val source = """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.NavHostActivity
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit,
              navHost: @Composable (NavRoot, ((BaseRoute) -> Unit)?) -> Unit,
            ) {
                navHost(TestRoot()) {}
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavHost
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val destinations: Set<NavDestination>

              public val deepLinkHandlers: Set<DeepLinkHandler>

              public val deepLinkPrefixes: Set<DeepLinkHandler.Prefix>

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
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (!::khonshuTestComponent.isInitialized) {
                  khonshuTestComponent = component(this, this, TestParentScope::class, intent.extras) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle, extras ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle, extras)
                  }
                }

                setContent {
                  KhonshuTest(khonshuTestComponent) { startRoute, destinationChangedCallback ->
                    NavHost(
                      startRoute,
                      khonshuTestComponent.destinations,
                      khonshuTestComponent.deepLinkHandlers,
                      khonshuTestComponent.deepLinkPrefixes,
                      destinationChangedCallback,
                    )
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: @Composable (NavRoot,
                ((BaseRoute) -> Unit)?) -> Unit) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                  navHost = navHost,
                )
              }
            }

        """.trimIndent()

        test(data, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostActivityData with default values`() {
        val withDefaultValues = data.copy(
            scope = ActivityScope::class.asClassName(),
            parentScope = AppScope::class.asClassName(),
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.compose.NavHostActivity
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot

            @NavHostActivity(
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              sendAction: (TestAction) -> Unit,
              navHost: @Composable (NavRoot, ((BaseRoute) -> Unit)?) -> Unit,
            ) {
                navHost(TestRoot()) {}
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.AppScope
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavHost
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(ActivityScope::class)
            @ContributesSubcomponent(
              scope = ActivityScope::class,
              parentScope = AppScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val destinations: Set<NavDestination>

              public val deepLinkHandlers: Set<DeepLinkHandler>

              public val deepLinkPrefixes: Set<DeepLinkHandler.Prefix>

              @get:ForScope(ActivityScope::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @ForScope(ActivityScope::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @ForScope(ActivityScope::class)
                    arguments: Bundle): KhonshuTestComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(ActivityScope::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(ActivityScope::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (!::khonshuTestComponent.isInitialized) {
                  khonshuTestComponent = component(this, this, AppScope::class, intent.extras) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle, extras ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle, extras)
                  }
                }

                setContent {
                  KhonshuTest(khonshuTestComponent) { startRoute, destinationChangedCallback ->
                    NavHost(
                      startRoute,
                      khonshuTestComponent.destinations,
                      khonshuTestComponent.deepLinkHandlers,
                      khonshuTestComponent.deepLinkPrefixes,
                      destinationChangedCallback,
                    )
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: @Composable (NavRoot,
                ((BaseRoute) -> Unit)?) -> Unit) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  state = currentState,
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                  navHost = navHost,
                )
              }
            }

        """.trimIndent()

        test(withDefaultValues, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostActivityData with Composable Dependencies`() {
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

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.NavHostActivity
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
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
                navHost: @Composable (NavRoot, ((BaseRoute) -> Unit)?) -> Unit,
            ) {
                navHost(TestRoot()) {}
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavHost
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
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
            @SingleIn(TestScreen::class)
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

              public val destinations: Set<NavDestination>

              public val deepLinkHandlers: Set<DeepLinkHandler>

              public val deepLinkPrefixes: Set<DeepLinkHandler.Prefix>

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
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTest2Activity : ComponentActivity() {
              private lateinit var khonshuTest2Component: KhonshuTest2Component

              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (!::khonshuTest2Component.isInitialized) {
                  khonshuTest2Component = component(this, this, TestParentScope::class, intent.extras) {
                      parentComponent: KhonshuTest2Component.ParentComponent, savedStateHandle, extras ->
                    parentComponent.khonshuTest2ComponentFactory().create(savedStateHandle, extras)
                  }
                }

                setContent {
                  KhonshuTest2(khonshuTest2Component) { startRoute, destinationChangedCallback ->
                    NavHost(
                      startRoute,
                      khonshuTest2Component.destinations,
                      khonshuTest2Component.deepLinkHandlers,
                      khonshuTest2Component.deepLinkPrefixes,
                      destinationChangedCallback,
                    )
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest2(component: KhonshuTest2Component, navHost: @Composable (NavRoot,
                ((BaseRoute) -> Unit)?) -> Unit) {
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
                  navHost = navHost,
                )
              }
            }

        """.trimIndent()

        test(withInjectedParameters, "com/test/Test2.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostActivityData without sendAction`() {
        val withoutSendAction = data.copy(
            sendActionParameter = null,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.NavHostActivity
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              state: TestState,
              navHost: @Composable (NavRoot, ((BaseRoute) -> Unit)?) -> Unit,
            ) {
                navHost(TestRoot()) {}
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavHost
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
            import kotlin.Unit
            import kotlin.collections.Set

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val destinations: Set<NavDestination>

              public val deepLinkHandlers: Set<DeepLinkHandler>

              public val deepLinkPrefixes: Set<DeepLinkHandler.Prefix>

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
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (!::khonshuTestComponent.isInitialized) {
                  khonshuTestComponent = component(this, this, TestParentScope::class, intent.extras) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle, extras ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle, extras)
                  }
                }

                setContent {
                  KhonshuTest(khonshuTestComponent) { startRoute, destinationChangedCallback ->
                    NavHost(
                      startRoute,
                      khonshuTestComponent.destinations,
                      khonshuTestComponent.deepLinkHandlers,
                      khonshuTestComponent.deepLinkPrefixes,
                      destinationChangedCallback,
                    )
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: @Composable (NavRoot,
                ((BaseRoute) -> Unit)?) -> Unit) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                Test(
                  state = currentState,
                  navHost = navHost,
                )
              }
            }

        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostActivityData without state`() {
        val withoutSendAction = data.copy(
            stateParameter = null,
        )

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import com.freeletics.khonshu.codegen.compose.NavHostActivity
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.test.parent.TestParentScope

            @NavHostActivity(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
              stateMachine = TestStateMachine::class,
              activityBaseClass = ComponentActivity::class,
            )
            @Composable
            @Suppress("unused_parameter")
            public fun Test(
              sendAction: (TestAction) -> Unit,
              navHost: @Composable (NavRoot, ((BaseRoute) -> Unit)?) -> Unit,
            ) {
                navHost(TestRoot()) {}
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.DeepLinkHandler
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.compose.NavDestination
            import com.freeletics.khonshu.navigation.compose.NavHost
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
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val destinations: Set<NavDestination>

              public val deepLinkHandlers: Set<DeepLinkHandler>

              public val deepLinkPrefixes: Set<DeepLinkHandler.Prefix>

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
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              private lateinit var khonshuTestComponent: KhonshuTestComponent

              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (!::khonshuTestComponent.isInitialized) {
                  khonshuTestComponent = component(this, this, TestParentScope::class, intent.extras) {
                      parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle, extras ->
                    parentComponent.khonshuTestComponentFactory().create(savedStateHandle, extras)
                  }
                }

                setContent {
                  KhonshuTest(khonshuTestComponent) { startRoute, destinationChangedCallback ->
                    NavHost(
                      startRoute,
                      khonshuTestComponent.destinations,
                      khonshuTestComponent.deepLinkHandlers,
                      khonshuTestComponent.deepLinkPrefixes,
                      destinationChangedCallback,
                    )
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: @Composable (NavRoot,
                ((BaseRoute) -> Unit)?) -> Unit) {
              val stateMachine = remember { component.testStateMachine }
              val state = stateMachine.asComposeState()
              val currentState = state.value
              if (currentState != null) {
                val scope = rememberCoroutineScope()
                Test(
                  sendAction = { scope.launch { stateMachine.dispatch(it) } },
                  navHost = navHost,
                )
              }
            }

        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }
}
