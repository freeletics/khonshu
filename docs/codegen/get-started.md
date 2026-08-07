# Get started

Khonshu's Codegen is a KSP processor that helps with generating dependency injection related code and
common boilerplate for screens.

## Advantages

- eliminate boilerplate that usually needs to be repeated on each screen
- integrated with Khonshu's navigation to simplify its setup
- easily let objects survive configuration changes


## Dependency

```groovy
implementation("com.freeletics.khonshu:codegen-runtime:<latest-version>")
ksp("com.freeletics.khonshu:codegen-compiler:<latest-version>")
```


## Basic usage

The `@NavDestination` annotation is added to the top level composable of a screen. This function
can have 2 parameters: the state that should be rendered and a lambda that allows
the composable to send actions for user interactions. Adding the annotation will then generate
another Composable function called `KhonshuExampleUi`, a Metro graph and a `NavDestination`
that uses the given `route` and the generated composable.

```kotlin
@NavDestination(
    route = ExampleRoute::class,
    parentScope = ActivityScope::class, // ActivityScope is the default value and can be omitted
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
*`scope`, `parentScope` and `destinationScope` are described in the next sections/*

The generated `KhonshuExampleUi` function will use the generated graph, the
annotated composable as well as the `stateMachine` parameter from the
annotation. It will automatically hook up the
state machine with the composable so that the state from the state machine
is passed to the composable and actions from the latter are sent back to the
state machine. The generated composable will use the generated graph
to obtain the state machine.


## Generated graph

All annotations have a `scope` and a `parentScope` parameter. The scope will be used in Metro's
`@GraphExtension` annotation and the parent scope is used to contribute the `@GraphExtension.Factory`
to the parent graph.

Since the generated subgraph is using `@GraphExtension`, it is possible
to use `@ContributesTo`, `@ContributesBinding` and so on with that same scope
to contribute objects into it.

`scope` is also used in the `@SingleIn` annotation on the generated Metro graph
Any object using this scope will automatically survive configuration changes and will
not be recreated together with the UI.

A factory for the generated subgraph is automatically generated and contributed to
the graph that uses `parentScope` as its own scope.

For convenience purposes the generated graph will make the instance of `route` that was used to
navigate to the screen and a `SavedStateHandle` available which can be injected to classes like
the state machine to save state. When injecting the `SavedStateHandle` a `@ForScope(ExampleScope::class)`
qualifier needs to be added.


## NavDestination

A `NavDestination` is automatically generated for each annotated screen. The type of the generated
destination is based on the used `NavRoute`. To get an `OverlayDestination` the route class needs
to implement the `Overlay` marker interface.

To avoid having to access any generated code the destination is directly contributed to
a `Set<NavDestination>` which can then be injected where the `NavHost` is
created. By default this set lives in the `AppScope` graph, but this can be changed
by setting a different scope marker class to `destinationScope`. It's also possible to use
`destinationScope` to create distinct sets of destinations. For example if an app has a logged in
and a logged out graph, their scopes could be used as `destinationScope` depending on whether
a screen is shown in the logged in or logged out state.


## Navigation set up

The integration of Khonshu's Codegen and Navigation libraries also expects a `DestinationNavigator`
binding to be available. This can be easily achieved by adding `@SingleIn(ExampleScope::class)
@ForScope(ExampleScope::class) @ContributesBinding(ExampleScope::class, binding<DestinationNavigator>())`
to a subclass of it. The generated code will automatically take care of setting up
the navigator by calling `ActivityNavigatorEffect` in the compose UI layer with the navigator.


## Sharing objects between screens

Sometimes it is needed to share an object between 2 or more screens, for example
in a flow of screens that are connected to each other and work on the same data.
This is possible by using the route of the first screen in the flow as `parentScope`
for the other scopes. Internally this will cause the generated graph for the
first screen to become the parent for the graphs of the other screens.

This then allows injecting anything that is available in the scope of the first screen,
including the `SavedStateHandle` and route of the initial/parent screen.


## Example

This is a minimal example of how using Khonshu's Codegen for a screen would look like.

```kotlin
// state machine survives orientation changes
@Inject
@SingleIn(ExampleRoute::class)
internal class ExampleStateMachine(
    val route: ExampleRoute, // inject the navigator route that was used to get to this screen
    @ForScope(ExampleRoute::class)
    val savedStateHandle: SavedStateHandle, // a saved state handle tied to this screen
    val repository: ExampleRepository, // a repository that pas provided somewhere in the app
) : StateMachine<ExampleState, ExampleAction> {
    // ...
}

@Inject
// scope the navigator so that everything interacts with the same instance
@SingleIn(ExampleRoute::class)
@ForScope(ExampleRoute::class)
// make ExampleNavigator available as DestinationNavigator so that the generated code can automatically
// set up the navigation handling
@ContributesBinding(ExampleRoute::class)
class ExampleNavigator(hostNavigator: HostNavigator) : DestinationNavigator(hostNavigator) {
    // ...
}


@NavDestination(
    route = ExampleRoute::class, // the route used to navigate to ExampleUi
    parentScope = AppScope::class, // the scope of the app level graph
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
graph through the application class. This can be easily done by implementing `GlobalGraphProvider`:

```kotlin
@SingleIn(AppScope::class)
@DependencyGraph(scope = AppScope::class)
interface AppGraph {
    // allows an Activity to get all generated NavDestinations to set up the NavHost
    val destinations: Set<NavDestination>

    @DependencyGraph.Factory
    interface Factory {
        fun create(): AppGraph
    }
}

class App : Application(), GlobalGraphProvider {
    private val graph by lazy {
        createGraphFactory<AppGraph.Factory>().create()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getGraph(scope: KClass<*>): T {
        if (scope == AppScope::class) {
            return graph as T
        }
        throw IllegalArgumentException("Unknown scope")
    }
}
```
