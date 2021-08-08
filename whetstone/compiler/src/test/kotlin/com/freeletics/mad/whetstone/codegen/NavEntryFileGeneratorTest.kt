package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.naventry.NavEntryFileGenerator
import com.squareup.kotlinpoet.ClassName
import io.kotest.matchers.shouldBe
import org.junit.Test

class NavEntryFileGeneratorTest {

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
        val generator = NavEntryFileGenerator(full)

        generator.generate().toString() shouldBe """
            package com.test
            
            import android.content.Context
            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.whetstone.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.NavEntryId
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeSubcomponent
            import com.test.parent.TestParentScope
            import dagger.Binds
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Subcomponent
            import dagger.multibindings.IntoMap
            import io.reactivex.disposables.CompositeDisposable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @InternalWhetstoneApi
            @ScopeTo(TestFlowScope::class)
            @MergeSubcomponent(scope = TestFlowScope::class)
            public interface NavEntryTestFlowComponent {
              @Subcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance compositeDisposable: CompositeDisposable,
                  @BindsInstance coroutineScope: CoroutineScope
                ): NavEntryTestFlowComponent
              }
            }

            @ContributesTo(TestParentScope::class)
            public interface NavEntryTestFlowComponentFactoryProvider {
              @InternalWhetstoneApi
              public fun getNavEntryTestFlowComponent(): NavEntryTestFlowComponent.Factory
            }

            @InternalWhetstoneApi
            internal class TestFlowViewModel(
              factory: NavEntryTestFlowComponentFactoryProvider,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              private val scope: CoroutineScope = MainScope()

              public val component: NavEntryTestFlowComponent =
                  factory.getNavEntryTestFlowComponent().create(savedStateHandle, arguments, disposable, scope)

              public override fun onCleared(): Unit {
                disposable.clear()
                scope.cancel()
              }
            }

            public class TestFlowComponentGetter @Inject constructor(
              @NavEntryId(TestFlowScope::class)
              private val id: Int
            ) : NavEntryComponentGetter {
              @InternalWhetstoneApi
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(id)
                val viewModelProvider = viewModelProvider<NavEntryTestFlowComponentFactoryProvider>(entry,
                    context, TestParentScope::class) { component, handle -> 
                  val arguments = entry.arguments ?: Bundle.EMPTY
                  TestFlowViewModel(component, handle, arguments)
                }
                val viewModel = viewModelProvider[TestFlowViewModel::class.java]
                return viewModel.component
              }
            }
            
            @Module
            @ContributesTo(TestParentScope::class)
            public interface WhetstoneTestFlowModule {
              @Binds
              @IntoMap
              @NavEntryComponentGetterKey("com.test.TestFlowScope")
              public fun bindComponentGetter(getter: TestFlowComponentGetter): NavEntryComponentGetter
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for NavEntryData without coroutines`() {
        val withoutCoroutines = full.copy(coroutinesEnabled = false)
        val generator = NavEntryFileGenerator(withoutCoroutines)

        generator.generate().toString() shouldBe """
            package com.test
            
            import android.content.Context
            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.whetstone.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.NavEntryId
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeSubcomponent
            import com.test.parent.TestParentScope
            import dagger.Binds
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Subcomponent
            import dagger.multibindings.IntoMap
            import io.reactivex.disposables.CompositeDisposable
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.Unit

