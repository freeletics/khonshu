# Navigation integration

The codegen also simplifies the usage of the [Khonshu Navigation library](../navigation/get-started.md).

## Basic usage

=== "Compose"

    Instead of adding the `@ComposeScreen` annotation use `@ComposeDestination`. The `scope`
    parameter is replaced by a `route` parameter. The given `route` will be used both as a 
    scope marker and to generate a `NavDestination` for this screen.

    Additionally there is a `destinationType` parameter to determine whether the 
    annotated composable is a screen or the content of a dialog or bottom sheet and a 
    `destinationScope` parameter that determines to which component the `NavDestination` is 
    contributed to.

    ```kotlin
    @ComposeDestination(
        route = ExampleRoute::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
        destinationType = DestintionType.SCREEN,
        destinationScope = AppScope::class, // AppScope is the default value and can be omitted
    )
    @Composable
    fun ExampleUi(...) {...} // same as in general guide
    ```

=== "Compose with Fragments"

    Instead of adding the `@ComposeFragment` annotation use `@ComposeDestination`. The `scope`
    parameter is replaced by a `route` parameter. The given `route` will be used both as a 
    scope marker and to generate a `NavDestination` for this screen.

    Additionally there is a `destinationType` parameter to determine whether the 
    annotated composable is a screen or the content of a dialog or bottom sheet and a 
    `destinationScope` parameter that determines to which component the `NavDestination` is 
    contributed to.

    ```kotlin
    @ComposeDestination(
        route = ExampleRoute::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
        destinationType = DestintionType.SCREEN,
        destinationScope = AppScope::class, // AppScope is the default value and can be omitted
    )
    @Composable
    fun ExampleUi(...) {...} // same as in general guide
    ```
=== "Views with Fragments"

    Instead of adding the `@RendererFragment` annotation use `@RendererDestination`. The `scope`
    parameter is replaced by a `route` parameter. The given `route` will be used both as a 
    scope marker and to generate a `NavDestination` for this screen.

    Additionally there is a `destinationType` parameter to determine whether the 
    annotated composable is a screen or the content of a dialog or bottom sheet and a 
    `destinationScope` parameter that determines to which component the `NavDestination` is 
    contributed to.

    ```kotlin
    @RendererDestination(
        route = ExampleRoute::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
        destinationType = DestintionType.SCREEN,
        destinationScope = AppScope::class, // AppScope is the default value and can be omitted
    )
    internal class ExampleRenderer ... // same as in general guide
    ```

The instance of `NavRoute` that was passed to the screen when navigating to 
it will be automatically available in the generated component, so it can be 
injected into the state machine or other classes to read given parameters.

The generated `NavDestination` for the screen that uses `route` and `destinationType`
will be provided in to a `Set` in the component that uses `destinationScope` as its
scope (usually an app wide or an Activity level scope). With that it's not necessary
to manually create a `Set` of all destinations anymore. It can simply be injected.

The integration of Khonshu's Codegen and Navigation libraries also expects a `NavEventNavigator`
to be injectable. This can be easily achieved by adding
`@ScopeTo(ExampleScope::class) @ContributesBinding(ExampleScope::class, NavEventNavigator::class)`
to a subclass of it. The generated code will automatically take care of setting up
the navigator by calling `NavigationSetup` for compose and `handleNavigation`
for Fragments inside the generated code.


## Example

This is a minimal example of how using Codegen for a screen with the navigation integration 
would look like.

```kotlin
// state machine survives orientation changes
@ScopeTo(ExampleRoute::class)
internal class ExampleStateMachine @Inject constructor(
    val route: ExampleRoute, // inject the navigator route that was used to get to this screen
    val repository: ExampleRepository, // a repository that pas provided somewhere in the app
) : StateMachine<ExampleState, ExampleAction> { 
    // ... 
}

// scope the navigator so that everything interacts with the same instance
@ScopeTo(ExampleRoute::class)
// make ExampleNavigator available as NavEventNavigator so that the generated code can automatically
// set up the navigation handling
@ContributesBinding(ExampleRoute::class, NavEventNavigator::class)
class ExampleNavigator @Inject constructor() : NavEventNavigator() {
    // ... 
}
```

=== "Compose"

    ```kotlin
    @ComposeDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleUi
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
        destinationType = DestinationType.SCREEN, // whether it's a screen, dialog or bottom sheet
        destinationScope = AppScope::class, // contribute the generated destination to AppScope, AppScope is the default value and can be omitted
    )
    @Composable
    internal fun ExampleUi(
        state: ExampleState,
        sendAction: (ExampleAction) -> Unit,
    ) { 
        // render the ui for ExampleState
    }
    ```

=== "Compose with Fragments"

    ```kotlin
    @ComposeDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleUi
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
        destinationType = DestinationType.SCREEN, // whether it's a screen, dialog or bottom sheet
        destinationScope = AppScope::class, // contribute the generated destination to AppScope, AppScope is the default value and can be omitted
    )
    @Composable
    internal fun ExampleUi(
        state: ExampleState,
        sendAction: (ExampleAction) -> Unit,
    ) { 
        // render the ui for ExampleState
    }
    ```

=== "Views with Fragments"

    ```kotlin
    @RendererDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleRenderer
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used fo
        destinationType = DestinationType.SCREEN, // whether it's a screen, dialog or bottom sheet
        destinationScope = AppScope::class, // contribute the generated destination to AppScope, AppScope is the default value and can be omitted
    )
    internal class ExampleRenderer @AssistedInject constructor(
        @Assisted private val binding: ExampleViewBinding,
    ) : ViewRenderer<ExampleState, ExampleAction>(binding) {
    
        override fun renderToView(state: ExampleState) {
            // render the ui for ExampleState
        }
    
        @AssistedFactory
        abstract class Factory : ViewRenderer.Factory<ExampleViewBinding, ExampleRenderer>(ExampleViewBinding::inflate)
    }
    ```

The same `AppComponent` and `Application` class set up as in the general guide is needed as well.
