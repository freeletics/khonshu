# Get started

Khonshu's Codegen is a plugin for [Anvil](https://github.com/square/anvil) that helps with
generating dependency injection related code and common boilerplate for screens.

## Advantages

- eliminate boilerplate that usually needs to be repeated on each screen
- optional integration with Navigator to simplify its setup
- easily let objects survive configuration changes

One other general advantage is around Fragments. If an app uses them or has to use them for legacy
reasons, Codegen will mostly hide them from the developers because it generates the Fragment
for each screen it's used in. This way logic is kept out of them and the actual components like
UI and state machine (presenter/view model) can be tested more easily in isolation. It also makes
a migration away from Fragments easier since generated code can be easily replaced by other
generated code. In fact migrating an app where each screen uses Codegen from Fragments to
just using Compose is as easy as replacing a single annotation on each screen.


## Dependency

```groovy
implementation("com.freeletics.khonshu:codegen-runtime:<latest-version>")
// instead of `anvil` it's also possible to use `ksp`
anvil("com.freeletics.khonshu:codegen-compiler:<latest-version>")
```


## Basic usage

The library provides 2 different runtime implementations. One for `Fragment` based apps and one
for pure `Compose` apps. If an app uses Compose but the composables are hosted inside fragments
it falls into the `Fragment` category.

=== "Compose"

    The `@NavDestination` annotation is added to the top level composable of a screen. This function
    can have 2 parameters: the state that should be rendered and a lambda that allows
    the composable to send actions for user interactions. Adding the annotation will then generate
    another Composable function called `KhonshuExampleUi`, a Dagger component and a `NavDestination`
    that uses the given `route` and the generated composable.

    ```kotlin
    @NavDestination(
        route = ExampleRoute::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
        destinationScope = AppScope::class, // AppScope is the default value and can be omitted
    )
    @Composable
    internal fun ExampleUi(
      state: ExampleState,
      sendAction: (ExampleAction) -> Unit
    ) {
      // composable logic ...
    }
    ```
    *`scope`, `parentScope` and destionationScope` are described in the next sections*

    The generated `KhonshuExampleUi` function will use the generated component, the
    annotated composable as well as the `stateMachine` parameter from the
    annotation. It will automatically hook up the
    state machine with the composable so that the state from the state machine
    is passed to the composable and actions from the latter are sent back to the
    state machine. The generated composable will use the generated component
    to obtain the state machine.

=== "Compose with Fragments"

    The `@ComposeFragmentDestination` annotation is added to the top level composable of a screen. This function
    can have 2 parameters: the state that should be rendered and a lambda that allows
    the composable to send actions for user interactions. Adding the annotation will then generate
    a `Fragment` called `KhonshuExampleUiFragment`, a Dagger component and a `NavDestination`
    that uses the given `route` and the generated Fragment.

    ```kotlin
    @ComposeFragmentDestination(
        route = ExampleRoute::class,
        parentScope = AppScope::class, // AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class,
        destinationScope = AppScope::class, // AppScope is the default value and can be omitted
    )
    @Composable
    internal fun ExampleUi(
      state: ExampleState,
      sendAction: (ExampleAction) -> Unit
    ) {
      // composable logic ...
    }
    ```
    *`scope`, `parentScope` and destionationScope` are described in the next sections*

    The generated `KhonshuExampleUiFragment` will use the generated component, the
    annotated composable as well as the `stateMachine` parameter from the
    annotation. It will use the composable as its view and automatically hook up the
    state machine with the composable so that the state from the state machine
    is passed to the composable and actions from the latter are sent back to the
    state machine. The generated fragment will use the generated component
    to obtain the state machine.

    The annotation has an optional `fragmentBaseClass` parameter that allows to
    specify a class other than `Fragment` to be used as super class for the
    generated Fragment. This allows using `DialogFragment` or `BottomSheetDialogFragment`
    for example.


## Generated component

All annotations have a `scope` and a `parentScope` parameter. These will be used in Anvil's
`@ContributesSubcomponent` annotation on the generated subcomponent, i.e.
`@ContributesSubcomponent(route = ExampleRoute::class, parentScope = AppScope::class)`.

Since the generated subcomponent is using `@ContributesSubcomponent`, it is possible
to use `@ContributesTo`, `@ContributesBinding` and so on with that same scope
to contribute objects into it.