            @InternalWhetstoneApi
            @ScopeTo(TestFlowScope::class)
            @MergeSubcomponent(scope = TestFlowScope::class)
            public interface NavEntryTestFlowComponent {
              @Subcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance compositeDisposable: CompositeDisposable
                ): NavEntryTestFlowComponent
              }
            }
            
            @ContributesTo(TestParentScope::class)
            public interface NavEntryTestFlowComponentFactoryProvider {
              @InternalWhetstoneApi
              public fun getNavEntryTestFlowComponent(): NavEntryTestFlowComponent.Factory
            }

            @InternalWhetstoneApi
            internal class TestFlowViewModel(
              factory: NavEntryTestFlowComponentFactoryProvider,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle
            ) : ViewModel() {
              private val disposable: CompositeDisposable = CompositeDisposable()

              public val component: NavEntryTestFlowComponent =
                  factory.getNavEntryTestFlowComponent().create(savedStateHandle, arguments, disposable)

              public override fun onCleared(): Unit {
                disposable.clear()
              }
            }

            public class TestFlowComponentGetter @Inject constructor(
              @NavEntryId(TestFlowScope::class)
              private val id: Int
            ) : NavEntryComponentGetter {
              @InternalWhetstoneApi
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(id)
                val viewModelProvider = viewModelProvider<NavEntryTestFlowComponentFactoryProvider>(entry,
                    context, TestParentScope::class) { component, handle -> 
                  val arguments = entry.arguments ?: Bundle.EMPTY
                  TestFlowViewModel(component, handle, arguments)
                }
                val viewModel = viewModelProvider[TestFlowViewModel::class.java]
                return viewModel.component
              }
            }
            
            @Module
            @ContributesTo(TestParentScope::class)
            public interface WhetstoneTestFlowModule {
              @Binds
              @IntoMap
              @NavEntryComponentGetterKey("com.test.TestFlowScope")
              public fun bindComponentGetter(getter: TestFlowComponentGetter): NavEntryComponentGetter
            }
            
        """.trimIndent()
    }

    @Test
    fun `generates code for NavEntryData without rxjava`() {
        val withoutRxJava = full.copy(rxJavaEnabled = false)
        val generator = NavEntryFileGenerator(withoutRxJava)

        generator.generate().toString() shouldBe """
            package com.test
            
            import android.content.Context
            import android.os.Bundle
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.navigation.NavBackStackEntry
            import com.freeletics.mad.whetstone.NavEntryComponentGetter
            import com.freeletics.mad.whetstone.NavEntryId
            import com.freeletics.mad.whetstone.ScopeTo
            import com.freeletics.mad.whetstone.`internal`.InternalWhetstoneApi
            import com.freeletics.mad.whetstone.`internal`.NavEntryComponentGetterKey
            import com.freeletics.mad.whetstone.`internal`.viewModelProvider
            import com.squareup.anvil.annotations.ContributesTo
            import com.squareup.anvil.annotations.MergeSubcomponent
            import com.test.parent.TestParentScope
            import dagger.Binds
            import dagger.BindsInstance
            import dagger.Module
            import dagger.Subcomponent
            import dagger.multibindings.IntoMap
            import javax.inject.Inject
            import kotlin.Any
            import kotlin.Int
            import kotlin.Unit
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.MainScope
            import kotlinx.coroutines.cancel

            @InternalWhetstoneApi
            @ScopeTo(TestFlowScope::class)
            @MergeSubcomponent(scope = TestFlowScope::class)
            public interface NavEntryTestFlowComponent {
              @Subcomponent.Factory
              public interface Factory {
                public fun create(
                  @BindsInstance savedStateHandle: SavedStateHandle,
                  @BindsInstance arguments: Bundle,
                  @BindsInstance coroutineScope: CoroutineScope
                ): NavEntryTestFlowComponent
              }
            }
            
            @ContributesTo(TestParentScope::class)
            public interface NavEntryTestFlowComponentFactoryProvider {
              @InternalWhetstoneApi
              public fun getNavEntryTestFlowComponent(): NavEntryTestFlowComponent.Factory
            }

            @InternalWhetstoneApi
            internal class TestFlowViewModel(
              factory: NavEntryTestFlowComponentFactoryProvider,
              savedStateHandle: SavedStateHandle,
              arguments: Bundle
            ) : ViewModel() {
              private val scope: CoroutineScope = MainScope()

              public val component: NavEntryTestFlowComponent =
                  factory.getNavEntryTestFlowComponent().create(savedStateHandle, arguments, scope)

              public override fun onCleared(): Unit {
                scope.cancel()
              }
            }

            public class TestFlowComponentGetter @Inject constructor(
              @NavEntryId(TestFlowScope::class)
              private val id: Int
            ) : NavEntryComponentGetter {
              @InternalWhetstoneApi
              public override fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any {
                val entry = findEntry(id)
                val viewModelProvider = viewModelProvider<NavEntryTestFlowComponentFactoryProvider>(entry,
                    context, TestParentScope::class) { component, handle -> 
                  val arguments = entry.arguments ?: Bundle.EMPTY
                  TestFlowViewModel(component, handle, arguments)
                }
                val viewModel = viewModelProvider[TestFlowViewModel::class.java]
                return viewModel.component
              }
            }
            
            @Module
            @ContributesTo(TestParentScope::class)
            public interface WhetstoneTestFlowModule {
              @Binds
              @IntoMap
              @NavEntryComponentGetterKey("com.test.TestFlowScope")
              public fun bindComponentGetter(getter: TestFlowComponentGetter): NavEntryComponentGetter
            }
            
        """.trimIndent()
    }
}
