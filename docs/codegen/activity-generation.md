# Generating the Activity

Similar to generating the set up for each screen, Khonshu also supports generating
an `Activity` and the set up that is required for it. To do that add the
`@NavHostActivity` annotation to a `@Composable` function. The composable will then
be shown in the generated `Activity`.


```kotlin
 // a scope marker used to represent the `Activity`.
sealed interface ExampleActivityScope

@NavHostActivity(
    scope = ExampleActivityScope::class,
    parentScope = AppScope::class, // AppScope is the default value and can be omitted
    stateMachine = ExampleStateMachine::class,
    activityBaseClass = ComponentActivity::class, // the super class for the generated activity
)
@Composable
internal fun ExampleUi(
  state: ExampleState,
  sendAction: (ExampleAction) -> Unit,
  navHost: SimpleNavHost,
) {
    // place the given NavHost composable
    navHost(Modifier.fillMaxSize()) { root, route -> /* route changed listener */ }
}
```

The `scope` and `parentScope` work the same as `route` and `parentScope` for the destination code
generation. `stateMachine` also behaves the same and the composable will receive `state` from
it and can use `sendAction` to talk to the state machine.

A new parameter is the `navHost` composable. This composable is a pre-configured `NavHost` that
already has all destinations and deep links added to it. This leaves 2 parameters when calling
the composable:
- A `Modifier` to influence how the `NavHost` is shown on screen
- A `(NavRoot, BaseRoute) -> Unit)` function that will be called whenever the currently shown destination
  changes. This is optional and null can be passed instead.

The code generation will automatically take care of creating a `HostNavigator` and
makes it available in the `scope`. This means any screen can simply inject `HostNavigator` to
use it.

One requirement is that the `destinationScope` used by other screens is either the `scope` or
`parentScope` (or a parent of the parent if there are deeper scope hierarchies) used for
`@NavHostActivity`. Otherwise the `Set<NavDestination>` won't be available
for injection and Metro will fail when injecting dependencies.
