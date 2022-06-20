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
            import io.reactivex.disposables.CompositeDisposable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class,
            )
            public interface NavEntryTestFlowScopeComponent {
              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                  @BindsInstance compositeDisposable: CompositeDisposable,
                  @BindsInstance coroutineScope: CoroutineScope,
                ): NavEntryTestFlowScopeComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun navEntryTestFlowScopeComponentFactory(): Factory
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowScopeViewModel(
              parentComponent: NavEntryTestFlowScopeComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              private val scope: CoroutineScope = MainScope()

              public val component: NavEntryTestFlowScopeComponent =
                  parentComponent.navEntryTestFlowScopeComponentFactory().create(savedStateHandle, testRoute,
                  disposable, scope)

              public override fun onCleared(): Unit {
                disposable.clear()
                scope.cancel()
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
            import io.reactivex.disposables.CompositeDisposable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.Unit

            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class,
            )
            public interface NavEntryTestFlowScopeComponent {
              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                  @BindsInstance compositeDisposable: CompositeDisposable,
                ): NavEntryTestFlowScopeComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun navEntryTestFlowScopeComponentFactory(): Factory
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowScopeViewModel(
              parentComponent: NavEntryTestFlowScopeComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              public val component: NavEntryTestFlowScopeComponent =
                  parentComponent.navEntryTestFlowScopeComponentFactory().create(savedStateHandle, testRoute,
                  disposable)

              public override fun onCleared(): Unit {
                disposable.clear()
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
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class,
            )
            public interface NavEntryTestFlowScopeComponent {
              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance testRoute: TestRoute,
                  @BindsInstance coroutineScope: CoroutineScope,
                ): NavEntryTestFlowScopeComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun navEntryTestFlowScopeComponentFactory(): Factory
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowScopeViewModel(
              parentComponent: NavEntryTestFlowScopeComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              private val scope: CoroutineScope = MainScope()

              public val component: NavEntryTestFlowScopeComponent =
                  parentComponent.navEntryTestFlowScopeComponentFactory().create(savedStateHandle, testRoute,
                  scope)

              public override fun onCleared(): Unit {
                scope.cancel()
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
