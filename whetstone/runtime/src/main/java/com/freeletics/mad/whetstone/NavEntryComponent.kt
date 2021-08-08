package com.freeletics.mad.whetstone

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * Annotate a function in a Dagger [dagger.Module] to enable generating a Dagger component that
 * survives configuration changes and is kept as long as a specific `NavBackStackEntry`
 * is on the back stack.
 *
 * The generated component uses [ScopeTo] as it's scope where the [ScopeTo.marker]
 * parameter is the specified [scope] class. This scope can be used to scope classes
 * in the component an tie them to to component's life time. The annotated class is also used as
 * [com.squareup.anvil.annotations.MergeComponent.scope], so it can be used to contribute modules
 * and bindings to the generated component.
 *
 * E.g. for `@RetainedComponent(scope = CoachFlow::class, ...)` the scope of the generated
 * component will be `ScopeTo(CoachFlow::class)`, modules can be contributed with
 * `@ContributesTo(CoachFlow::class)` and bindings with
 * `@ContributesBinding(CoachFlow::class, ...)`.
 *
 * By default the following classes are available for injection in the component:
 * - a [androidx.lifecycle.SavedStateHandle]
 * - a [android.os.Bundle] obtained from [androidx.navigation.NavController.currentBackStackEntry]
 * - if [coroutinesEnabled] is `true`, a [io.reactivex.disposables.CompositeDisposable] that will be cleared automatically
 * - if [rxJavaEnabled] is `true`, a [kotlinx.coroutines.CoroutineScope] that will be cancelled automatically
 *
 * The component that uses [parentScope] as it's scope will be used as parent component. That
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
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
annotation class NavEntryComponent(
    val scope: KClass<*>,
    val parentScope: KClass<*>,

    val coroutinesEnabled: Boolean = false,
    val rxJavaEnabled: Boolean = false,
)
