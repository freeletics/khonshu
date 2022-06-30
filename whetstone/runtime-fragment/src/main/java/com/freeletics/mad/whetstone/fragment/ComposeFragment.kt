package com.freeletics.mad.whetstone.fragment

import androidx.fragment.app.Fragment
import com.freeletics.mad.statemachine.StateMachine
import kotlin.reflect.KClass

/**
 * By adding this annotation to a [androidx.compose.runtime.Composable] function, a `Fragment` is
 * generated that will have the same name with `Fragment` as suffix. The Fragment will create a
 * `ComposeView` that will call the annotated composable. It is required that the annotated function
 * has `State` as first parameter and `(Action) -> Unit` as second parameter, where `State` and
 * `Action` match the given `StateMachine`.
 *
 * The generated `Fragment` will use the given [fragmentBaseClass] as it's super class, with
 * [androidx.fragment.app.Fragment] class used as default if nothing is specified.
 *
 * **StateMachine**
 *
 * The [stateMachine] will be collected while the composition is active and the emitted state will
 * be passed to the annotated [androidx.compose.runtime.Composable] function. Any invocation of the
 * `(Action) -> Unit` parameter will be dispatched to the `StateMachine`
 *
 * It is recommended to annotate the given `StateMachine` class with a Dagger scope like
 * explained below.
 *
 * **Scopes, Dagger and Anvil**
 *
 * There will also be a generated Dagger subcomponent that is tied to the Fragment but
 * survives configuration changes and stays alive while the Fragment destination is on the
 * backstack.
 *
 * The generated component uses [com.freeletics.mad.whetstone.ScopeTo] as its scope where the
 * [com.freeletics.mad.whetstone.ScopeTo.marker] parameter is the specified [scope] class. This
 * scope can be used to scope classes in the component and tie them to component's life time,
 * effectively making them survive configuration changes. The annotated class is also used as
 * [com.squareup.anvil.annotations.ContributesSubcomponent.scope], so it can be used to contribute
 * modules and bindings to the generated component.
 *
 * E.g. for `@ComposeFragment(scope = CoachScreen::class, ...)` the scope of the generated
 * component will be `@ScopeTo(CoachScreen::class)`, modules can be contributed with
 * `@ContributesTo(CoachScreen::class)` and bindings with
 * `@ContributesBinding(CoachScreen::class, ...)`.
 *
 * By default the following classes are available for injection in the component:
 * - a [androidx.lifecycle.SavedStateHandle]
 * - a [android.os.Bundle] with arguments passed to the screen
 *
 * A factory for the generated subcomponent is automatically generated and contributed to
 * the component that uses [parentScope] as its own scope. This component will be looked up internally
 * with `Context.getSystemService(name)` using the fully qualified name of the given [parentScope] as
 * key for the lookup. It is expected that the app will provide it through its `Application` class or an
 * `Activity`.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class ComposeFragment(
    val scope: KClass<*>,
    val parentScope: KClass<*>,
    val stateMachine: KClass<out StateMachine<*, *>>,
    val fragmentBaseClass: KClass<out Fragment> = Fragment::class,
)
