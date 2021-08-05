package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.internal.EmptyNavigationHandler
import com.freeletics.mad.whetstone.internal.EmptyNavigator
import com.freeletics.mad.navigator.NavigationHandler
import com.freeletics.mad.navigator.Navigator
import com.gabrielittner.renderer.ViewRenderer
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * Annotate a `@Compososable` function or `Renderer` class that represents a screen to enable
 * generating a Dagger component that survives configuration changes.
 *
 * The generated component uses [ScopeTo] as it's scope where the [ScopeTo.marker]
 * parameter is the specified [scope] class. This scope can be used to scope classes
 * in the component an tie them to to component's life time. The annotated class is also used as
 * [com.squareup.anvil.annotations.MergeComponent.scope], so it can be used to contribute modules
 * and bindings to the generated component.
 *
 * E.g. for `@RetainedComponent(scope = CoachScreen::class, ...)` the scope of the generated
 * component will be `ScopeTo(CoachScreen::class)`, modules can be contributed with
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
 *
 * [stateMachine] will be exposed as a property from the generated component so that an instance can
 * be retrieved from it. If [navigator] and [navigationHandler] are set to something other than the
 * default values then properties for those 2 are also generated. It is required to set both
 * navigation parameters at the same time. See the documentation of [Navigator] and
 * [NavigationHandler] for information how these classes should be implemented and how they will be
 * used.
 *
 * The generated component is not meant to be used directly, instead it is used through other
 * generated classes. An instance of it is held by an also generated [androidx.lifecycle.ViewModel].
 * The component will be held until the `ViewModel` is cleared. This means that it and everything
 * else using it's  scope will survive configuration changes and will stay active while the screen
 * is on the backstack. The generated `ViewModel` will be used by one of the classes generated
 * through the [ComposeScreen], [ComposeFragment] and [RendererFragment] annotations.
 */
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
annotation class RetainedComponent(
    val scope: KClass<*>,
    val parentScope: KClass<*>,
    val dependencies: KClass<*>,

    //TODO should be KClass<out StateMachine<*, *>>
    // leaving out the constraint for now to be compatible with Renderer's LiveDataStateMachine
    val stateMachine: KClass<*>,
    val navigator: KClass<out Navigator> = EmptyNavigator::class,
    val navigationHandler: KClass<out NavigationHandler<*>> = EmptyNavigationHandler::class,

    val coroutinesEnabled: Boolean = false,
    val rxJavaEnabled: Boolean = false,
)

/**
 * This is an add-on annotation for the [RetainedComponent] annotation. Use this on the same
 * `@Composable` function hat [RetainedComponent] is used on.
 *
 * By adding this annotation a [androidx.compose.runtime.Composable] function that uses tbe same
 * name as the annotated class is generated. It expects an [androidx.navigation.NavController] as
 * it's sole parameter.
 *
 * **StateMachine**
 *
 * The `StateMachine` will be collected while the composition is active and the emitted state will
 * be passed to an expected [androidx.compose.runtime.Composable] function that has the same name as
 * the annotated marker class with `Ui` as suffix. The function gets the `state` as first parameter
 * and a `(Action) -> Unit` function as second parameter. Actions that are passed to the function
 * will be dispatched to the `StateMachine.
 *
 * **Navigation**
 *
 * If the 2 optional `navigator` and `navigationHandler` were set on [RetainedComponent] the
 * generated function will use these together with the passed in `NavController` to set up
 * navigation while the composition is active.
 *
 * **Example**
 *
 * This example code:
 * ```
 * @RetainedComponent(...)
 * @Compose
 * class CoachScreen
 * ```
 *
 * Generates the following composable:
 * ```
 * @Composable
 * fun CoachScreen(navController: NavController) {
 *   // setup code omitted
 *   val state = stateMachine.state.collectAsState()
 *   val state = stateMachine.state.collectAsState()
 *   CalendarUi(state.value) { action ->
 *     scope.launch { stateMachine.dispatch(action) }
 *   }
 * }
 * ```
 *
 * The generated composable function expects a composable like this to exist:
 * ```
 * @Composable
 * fun CoachScreenUi(state: CoachState, actions: (CoachAction) -> Unit) {
 *   // ui code
 * }
 * ```
 */
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
annotation class ComposeScreen

/**
 * This is an add-on annotation for the [RetainedComponent] annotation. Use this on the same
 * `@Composable` function hat [RetainedComponent] is used on.
 *
 * By adding this annotation the same [androidx.compose.runtime.Composable] function that
 * [ComposeScreen] generates is generated. See the documention of [ComposeScreen] for more
 * information about that.
 *
 * In addition a [androidx.fragment.app.Fragment] is generated. This Fragment will use
 * [androidx.compose.ui.platform.ComposeView] as it's view and will call the generated composable
 * from it's `setContent` method.
 */
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
annotation class ComposeFragment

/**
 * This is an add-on annotation for the [RetainedComponent] annotation. Use this on the same
 * `Renderer` class hat [RetainedComponent] is used on.
 *
 * By adding this annotation a [androidx.fragment.app.Fragment] is generated. This
 *
 * The given [rendererFactory] will be used to create the fragment's `View`. While the `Fragment`
 * is at least [androidx.lifecycle.Lifecycle.State.STARTED] the `StateMachine` will be collected and
 * the emitted state will be passed to the [com.gabrielittner.renderer.ViewRenderer] created from
 * the factory. In the same way actions emitted from the `ViewRenderer` will be collected and
 * dispatched to the `StateMachine`.
 *
 * If the 2 optional `navigator` and `navigationHandler` were set on [RetainedComponent] the
 * generated `Fragment` will use these to set up navigation after the injection.
 */
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
annotation class RendererFragment(
    val rendererFactory: KClass<out ViewRenderer.Factory<*, *>>,
)
