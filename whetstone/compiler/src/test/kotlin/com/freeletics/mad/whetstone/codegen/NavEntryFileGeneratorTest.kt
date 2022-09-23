package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.Navigation
import com.squareup.kotlinpoet.ClassName
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class NavEntryFileGeneratorTest {

    private val navigation = Navigation.Compose(
        route = ClassName("com.test", "TestRoute"),
        destinationType = "SCREEN",
        destinationScope = ClassName("com.test.destination", "TestDestinationScope"),
    )

    private val data = NavEntryData(
        packageName = "com.test",
        scope = ClassName("com.test", "TestFlowScope"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        navigation = navigation,
    )

    @Test
    fun `generates code for NavEntryData`() {
        val actual = FileGenerator().generate(data).toString()

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
            import com.test.destination.TestDestinationScope
            import com.test.parent.TestParentScope
            import dagger.BindsInstance
            import dagger.Module
            import dagger.multibindings.Multibinds
            import java.io.Closeable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.OptIn
            import kotlin.Unit
            import kotlin.collections.Set

            @OptIn(InternalWhetstoneApi::class)
            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class,
            )
            public interface WhetstoneTestFlowScopeNavEntryComponent {
              @get:NavEntry(TestFlowScope::class)
              public val closeables: Set<Closeable>

              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(@BindsInstance @NavEntry(TestFlowScope::class)
                    savedStateHandle: SavedStateHandle, @BindsInstance @NavEntry(TestFlowScope::class)
                    testRoute: TestRoute): WhetstoneTestFlowScopeNavEntryComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun whetstoneTestFlowScopeNavEntryComponentFactory(): Factory
              }
            }

            @Module
            @ContributesTo(TestFlowScope::class)
            public interface WhetstoneTestFlowScopeNavEntryModule {
              @Multibinds
              @NavEntry(TestFlowScope::class)
              public fun bindCloseables(): Set<Closeable>
            }

            @InternalWhetstoneApi
            internal class WhetstoneTestFlowScopeNavEntryViewModel(
              parentComponent: WhetstoneTestFlowScopeNavEntryComponent.ParentComponent,
              savedStateHandle: SavedStateHandle,
              testRoute: TestRoute,
            ) : ViewModel() {
              public val component: WhetstoneTestFlowScopeNavEntryComponent =
                  parentComponent.whetstoneTestFlowScopeNavEntryComponentFactory().create(savedStateHandle,
                  testRoute)

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
            public class TestFlowScopeNavEntryComponentGetter @Inject constructor() : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class, InternalNavigatorApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(TestRoute::class.destinationId())
                val route: TestRoute = entry.arguments!!.toRoute()
                val viewModel = viewModel(entry, context, TestParentScope::class, TestDestinationScope::class,
                    route, findEntry, ::WhetstoneTestFlowScopeNavEntryViewModel)
                return viewModel.component
              }
            }

        """.trimIndent()

        assertThat(actual).isEqualTo(expected)
    }
}
