package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.NavEntryData
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
import org.junit.Test

internal class NavEntryFileGeneratorTest {

    private val full = NavEntryData(
        baseName = "TestFlow",
        packageName = "com.test",
        scope = ClassName("com.test", "TestFlowScope"),
        parentScope = ClassName("com.test.parent", "TestParentScope"),
        coroutinesEnabled = true,
        rxJavaEnabled = true,
    )

    @Test
    fun `generates code for full NavEntryData`() {
        FileGenerator().generate(full).toString() shouldBe """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.whetstone.NavEntryId
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
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

            @InternalWhetstoneApi
            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class
            )
            public interface NavEntryTestFlowComponent {
              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance compositeDisposable: CompositeDisposable,
                  @BindsInstance coroutineScope: CoroutineScope
                ): NavEntryTestFlowComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun factory(): Factory
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowViewModel(
              factory: NavEntryTestFlowComponent.Factory,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              private val scope: CoroutineScope = MainScope()

              public val component: NavEntryTestFlowComponent = factory.create(savedStateHandle, arguments,
                  disposable, scope)

              public override fun onCleared(): Unit {
                disposable.clear()
                scope.cancel()
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestFlowScope::class)
            @ContributesMultibinding(
              TestParentScope::class,
              NavEntryComponentGetter::class
            )
            public class TestFlowComponentGetter @Inject constructor(
              @NavEntryId(TestFlowScope::class)
              private val id: Int
            ) : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(id)
                val viewModelProvider = viewModelProvider<NavEntryTestFlowComponent.ParentComponent>(entry,
                    context, TestParentScope::class) { parentComponent, handle -> 
                  val arguments = entry.arguments ?: Bundle.EMPTY
                  TestFlowViewModel(parentComponent.factory, handle, arguments)
                }
                val viewModel = viewModelProvider[TestFlowViewModel::class.java]
                return viewModel.component
              }
            }

        """.trimIndent()
    }

    @Test
    fun `generates code for NavEntryData without coroutines`() {
        val withoutCoroutines = full.copy(coroutinesEnabled = false)

        FileGenerator().generate(withoutCoroutines).toString() shouldBe """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.whetstone.NavEntryId
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
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

            @InternalWhetstoneApi
            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class
            )
            public interface NavEntryTestFlowComponent {
              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance compositeDisposable: CompositeDisposable
                ): NavEntryTestFlowComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun factory(): Factory
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowViewModel(
              factory: NavEntryTestFlowComponent.Factory,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              public val component: NavEntryTestFlowComponent = factory.create(savedStateHandle, arguments,
                  disposable)

              public override fun onCleared(): Unit {
                disposable.clear()
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestFlowScope::class)
            @ContributesMultibinding(
              TestParentScope::class,
              NavEntryComponentGetter::class
            )
            public class TestFlowComponentGetter @Inject constructor(
              @NavEntryId(TestFlowScope::class)
              private val id: Int
            ) : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(id)
                val viewModelProvider = viewModelProvider<NavEntryTestFlowComponent.ParentComponent>(entry,
                    context, TestParentScope::class) { parentComponent, handle -> 
                  val arguments = entry.arguments ?: Bundle.EMPTY
                  TestFlowViewModel(parentComponent.factory, handle, arguments)
                }
                val viewModel = viewModelProvider[TestFlowViewModel::class.java]
                return viewModel.component
              }
            }

        """.trimIndent()
    }

    @Test
    fun `generates code for NavEntryData without rxjava`() {
        val withoutRxJava = full.copy(rxJavaEnabled = false)

        FileGenerator().generate(withoutRxJava).toString() shouldBe """
            package com.test

            import android.content.Context
            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.whetstone.NavEntryId
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
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

            @InternalWhetstoneApi
            @ScopeTo(TestFlowScope::class)
            @ContributesSubcomponent(
              scope = TestFlowScope::class,
              parentScope = TestParentScope::class
            )
            public interface NavEntryTestFlowComponent {
              @ContributesSubcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance coroutineScope: CoroutineScope
                ): NavEntryTestFlowComponent
              }

              @ContributesTo(TestParentScope::class)
              public interface ParentComponent {
                public fun factory(): Factory
              }
            }

            @InternalWhetstoneApi
            internal class TestFlowViewModel(
              factory: NavEntryTestFlowComponent.Factory,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle
            ) : ViewModel() {
              private val scope: CoroutineScope = MainScope()

              public val component: NavEntryTestFlowComponent = factory.create(savedStateHandle, arguments,
                  scope)

              public override fun onCleared(): Unit {
                scope.cancel()
              }
            }

            @OptIn(InternalWhetstoneApi::class)
            @NavEntryComponentGetterKey(TestFlowScope::class)
            @ContributesMultibinding(
              TestParentScope::class,
              NavEntryComponentGetter::class
            )
            public class TestFlowComponentGetter @Inject constructor(
              @NavEntryId(TestFlowScope::class)
              private val id: Int
            ) : NavEntryComponentGetter {
              @OptIn(InternalWhetstoneApi::class)
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(id)
                val viewModelProvider = viewModelProvider<NavEntryTestFlowComponent.ParentComponent>(entry,
                    context, TestParentScope::class) { parentComponent, handle -> 
                  val arguments = entry.arguments ?: Bundle.EMPTY
                  TestFlowViewModel(parentComponent.factory, handle, arguments)
                }
                val viewModel = viewModelProvider[TestFlowViewModel::class.java]
                return viewModel.component
              }
            }

        """.trimIndent()
    }
}
