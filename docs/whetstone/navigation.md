# Navigator integration

Whetstone has a special integration with the [MAD Navigator library](../navigator/get-started.md).

## Dependency

Instead of or additionally to depending on `com.freeletics.mad:whetstone-runtime-...` a dependency
on the appropriate `whetstone-navigation` artifact needs to be added:

=== "Compose"

    ```groovy
    implementation("com.freeletics.mad:whetstone-navigation-compose:<latest-version>")
    anvil("com.freeletics.mad:whetstone-compiler:<latest-version>")
    ```

=== "Compose with Fragments"

    ```groovy
    implementation("com.freeletics.mad:whetstone-navigation-fragment:<latest-version>")
    anvil("com.freeletics.mad:whetstone-compiler:<latest-version>")
    ```

=== "Views with Fragments"

    ```kotlin
    implementation("com.freeletics.mad:whetstone-navigation-fragment:<latest-version>")
    anvil("com.freeletics.mad:whetstone-compiler:<latest-version>")
    ```

## Basic usage

Add the additional `@NavDestination` annotation next to the general Whetstone annotation
to activate the navigator integration.

=== "Compose"

    ```kotlin
    @ComposeScreen(...) // same as in general guide
    @NavDestination(
        route = ExampleRoute::class,
        type = DestinationType.SCREEN,
        destinationScope = AppScope::class,
    )
    @Composable
    fun ExampleUi(...) {...} // same as in general guide
    ```

=== "Compose with Fragments"

    ```kotlin
    @ComposeFragment(...) // same as in general guide
    @NavDestination(
        route = ExampleRoute::class,
        type = DestinationType.SCREEN,
        destinationScope = AppScope::class,
    )
    @Composable
    fun ExampleUi(...) {...} // same as in general guide
    ```
=== "Views with Fragments"

    ```kotlin
    @RendererFragment(...)  // same as in general guide
    @NavDestination(
        route = ExampleRoute::class,
        type = DestinationType.SCREEN,
        destinationScope = AppScope::class,
    )
    internal class ExampleRenderer ... // same as in general guide
    ```

The `route` parameter needs to reference the `NavRoute` class that is used
to navigate to the screen with the annotated UI. The instance of `NavRoute`
that was passed to the screen when navigating to it will be automatically
available in the generated component, so it can be injected into the state
machine or other classes to read given parameters.

When the `NavDestination` is added Whetstone also expects a `NavEventNavigator`
to be injectable. This can be easily achieved by adding
`@ScopeTo(ExampleScope::class) @ContributesBinding(ExampleScope::class, NavEventNavigator::class)`
to a subclass of it. Whetstone will automatically take care of setting up
the navigator by calling `NavigationSetup` for compose and `handleNavigation`
for Fragments inside the generated code.

The last part of the integration is that Whetstone will automatically generate
a `NavDestination` for the screen by using the `route` and `type`. This
generated destination is automatically provided into a `Set` in the component
that uses `destinationScope` as its scope (usually an app wide or Activity
level scope). With that it's not necessary to manually create a `Set`
of all destinations anymore. It can simply be injected.


## Example

This is a minimal example of how using Whetstone for a screen with the Navigator integration 
would look like.



This is a minimal example of how using Whetstone for a screen would look like.

```kotlin
// marker class for the scope
sealed interface ExampleScope

// state machine survives orientation changes
@ScopeTo(ExampleScope::class)
internal class ExampleStateMachine @Inject constructor(
    val route: ExampleRoute, // inject the navigator route that was used to get to this screen
    val repository: ExampleRepository, // a repository that pas provided somewhere in the app
) : StateMachine<ExampleState, ExampleAction> { 
    // ... 
}

// scope the navigator so that everything interacts with the same instance
@ScopeTo(ExampleScope::class)
// make ExampleNavigator available as NavEventNavigator so that the generated code can automatically
// set up the navigation handling
@ContributesBinding(ExampleScope::class, NavEventNavigator::class)
class ExampleNavigator @Inject constructor() : NavEventNavigator() {
    // ... 
}
```

=== "Compose"

    ```kotlin
    @ComposeScreen(
        scope = ExampleScope::class, // uses our marker class
        parentScope = AppScope::class, // the scope of the app level component
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
    )
    @NavDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleUi
        type = DestinationType.SCREEN, // whether it's a screen, dialog or bottom sheet
        destinationScope = AppScope::class, // contribute the generated destination to AppScope
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
    @ComposeFragment(
        scope = ExampleScope::class, // uses our marker class
        parentScope = AppScope::class, // the scope of the app level component
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
    )
    @NavDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleUi
        type = DestinationType.SCREEN, // whether it's a screen, dialog or bottom sheet
        destinationScope = AppScope::class, // contribute the generated destination to AppScope
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
    @RendererFragment(
        scope = ExampleScope::class, // uses our marker class
        parentScope = AppScope::class, // the scope of the app level component
        rendererFactory = ExampleRenderer.Factory::class, // references the factory below
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
    )
    @NavDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleUi
        type = DestinationType.SCREEN, // whether it's a screen, dialog or bottom sheet
        destinationScope = AppScope::class, // contribute the generated destination to AppScope
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