`scope` is also used for Dagger scopes. The generated component is annotated
with the `@SingleIn` annotation that ships with Anvil and uses
the `scope` value as a parameter. To scope a class just add
`@SingleIn(ExampleScope::class)` to it. Any object using this scope will automatically
survive configuration changes and will not be recreated together with the UI. In fact any
scoped object that is created in generated component will do so together with component itself.

A factory for the generated subcomponent is automatically generated and contributed to
the component that uses `parentScope` as its own scope. This component will be looked up internally
with `Context.getSystemService(name)` using the fully qualified name of the given `parentScope` as
key for the lookup. It is expected that the app will provide it through its `Application` class or an
`Activity`.

For convenience purposes the generated component will make the instance of `route` that was used to
navigate to the screen and a `SavedStateHandle` available which can be injected to classes like
the state machine to save state. When injecting the `SavedStateHandle` a `@ForScope(ExampleScope::class)`
qualifier needs to be added.


## NavDestination

A `NavDestination` is automatically generated for each annotated screen. The type of the generated
destination is based on the used `NavRoute`. To get an `OverlayDestination` (`DialogDestination` for
Fragments) the route class needs to implement the `Overlay` marker interface.

To avoid having to access any generated code the destination is directly contributed to
a `Set<NavDestination>` which can then be injected where the `NavHost` or `NavHostFragment` is
created. By default this set lives in the `AppScope` component, but this can be changed
by setting a different scope marker class to `destinationScope`. It's also possible to use
`destinationScope` to create distinct sets of destinations. For example if an app has a logged in
and a logged out component, their scopes could be used as `destinationScope` depending on whether
a screen is shown in the logged in or logged out state.


## Navigation set up

The integration of Khonshu's Codegen and Navigation libraries also expects a `NavEventNavigator`
to be injectable. This can be easily achieved by adding `@SingleIn(ExampleScope::class)
@ForScope(ExampleScope::class) @ContributesBinding(ExampleScope::class, NavEventNavigator::class)`
to a subclass of it. The generated code will automatically take care of setting up
the navigator by calling `NavigationSetup` for compose and `handleNavigation`
for Fragments inside the generated code.


## Sharing objects between screens

Sometimes it is needed to share an object between 2 or more screens, for example
in a flow of screens that are connected to each other and work on the same data.
This is possible by using the route of the first screen in the flow as `parentScope`
for the other scopes. Internally this will cause the generated component for the
first screen to become the parent for the components of the other screens.

This then allows injecting anything that is available in the scope of the first screen,
including the `SavedStateHandle` and route of the initial/parent screen.


## Example

This is a minimal example of how using Khonshu's Codegen for a screen would look like.

```kotlin
// marker class for the scope
sealed interface ExampleScope

// state machine survives orientation changes
@SingleIn(ExampleRoute::class)
internal class ExampleStateMachine @Inject constructor(
    val route: ExampleRoute, // inject the navigator route that was used to get to this screen
    @ForScope(ExampleRoute::class)
    val savedStateHandle: SavedStateHandle, // a saved state handle tied to this screen
    val repository: ExampleRepository, // a repository that pas provided somewhere in the app
) : StateMachine<ExampleState, ExampleAction> {
    // ...
}

// scope the navigator so that everything interacts with the same instance
@SingleIn(ExampleRoute::class)
@ForScope(ExampleRoute::class)
// make ExampleNavigator available as NavEventNavigator so that the generated code can automatically
// set up the navigation handling
@ContributesBinding(ExampleRoute::class, NavEventNavigator::class)
class ExampleNavigator @Inject constructor() : NavEventNavigator() {
    // ...
}
```

=== "Compose"

    ```kotlin
    @NavDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleUi
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
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
    @ComposeFragmentDestination(
        route = ExampleRoute::class, // the route used to navigate to ExampleUi
        parentScope = AppScope::class, // the scope of the app level component, AppScope is the default value and can be omitted
        stateMachine = ExampleStateMachine::class, // the state machine used for this ui
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


Using this would require a one time setup in the app so that the screens can look up the `AppScope`
component through `getSystemService` to retrieve the parent component:

```kotlin
@AppScope
@MergeComponent(scope = AppScope::class)
interface AppComponent {
    // allows an Activity to get all generated NavDestinations to set up the NavHost
    val destinations: Set<NavDestination>

    @Component.Factory
    interface Factory {
        fun create(): AppComponent
    }
}

class App : Application() {

    private val component: AppComponent = DaggerAppComponent.factory().create(this)

    override fun getSystemService(name: String): Any {
        if (name == AppScope::class.qualifiedName) {
            return component
        }
        return super.getSystemService(name)
    }
}
```
