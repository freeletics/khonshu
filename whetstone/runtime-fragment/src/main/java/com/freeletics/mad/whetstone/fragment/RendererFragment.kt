package com.freeletics.mad.whetstone.fragment

import androidx.fragment.app.Fragment
import com.freeletics.mad.statemachine.StateMachine
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

/**
 * By adding this annotation to a [com.gabrielittner.renderer.ViewRenderer] class, a `Fragment` is
 * generated that will have the same name with `Fragment` as suffix. The Fragment will use the
 * given [rendererFactory] as it's view.
 *
 * The generated `Fragment` will use the given [fragmentBaseClass] as it's super class, with
 * [androidx.fragment.app.Fragment] class used as default if nothing is specified.
 *
 * **StateMachine**
 *
 * The `StateMachine` will be collected while the `Fragment` is at least in a
 * [androidx.lifecycle.Lifecycle.State.STARTED] state and the emitted state will
 * be passed to the `render` function of the `Renderer`. The `actions` of the `Renderer will also
 * be collected in the mean time and dispatched to the `StateMachine`.
 *
 * **Scopes, Dagger and Anvil**
 *
 * There will also be a generated Dagger subcomponent that is tied to the Fragment but
 * survives configuration changes and stays alive while the Fragment destination is on the
 * backstack.
 *
 * The generated component uses [com.freeletics.mad.whetstone.ScopeTo] as it's scope where the
 * [com.freeletics.mad.whetstone.ScopeTo.marker] parameter is the specified [scope] class. This
 * scope can be used to scope classes in the component an tie them to to component's life time,
 * effectively making them survive configuration changes. The  annotated class is also used as
 * [com.squareup.anvil.annotations.ContributesSubcomponent.scope], so it can be used to contribute
 * modulesc and bindings to the generated component.
 *
 * E.g. for `@RendererFragment(scope = CoachScreen::class, ...)` the scope of the generated
 * component will be `@ScopeTo(CoachScreen::class)`, modules can be contributed with
 * `@ContributesTo(CoachScreen::class)` and bindings with
 * `@ContributesBinding(CoachScreen::class, ...)`.
 *
 * By default the following classes are available for injection in the component:
 * - a [androidx.lifecycle.SavedStateHandle]
 * - a [android.os.Bundle] with arguments passed to the screen
 *
 * A factory for the generated subcomponent is automatically generated and contributed to
 * the component that uses [parentScope` as its own scope. This component will be looked up internally
 * with `Context.getSystemService(name)` using the fully qualified name of the given [parentScope] as
 * key for the lookup. It is expected that the app will provide it through its `Application` class or an
 * `Activity`.
 */
@Target(CLASS)
@Retention(RUNTIME)
public annotation class RendererFragment(
    val scope: KClass<*>,
    val parentScope: KClass<*>,
    val stateMachine: KClass<out StateMachine<*, *>>,
    //TODO should be KClass<out ViewRenderer.Factory<*, *>>
    // leaving out the constraint for now to be compatible with some custom factories using the same signature
    val rendererFactory: KClass<*>,
    val fragmentBaseClass: KClass<out Fragment> = Fragment::class,
)
