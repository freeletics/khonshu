package com.freeletics.mad.whetstone

import android.content.Context
import androidx.navigation.NavBackStackEntry
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * Annotate a function in a Dagger [dagger.Module] to enable generating a Dagger component that
 * survives configuration changes and is kept as long as a specific `NavBackStackEntry`
 * is on the back stack.
 *
 * The generated component uses [ScopeTo] as its scope where the [ScopeTo.marker]
 * parameter is the specified [scope] class. This scope can be used to scope classes
 * in the component and tie them to to component's life time. The annotated class is also used as
 * [com.squareup.anvil.annotations.MergeComponent.scope], so it can be used to contribute modules
 * and bindings to the generated component.
 *
 * E.g. for `@NavEntryComponent(scope = CoachFlow::class, ...)` the scope of the generated
 * component will be `ScopeTo(CoachFlow::class)`, modules can be contributed with
 * `@ContributesTo(CoachFlow::class)` and bindings with
 * `@ContributesBinding(CoachFlow::class, ...)`.
 *
 * By default the following classes are available for injection in the component:
 * - a [androidx.lifecycle.SavedStateHandle]
 * - a [android.os.Bundle] obtained from [androidx.navigation.NavController.currentBackStackEntry]
 * - if [coroutinesEnabled] is `true`, a [kotlinx.coroutines.CoroutineScope] that will be cancelled automatically
 * - if [rxJavaEnabled] is `true`, a [io.reactivex.disposables.CompositeDisposable] that will be cleared automatically
 *
 * The component that uses [parentScope] as its scope will be used as parent component. That
 * component will be looked up by calling `getSystemService` on the current `Activity` and
 * the `Application`. The parameter passed to `getSystemService` is the fully qualified name of
 * the [parentScope] class. It is recommended to use the same marker class that is used as Anvil
 * scope for the parent component.
 *
 * The generated component can be accessed through a `Map<Class<*>, NavEntryComponentGetter` that
 * will automatically be available in the parent component. See [NavEntryComponentGetter].
 *
 * It's recommended to put this annotation on the method that provides the matching [NavEntryId]
 * value.
 */
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class NavEntryComponent(
    val scope: KClass<*>,
    val parentScope: KClass<*>,

    val coroutinesEnabled: Boolean = false,
    val rxJavaEnabled: Boolean = false,
)

/**
 * A qualifier that should be used to provide a resource id that represents an androidx.navigation
 * destination. This navigation id will be the one that a [NavEntryComponent], that uses the same
 * scope as the given [value], is tied to.
 *
 * It is recommended to put [NavEntryComponent] onto the same method that uses this qualifier
 * annotation.
 */
@Qualifier
annotation class NavEntryId(val value: KClass<*>)

/**
 * A generated implementation of this can be used to retrieve a generated [NavEntryComponent].
 *
 * The implementation will be bound into a `Map<String, NavEntryComponentGetter` were the key
 * is the same scope that is used in [NavEntryId] and [NavEntryComponent].
 */
interface NavEntryComponentGetter {
    /**
     * The id that is passed as parameter to [findEntry] is the id that was provided with
     * [NavEntryId]. The given [findEntry] should look up a back strack entry for that id
     * in the current `NavController`.
     */
    fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any
}
