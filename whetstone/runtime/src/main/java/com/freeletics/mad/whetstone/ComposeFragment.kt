package com.freeletics.mad.whetstone

import com.freeletics.mad.navigator.NavigationHandler
import com.freeletics.mad.navigator.Navigator
import com.freeletics.mad.statemachine.StateMachine
import com.freeletics.mad.whetstone.internal.EmptyNavigationHandler
import com.freeletics.mad.whetstone.internal.EmptyNavigator
import kotlin.reflect.KClass

/**
 * By adding this annotation to a [androidx.compose.runtime.Composable] function, a Fragment is
 * generated that will have the same name with `Fragment` as suffix. The Fragment will create a
 * `ComposeView` that will call the annotated composable. It is required that the annotated function
 * has `State` as first parameter and `(Action) -> Unit` as second parameter, where `State` and
 * `Action` match the given `StateMachine`.
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
 * **Navigation**
 *
 * If the 2 optional `navigator` and `navigationHandler` are set the generated `Fragment` will use
 * these together with it's `NavController` to set up navigation after it was created.
 *
 * **Scopes, Dagger and Anvil**
 *
 * There will also be a generated Dagger component that is tied to the composable function but
 * survives configuration changes and stays alive while the composable destination is on the
 * backstack.
 *
 * The generated component uses [ScopeTo] as it's scope where the [ScopeTo.marker]
 * parameter is the specified [scope] class. This scope can be used to scope classes
 * in the component an tie them to to component's life time. The annotated class is also used as
 * [com.squareup.anvil.annotations.MergeComponent.scope], so it can be used to contribute modules
 * and bindings to the generated component.
 *
 * E.g. for `@RetainedComponent(scope = CoachScreen::class, ...)` the scope of the generated
 * component will be `@ScopeTo(CoachScreen::class)`, modules can be contributed with
 * `@ContributesTo(CoachScreen::class)` and bindings with
 * `@ContributesBinding(CoachScreen::class, ...)`.
 *
 * By default the following classes are available for injection in the component:
 * - everything accessible through the provided [dependencies] interface
 * - a [androidx.lifecycle.SavedStateHandle]
 * - a [android.os.Bundle] obtained from [androidx.navigation.NavController.currentBackStackEntry]
 * - if [coroutinesEnabled] is `true`, a [io.reactivex.disposables.CompositeDisposable] that will be cleared automatically
 * - if [rxJavaEnabled] is `true`, a [kotlinx.coroutines.CoroutineScope] that will be cancelled automatically
 *
 * The mentioned [dependencies] interface will be looked up by calling
 * [android.content.Context.getSystemService] on either the [android.app.Activity] context or the
 * [android.app.Application] context. The parameter passed to `getSystemService` is the fully
 * qualified name of [parentScope]. It is recommended to use the same marker class that is used as
 * Anvil scope for the [parentScope].
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComposeFragment(
    val scope: KClass<*>,
    val parentScope: KClass<*>,
    val dependencies: KClass<*>,

    val stateMachine: KClass<out StateMachine<*, *>>,
    val enableInsetHandling: Boolean = false,

    val navigator: KClass<out Navigator> = EmptyNavigator::class,
    val navigationHandler: KClass<out NavigationHandler<*>> = EmptyNavigationHandler::class,

    val coroutinesEnabled: Boolean = false,
    val rxJavaEnabled: Boolean = false,
)
