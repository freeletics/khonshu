package com.freeletics.mad.codegen

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * Add this annotation in addition to the `@NavDestination` annotation to a screen to enable
 * generating a Dagger component that survives configuration changes and is kept as long as a
 * specific `NavBackStackEntry` is on the back stack. This allows to create an extra scope that is
 * shared between multiple screens of a flow.
 *
 * To use the nav entry component as a parent the relevant screens should use the [scope] of this
 * annotation as their parent scope.
 *
 * **Scopes, Dagger and Anvil**
 *
 * The generated component uses [com.freeletics.mad.codegen.ScopeTo] as its scope where the
 * [com.freeletics.mad.codegen.ScopeTo.marker] parameter is the specified [scope] class. This
 * scope can be used to scope classes in the component and tie them to component's life time,
 * effectively making them survive configuration changes. The annotated class is also used as
 * [com.squareup.anvil.annotations.ContributesSubcomponent.scope], so it can be used to contribute
 * modules and bindings to the generated component.
 *
 * E.g. for `@NavEntryComponent(scope = CoachScreen::class, ...)` the scope of the generated
 * component will be `@ScopeTo(CoachScreen::class)`, modules can be contributed with
 * `@ContributesTo(CoachScreen::class)` and bindings with
 * `@ContributesBinding(CoachScreen::class, ...)`.
 *
 * By default the following classes are available for injection in the component:
 * - a [androidx.lifecycle.SavedStateHandle]
 * - a [android.os.Bundle] with arguments passed to the screen
 *
 * These automatically available classes use [NavEntry] as a qualifier where the [scope] is the
 * parameter. So for the [androidx.lifecycle.SavedStateHandle] to be injected
 * `@NavEntry(CoachScreen::class) val savedStateHandle: SavedStateHandle` needs to be used.
 *
 * A factory for the generated subcomponent is automatically generated and contributed to
 * the component that uses [parentScope] as its own scope. This component will be looked up internally
 * with `Context.getSystemService(name)` using the fully qualified name of the given [parentScope] as
 * key for the lookup. It is expected that the app will provide it through its `Application` class or an
 * `Activity`.
 */
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
public annotation class NavEntryComponent(
    val scope: KClass<*>,
    val parentScope: KClass<*> = AppScope::class,
)
