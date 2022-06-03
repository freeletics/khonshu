# Whetstone

Whetstone is a plugin for [Anvil](https://github.com/square/anvil) that helps with
generating dependency injection related code and common boilerplate for screens.

## Advantages

- eliminate boilerplate that usually needs to be repeated on each screen
- optional integration with Navigator to simplify its setup 
- easily let objects survive orientation changes 

One general advantage is around Fragments. If an app uses them or has to use them for legacy 
reasons, Whetstone will mostly hide them from the developers because it's generating the Fragment
for each screen it's used in. This way logic is kept out of them and the actual components like
UI and state machine (presenter/view model) can be tested more easily in isolation. It also makes
a migration away from Fragments easier since generated code can be easily replaced by other 
generated code. In fact migrating an app where each screen uses whetstone from Fragments to 
just using Compose is as easy as replacing a single annotation on each screen.

## Basic usage

Using the library depends a bit on the app it's being used in. If the app
is 100% compose and does not have any Fragments then the `@ComposeScreen`
annotation is the way to go. For apps build with Fragments there is a
`@ComposeFragment` annotation for screens using compose and for screens build
with views there is `@RendererFragment`.

### Pure Compose

In a pure compose app the `@ComposeScreen` annotation is added to the
top level composable that represents your screen. This function should have
exactly 2 parameters: the state that should be rendered and a lambda that allows
the composable to send actions for user interactions. This will then generate
another Composable function called `ExampleUiScreen` and a Dagger component.

```kotlin
@ComposeScreen(
    scope = ExampleScope::class,
    parentScope = AppScope::class,
    dependencies = ExampleDependencies::class,
    stateMachine = ExampleStateMachine::class,
)
@Composable
internal fun ExampleUi(
  state: ExampleState,
  actions: (ExampleAction) -> Unit
) {
  // composable logic ...
}
```


The generated `ExampleUiScreen` function will use the generated component, the
annotated composable as well as the `stateMachine` parameter from the
annotation. It will automatically hook up the
state machine with the composable so that the state from the state machine
is passed to the composable and actions from the latter are sent back to the
state machine. The generated composable will use the generated component
to obtain the state machine.

### Compose with Fragments

This works very similar to the pure compose version except that you use the
`@ComposeFagment` annotation. Instead of generating another composable this
will then generate a `ExampleUiFragment` class and a Dagger component.

```kotlin
@ComposeFragment(
    scope = ExampleScope::class,
    parentScope = AppScope::class,
    dependencies = ExampleDependencies::class,
    stateMachine = ExampleStateMachine::class,
)
@Composable
internal fun ExampleUi(
  state: ExampleState,
  actions: (ExampleAction) -> Unit
) {
  // composable logic ...
}
```

The generated `Fragment` will do the same thing as the generated composable would.
It uses the same generated component to obtain an instance of the `stateMachine`
class and then will connect the annotated composable with that.

The annotation has an optional `fragmentBaseClass` parameter that allows to
specify a class other than `Fragment` to be used as super class for the
generated Fragment. This allows using `DialogFragment` or `BottomSheetDialogFragment`
for example.


### Views with Fragments

The last covered use case is view based screens in an app that uses Fragments.
This is based on the separate [Renderer library](https://github.com/gabrielittner/renderer)
which separates the view/ui logic from Fragments or other framework classes.
Similar to the composables above a Renderer receives a state object and emits
actions. Whetstone has a `@RendererFragment` annotation which will then, like the
compose annotation, trigger the generation of a `ExampleRendererFragment` and a
Dagger component.


```kotlin
@RendererFragment(
    scope = ExampleScope::class,
    parentScope = AppScope::class,
    dependencies = ExampleDependencies::class,
    rendererFactory = ExampleRenderer.Factory::class, // references the factory below
    stateMachine = ExampleStateMachine::class,
)
internal class ExampleRenderer @AssistedInject constructor(
    @Assisted private val binding: ExampleViewBinding,
) : ViewRenderer<ExampleState, ExampleAction>(binding) {

    override fun renderToView(state: ExampleState) {
        // view logic
    }

    @AssistedFactory
    abstract class Factory : ViewRenderer.Factory<ExampleViewBinding, ExampleRenderer>(ExampleViewBinding::inflate)
}
```

The generated code is very similar except that the generated `Fragment` will use
the annotated `Renderer` instead of a composable for its UI.

The annotation has the same optional `fragmentBaseClass` parameter that
`@ComposeFragment` has.


## Generated component

All the annotations have the `scope`, `dependencies` and `parentScope` parameter in
common. The first 2 will be used on `@MergeComponent` annotation of the generated
component, i.e. `@MergeComponent(scope = ExampleScope::class, dependencies = [ExampleDependencies::class])`.

Since the generated component is using Anvils `@MergeComponent`, it is possible
to use `@ContributesTo`, `@ContributesBinding` and so on with that same scope
to contribute objects into it.

`scope` is also used for Dagger scopes. The generated component is annotated
with the `@ScopeTo` annotation that ships with the Whetstone runtime and uses
the `scope` value as a parameter. To scope a class just add
`@ScopeTo(ExampleScope::class)` to it. Any object using this scope will
together with the component automatically survive configuration changes and
will not be recreated together with the UI.

The `dependencies` interface for [component dependencies](https://dagger.dev/api/2.22/dagger/Component.html#component-dependencies)
will be looked up internally using `Context.getSystemService(name)` using the
fully qualified name of the given `parentScope` as key for the lookup. It is
expected that the app will provide it through it's `Application` class or an
`Activity`. It is recommended to add `@ContributesTo` the dependencies interface
and that the `parentScope` value should be the same one that is used in that
annotation.

For convenience purposes the generated component will make a `SavedStateHandle`
available which can be injected to classes like the state machine to save state.

## Navigator integration

Whetstone has a special integration with the [MAD Navigator library](../navigator/README.md)
and has a `@NavDestination` annotation to also include the navigation setup in the
generated code.

It can be used like this and is added next to the
`@ComposeScreen`/`@ComposeFragment`/`@RendererFragment` annotation:
```kotlin
@ComposeScreen(...) // works the same for all whetstone annotations
@NavDestination(
    route = ExampleRoute::class,
    type = DestinationType.SCREEN,
    destinationScope = AppScope::class,
)
@Composable
fun ExampleUi(...) {...}
```

The `route` parameter should reference the `NavRoute` class that is used
to navigate to the screen with the annotated UI. The instance of `NavRoute`
that was passed to the screen when navigating to it will be automatically
available in the generated component, so it can be injected into the state
machine or other classes to read the parameters.

When the `NavDestination` is added Whetstone also expects a `NavEventNavigator`
to be injectable. This can be easily achieved by adding
`@ScopeTo(ExampleScope::class) @ContributesBinding(ExampleScope::class, NavEventNavigator::class)`
to a subclass of it. Whetstone will automatically take care of setting up
the navigator by calling `NavigationSetup` for compose and `handleNavigation`
for Fragments inside the generated code.

The last part of the integration is that Whetstone will automatically generate
a `NavDestination` for the screen by using the `route` and `type`. This
generated destination is automatically provided into a `Set` in the component
that uses `destinationScope` as it's scope (usually an app wide or Activity
level scope). With that it's not necessary to manually create a `Set`
of all destinations anymore. It can simply be injected.


## Example

This is a minimal example of how using Whetstone for a screen with the Navigator integration 
would look like.

```kotlin
// marker class for the scope
sealed interface Example

// dependencies interface that is contributed to the AppScope component
@ContributesTo(AppScope::class)
interface ExampleDependencies {
    val repository: ExampleRepository // makes ExampleRepository from AppScope available
}

// state machine survives orientation changes
@ScopeTo(Example::class)
internal class ExampleStateMachine @Inject constructor(
    val route: ExampleRoute, // inject the navigator route that was used to get to this screen
    val repository: ExampleRepository, // the repository
    val navigator: ExampleNavigator,
) : StateMachine { ... }

// scope the navigator so that everything interacts with the same instance
@ScopeTo(Example::class) 
// make ExampleNavigator available as NavEventNavigator so that the generated code can automatically
// set up the navigation handling
@ContributesBinding(Example::class, NavEventNavigator::class) 
class ExampleNavigator @Inject constructor() : NavEventNavigator() { ... }

@ComposeScreen(
    scope = Example::class, // uses our marker class
    parentScope = AppScope::class, // same parent scope as we use to contribute ExampleDependencies
    dependencies = ExampleDependencies::class, // the dependencies interface
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

Using this would require a one time setup in the app so that the screens can look up the `AppScope`
component through `getSystemService` to retrieve the dependencies:

```kotlin
@Singleton
@MergeComponent(scope = AppScope::class)
interface AppComponent {
    // allows an Activity to get all generated NavDestinations to set up the NavHost
    val destinations: Set<NavDestination>
    
    @Component.Factory
    interface Factory {
        fun create(): AppComponent
    }
}

class App : Application(), ImageLoaderFactory {

    private val component: AppComponent = DaggerAppComponent.factory().create(this)
    
    override fun getSystemService(name: String): Any? {
        if (name == AppScope::class.qualifiedName) {
            return component
        }
        return super.getSystemService(name)
    }
}

```
