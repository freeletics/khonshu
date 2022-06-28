package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.NavEntryData
import com.squareup.kotlinpoet.ClassName
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class NavEntryFileGeneratorTest {

    private val full = NavEntryData(
        packageName = "com.test",
        scope = ClassName("com.test", "TestFlowScope"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        destinationScope = ClassName("com.test", "TestDestinationScope"),
        route = ClassName("com.test", "TestRoute"),
        coroutinesEnabled = true,
        rxJavaEnabled = true,
    )

    @Test
    fun `generates code for full NavEntryData`() {
        val actual = FileGenerator().generate(full).toString()

        val expected = """
            package com.test

            import android.content.Context
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.destinationId
            import com.freeletics.mad.navigator.`internal`.toRoute
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModel
            import com.squareup.anvil.annotations.ContributesMultibinding
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import io.reactivex.disposables.CompositeDisposable
            import java.io.Closeable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class,
            )
            public interface NavEntryTestFlowScopeComponent {
              @get:NavEntry(TestFlowScope::class)
              public val closeables: Set<Closeable>
    
              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestFlowScope::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestFlowScope::class)
                    testRoute: TestRoute): NavEntryTestFlowScopeComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun navEntryTestFlowScopeComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestFlowScope::class)
            public interface NavEntryTestFlowScopeModule {
              @Multibinds
              @NavEntry(TestFlowScope::class)
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestFlowScope::class)
                @NavEntry(TestFlowScope::class)
                public fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

                @Provides
                @IntoSet
                @NavEntry(TestFlowScope::class)
                public fun bindCompositeDisposable(@NavEntry(TestFlowScope::class)
                    compositeDisposable: CompositeDisposable): Closeable = Closeable {
                  compositeDisposable.clear()
                }
            
                @Provides
                @ScopeTo(TestFlowScope::class)
                @NavEntry(TestFlowScope::class)
                public fun provideCoroutineScope(): CoroutineScope = MainScope()

                @Provides
                @IntoSet
                @NavEntry(TestFlowScope::class)
                public fun bindCoroutineScope(@NavEntry(TestFlowScope::class) coroutineScope: CoroutineScope):
                    Closeable = Closeable {
                  coroutineScope.cancel()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowScopeViewModel(
              parentComponent: NavEntryTestFlowScopeComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: NavEntryTestFlowScopeComponent =
                  parentComponent.navEntryTestFlowScopeComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestFlowScope::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestFlowScopeComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(TestRoute::class.destinationId())
                val route: TestRoute = entry.arguments!!.toRoute()
                val viewModel = viewModel(entry, context, TestParentScope::class, TestDestinationScope::class,
                    route, findEntry, ::TestFlowScopeViewModel)
                return viewModel.component
              }
            }

        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates code for NavEntryData without coroutines`() {
        val withoutCoroutines = full.copy(coroutinesEnabled = false)
        val actual = FileGenerator().generate(withoutCoroutines).toString()

        val expected = """
            package com.test

            import android.content.Context
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.destinationId
            import com.freeletics.mad.navigator.`internal`.toRoute
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModel
            import com.squareup.anvil.annotations.ContributesMultibinding
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Provides
            import dagger.multibindings.IntoSet
            import dagger.multibindings.Multibinds
            import io.reactivex.disposables.CompositeDisposable
            import java.io.Closeable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set

            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class,
            )
            public interface NavEntryTestFlowScopeComponent {
              @get:NavEntry(TestFlowScope::class)
              public val closeables: Set<Closeable>

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestFlowScope::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestFlowScope::class)
                    testRoute: TestRoute): NavEntryTestFlowScopeComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun navEntryTestFlowScopeComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestFlowScope::class)
            public interface NavEntryTestFlowScopeModule {
              @Multibinds
              @NavEntry(TestFlowScope::class)
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestFlowScope::class)
                @NavEntry(TestFlowScope::class)
                public fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

                @Provides
                @IntoSet
                @NavEntry(TestFlowScope::class)
                public fun bindCompositeDisposable(@NavEntry(TestFlowScope::class)
                    compositeDisposable: CompositeDisposable): Closeable = Closeable {
                  compositeDisposable.clear()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowScopeViewModel(
              parentComponent: NavEntryTestFlowScopeComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: NavEntryTestFlowScopeComponent =
                  parentComponent.navEntryTestFlowScopeComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestFlowScope::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestFlowScopeComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(TestRoute::class.destinationId())
                val route: TestRoute = entry.arguments!!.toRoute()
                val viewModel = viewModel(entry, context, TestParentScope::class, TestDestinationScope::class,
                    route, findEntry, ::TestFlowScopeViewModel)
                return viewModel.component
              }
            }

        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates code for NavEntryData without rxjava`() {
        val withoutRxJava = full.copy(rxJavaEnabled = false)
        val actual = FileGenerator().generate(withoutRxJava).toString()

        val expected = """
            package com.test

            import android.content.Context
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.navigator.`internal`.InternalNavigatorApi
            import com.freeletics.mad.navigator.`internal`.destinationId
            import com.freeletics.mad.navigator.`internal`.toRoute
            import com.freeletics.mad.whetstone.NavEntry
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModel
            import com.squareup.anvil.annotations.ContributesMultibinding
            import com.squareup.anvil.annotations.ContributesSubcomponent
            import com.squareup.anvil.annotations.ContributesTo
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
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class,
            )
            public interface NavEntryTestFlowScopeComponent {
              @get:NavEntry(TestFlowScope::class)
              public val closeables: Set<Closeable>

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestFlowScope::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestFlowScope::class)
                    testRoute: TestRoute): NavEntryTestFlowScopeComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun navEntryTestFlowScopeComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestFlowScope::class)
            public interface NavEntryTestFlowScopeModule {
              @Multibinds
              @NavEntry(TestFlowScope::class)
              public fun bindCancellable(): Set<Closeable>

              public companion object {
                @Provides
                @ScopeTo(TestFlowScope::class)
                @NavEntry(TestFlowScope::class)
                public fun provideCoroutineScope(): CoroutineScope = MainScope()

                @Provides
                @IntoSet
                @NavEntry(TestFlowScope::class)
                public fun bindCoroutineScope(@NavEntry(TestFlowScope::class) coroutineScope: CoroutineScope):
                    Closeable = Closeable {
                  coroutineScope.cancel()
                }
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowScopeViewModel(
              parentComponent: NavEntryTestFlowScopeComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: NavEntryTestFlowScopeComponent =
                  parentComponent.navEntryTestFlowScopeComponentFactory().create(savedStateHandle, testRoute)

              public override fun onCleared(): Unit {
                component.closeables.forEach {
                  it.close()
                }
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestFlowScope::class)
            @ContributesMultibinding(
              TestDestinationScope::class,
              NavEntryComponentGetter::class,
            )
            public class TestFlowScopeComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(TestRoute::class.destinationId())
                val route: TestRoute = entry.arguments!!.toRoute()
                val viewModel = viewModel(entry, context, TestParentScope::class, TestDestinationScope::class,
                    route, findEntry, ::TestFlowScopeViewModel)
                return viewModel.component
              }
            }

        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }
}
