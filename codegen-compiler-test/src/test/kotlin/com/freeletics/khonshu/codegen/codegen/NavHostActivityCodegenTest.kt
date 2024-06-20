@file:Suppress("RedundantVisibilityModifier", "TestFunctionName")

package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.ComposableParameter
import com.freeletics.khonshu.codegen.NavHostActivityData
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

internal class NavHostActivityCodegenTest {

    private val data = NavHostActivityData(
        baseName = "Test",
        packageName = "com.test",
        scope = ClassName("com.test", "TestScreen"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        stateMachine = ClassName("com.test", "TestStateMachine"),
        activityBaseClass = ClassName("androidx.activity", "ComponentActivity"),
        navHostParameter = ComposableParameter(
            "navHost",
            ClassName("com.freeletics.khonshu.codegen", "SimpleNavHost"),
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
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
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
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Intent
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.`internal`.createHostNavigator
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.jvm.JvmSuppressWildcards
            import kotlin.reflect.KClass
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance viewModel: StackEntryStoreViewModel,
                  @BindsInstance @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @BindsInstance intent: Intent,
                ): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestComponentProvider(
              private final val activity: ComponentActivity,
            ) : ActivityComponentProvider {
              override fun <C> provide(scope: KClass<*>): C = component(activity, scope, TestScreen::class,
                  TestParentScope::class) { parentComponent: KhonshuTestComponent.ParentComponent,
                  savedStateHandle ->
                val viewModel = ViewModelProvider(activity,
                    SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                parentComponent.khonshuTestComponentFactory().create(viewModel, savedStateHandle,
                    activity.intent)
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Module
            @ContributesTo(TestScreen::class)
            public object KhonshuTestActivityModule {
              @Provides
              public fun provideImmutableNavDestinations(destinations: @JvmSuppressWildcards
                  Set<NavDestination>): ImmutableSet<NavDestination> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: @JvmSuppressWildcards ImmutableSet<NavDestination>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val componentProvider = remember {
                    KhonshuTestComponentProvider(this)
                  }
                  val component = remember(componentProvider) {
                    componentProvider.provide<KhonshuTestComponent>(TestScreen::class)
                  }
                  KhonshuTest(component) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityComponentProvider provides componentProvider) {
                      NavHost(
                        navigator = remember(component) { component.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: SimpleNavHost) {
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
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
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
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Intent
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.ActivityScope
            import com.freeletics.khonshu.codegen.AppScope
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.`internal`.createHostNavigator
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.jvm.JvmSuppressWildcards
            import kotlin.reflect.KClass
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(ActivityScope::class)
            @ContributesSubcomponent(
              scope = ActivityScope::class,
              parentScope = AppScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @get:ForScope(ActivityScope::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance viewModel: StackEntryStoreViewModel,
                  @BindsInstance @ForScope(ActivityScope::class) savedStateHandle: SavedStateHandle,
                  @BindsInstance intent: Intent,
                ): KhonshuTestComponent
              }

              @ContributesTo(AppScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestComponentProvider(
              private final val activity: ComponentActivity,
            ) : ActivityComponentProvider {
              override fun <C> provide(scope: KClass<*>): C = component(activity, scope, ActivityScope::class,
                  AppScope::class) { parentComponent: KhonshuTestComponent.ParentComponent, savedStateHandle ->
                val viewModel = ViewModelProvider(activity,
                    SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                parentComponent.khonshuTestComponentFactory().create(viewModel, savedStateHandle,
                    activity.intent)
              }
            }

            @Module
            @ContributesTo(ActivityScope::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(ActivityScope::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Module
            @ContributesTo(ActivityScope::class)
            public object KhonshuTestActivityModule {
              @Provides
              public fun provideImmutableNavDestinations(destinations: @JvmSuppressWildcards
                  Set<NavDestination>): ImmutableSet<NavDestination> = destinations.toImmutableSet()

              @Provides
              @SingleIn(ActivityScope::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: @JvmSuppressWildcards ImmutableSet<NavDestination>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val componentProvider = remember {
                    KhonshuTestComponentProvider(this)
                  }
                  val component = remember(componentProvider) {
                    componentProvider.provide<KhonshuTestComponent>(ActivityScope::class)
                  }
                  KhonshuTest(component) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityComponentProvider provides componentProvider) {
                      NavHost(
                        navigator = remember(component) { component.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: SimpleNavHost) {
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
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
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
                navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Intent
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.`internal`.createHostNavigator
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.other.TestClass2
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.String
            import kotlin.Unit
            import kotlin.collections.Map
            import kotlin.collections.Set
            import kotlin.jvm.JvmSuppressWildcards
            import kotlin.reflect.KClass
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTest2Component : Closeable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              public val testClass: TestClass

              public val test: TestClass2

              public val testSet: Set<String>

              public val testMap: Map<String, Int>

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance viewModel: StackEntryStoreViewModel,
                  @BindsInstance @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @BindsInstance intent: Intent,
                ): KhonshuTest2Component
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTest2ComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTest2ComponentProvider(
              private final val activity: ComponentActivity,
            ) : ActivityComponentProvider {
              override fun <C> provide(scope: KClass<*>): C = component(activity, scope, TestScreen::class,
                  TestParentScope::class) { parentComponent: KhonshuTest2Component.ParentComponent,
                  savedStateHandle ->
                val viewModel = ViewModelProvider(activity,
                    SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                parentComponent.khonshuTest2ComponentFactory().create(viewModel, savedStateHandle,
                    activity.intent)
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTest2Module {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Module
            @ContributesTo(TestScreen::class)
            public object KhonshuTest2ActivityModule {
              @Provides
              public fun provideImmutableNavDestinations(destinations: @JvmSuppressWildcards
                  Set<NavDestination>): ImmutableSet<NavDestination> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: @JvmSuppressWildcards ImmutableSet<NavDestination>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTest2Activity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val componentProvider = remember {
                    KhonshuTest2ComponentProvider(this)
                  }
                  val component = remember(componentProvider) {
                    componentProvider.provide<KhonshuTest2Component>(TestScreen::class)
                  }
                  KhonshuTest2(component) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityComponentProvider provides componentProvider) {
                      NavHost(
                        navigator = remember(component) { component.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest2(component: KhonshuTest2Component, navHost: SimpleNavHost) {
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
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
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
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Intent
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.`internal`.createHostNavigator
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.collections.Set
            import kotlin.jvm.JvmSuppressWildcards
            import kotlin.reflect.KClass
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance viewModel: StackEntryStoreViewModel,
                  @BindsInstance @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @BindsInstance intent: Intent,
                ): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestComponentProvider(
              private final val activity: ComponentActivity,
            ) : ActivityComponentProvider {
              override fun <C> provide(scope: KClass<*>): C = component(activity, scope, TestScreen::class,
                  TestParentScope::class) { parentComponent: KhonshuTestComponent.ParentComponent,
                  savedStateHandle ->
                val viewModel = ViewModelProvider(activity,
                    SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                parentComponent.khonshuTestComponentFactory().create(viewModel, savedStateHandle,
                    activity.intent)
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Module
            @ContributesTo(TestScreen::class)
            public object KhonshuTestActivityModule {
              @Provides
              public fun provideImmutableNavDestinations(destinations: @JvmSuppressWildcards
                  Set<NavDestination>): ImmutableSet<NavDestination> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: @JvmSuppressWildcards ImmutableSet<NavDestination>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val componentProvider = remember {
                    KhonshuTestComponentProvider(this)
                  }
                  val component = remember(componentProvider) {
                    componentProvider.provide<KhonshuTestComponent>(TestScreen::class)
                  }
                  KhonshuTest(component) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityComponentProvider provides componentProvider) {
                      NavHost(
                        navigator = remember(component) { component.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: SimpleNavHost) {
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
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.NavRoot
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
              navHost: SimpleNavHost,
            ) {
                navHost(Modifier) { _, _ -> }
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Intent
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.`internal`.createHostNavigator
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.jvm.JvmSuppressWildcards
            import kotlin.reflect.KClass
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance viewModel: StackEntryStoreViewModel,
                  @BindsInstance @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @BindsInstance intent: Intent,
                ): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestComponentProvider(
              private final val activity: ComponentActivity,
            ) : ActivityComponentProvider {
              override fun <C> provide(scope: KClass<*>): C = component(activity, scope, TestScreen::class,
                  TestParentScope::class) { parentComponent: KhonshuTestComponent.ParentComponent,
                  savedStateHandle ->
                val viewModel = ViewModelProvider(activity,
                    SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                parentComponent.khonshuTestComponentFactory().create(viewModel, savedStateHandle,
                    activity.intent)
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Module
            @ContributesTo(TestScreen::class)
            public object KhonshuTestActivityModule {
              @Provides
              public fun provideImmutableNavDestinations(destinations: @JvmSuppressWildcards
                  Set<NavDestination>): ImmutableSet<NavDestination> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: @JvmSuppressWildcards ImmutableSet<NavDestination>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val componentProvider = remember {
                    KhonshuTestComponentProvider(this)
                  }
                  val component = remember(componentProvider) {
                    componentProvider.provide<KhonshuTestComponent>(TestScreen::class)
                  }
                  KhonshuTest(component) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityComponentProvider provides componentProvider) {
                      NavHost(
                        navigator = remember(component) { component.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: SimpleNavHost) {
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
                  navHost = navHost,
                )
              }
            }

        """.trimIndent()

        test(withoutSendAction, "com/test/Test.kt", source, expected)
    }

    @Test
    fun `generates code for NavHostActivityData with lambda parameter`() {
        // nothing changes on the data side
        val withLambdaParameter = data

        @Language("kotlin")
        val source = """
            package com.test

            import androidx.activity.ComponentActivity
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import com.freeletics.khonshu.codegen.NavHostActivity
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.navigation.BaseRoute
            import com.freeletics.khonshu.navigation.NavRoot
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
              navHost: @Composable (Modifier, ((NavRoot, BaseRoute) -> Unit)?) -> Unit,
            ) {
                navHost(Modifier)  { _, _ -> }
            }
        """.trimIndent()

        @Language("kotlin")
        val expected = """
            package com.test

            import android.content.Intent
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.SavedStateViewModelFactory
            import androidx.lifecycle.ViewModelProvider
            import com.freeletics.khonshu.codegen.SimpleNavHost
            import com.freeletics.khonshu.codegen.`internal`.ActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.InternalCodegenApi
            import com.freeletics.khonshu.codegen.`internal`.LocalActivityComponentProvider
            import com.freeletics.khonshu.codegen.`internal`.asComposeState
            import com.freeletics.khonshu.codegen.`internal`.component
            import com.freeletics.khonshu.navigation.HostNavigator
            import com.freeletics.khonshu.navigation.NavDestination
            import com.freeletics.khonshu.navigation.NavHost
            import com.freeletics.khonshu.navigation.NavRoot
            import com.freeletics.khonshu.navigation.`internal`.InternalNavigationCodegenApi
            import com.freeletics.khonshu.navigation.`internal`.StackEntryStoreViewModel
            import com.freeletics.khonshu.navigation.`internal`.createHostNavigator
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.optional.ForScope
            import com.squareup.anvil.annotations.optional.SingleIn
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlin.jvm.JvmSuppressWildcards
            import kotlin.reflect.KClass
            import kotlinx.collections.immutable.ImmutableSet
            import kotlinx.collections.immutable.toImmutableSet
            import kotlinx.coroutines.launch

            @OptIn(InternalCodegenApi::class)
            @SingleIn(TestScreen::class)
            @ContributesSubcomponent(
              scope = TestScreen::class,
              parentScope = TestParentScope::class,
            )
            public interface KhonshuTestComponent : Closeable {
              public val testStateMachine: TestStateMachine

              public val hostNavigator: HostNavigator

              @get:ForScope(TestScreen::class)
              public val closeables: Set<Closeable>

              override fun close() {
                closeables.forEach {
                  it.close()
                }
              }

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance viewModel: StackEntryStoreViewModel,
                  @BindsInstance @ForScope(TestScreen::class) savedStateHandle: SavedStateHandle,
                  @BindsInstance intent: Intent,
                ): KhonshuTestComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun khonshuTestComponentFactory(): Factory
              }
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestComponentProvider(
              private final val activity: ComponentActivity,
            ) : ActivityComponentProvider {
              override fun <C> provide(scope: KClass<*>): C = component(activity, scope, TestScreen::class,
                  TestParentScope::class) { parentComponent: KhonshuTestComponent.ParentComponent,
                  savedStateHandle ->
                val viewModel = ViewModelProvider(activity,
                    SavedStateViewModelFactory())[StackEntryStoreViewModel::class.java]
                parentComponent.khonshuTestComponentFactory().create(viewModel, savedStateHandle,
                    activity.intent)
              }
            }

            @Module
            @ContributesTo(TestScreen::class)
            public interface KhonshuTestModule {
              @Multibinds
              @ForScope(TestScreen::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @Module
            @ContributesTo(TestScreen::class)
            public object KhonshuTestActivityModule {
              @Provides
              public fun provideImmutableNavDestinations(destinations: @JvmSuppressWildcards
                  Set<NavDestination>): ImmutableSet<NavDestination> = destinations.toImmutableSet()

              @Provides
              @SingleIn(TestScreen::class)
              @OptIn(InternalNavigationCodegenApi::class)
              public fun provideHostNavigator(
                viewModel: StackEntryStoreViewModel,
                startRoot: NavRoot,
                destinations: @JvmSuppressWildcards ImmutableSet<NavDestination>,
              ): HostNavigator = createHostNavigator(viewModel, startRoot, destinations)
            }

            @OptIn(InternalCodegenApi::class)
            public class KhonshuTestActivity : ComponentActivity() {
              override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                  val componentProvider = remember {
                    KhonshuTestComponentProvider(this)
                  }
                  val component = remember(componentProvider) {
                    componentProvider.provide<KhonshuTestComponent>(TestScreen::class)
                  }
                  KhonshuTest(component) { modifier, destinationChangedCallback ->
                    CompositionLocalProvider(LocalActivityComponentProvider provides componentProvider) {
                      NavHost(
                        navigator = remember(component) { component.hostNavigator },
                        modifier = modifier,
                        destinationChangedCallback = destinationChangedCallback,
                      )
                    }
                  }
                }
              }
            }

            @Composable
            @OptIn(InternalCodegenApi::class)
            private fun KhonshuTest(component: KhonshuTestComponent, navHost: SimpleNavHost) {
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
                  navHost = navHost,
                )
              }
            }

        """.trimIndent()

        test(withLambdaParameter, "com/test/Test.kt", source, expected)
    }
}
