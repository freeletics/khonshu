# Get started

We believe that navigation should be triggered from the business logic. This way it is
separated from the UI layer and becomes easily testable in isolation without having to
actually navigate inside a running app.

Originally this library was a wrapper around AndroidX navigation which was eventually
removed from the internals.The initial XML based definitions of destinations
and safe-args code generation did not scale well in a modularized code base where each
screen is its own module and where those modules shouldn't depend on each other. The
String based routes that were introduced with AndroidX navigation for Compose solved
the dependency issue but lost all type safety. This part of the original motivation
was mostly resolved with the introduction of type safe navigation for compose.

## Dependency

```groovy
implementation("com.freeletics.khonshu:navigation:<latest-version>")
```


## Destinations

A destination consists of 2 parts:
- the declaration of the destination itself which determines what is shown when the destination is navigated to;
- the route - a way to reach the destination.

### NavRoute

The route part is represented by the `NavRoute` interface. Each destination will get its own
implementation of this interface and screens can use it to navigate to the destination.

The most minimal implementation of `NavRoute` would be for a screen that doesn't require any
arguments can be a simple Kotlin object:
```kotlin
@Parcelize
data object HomeScreenRoute : NavRoute
```

The more common case when a destination needs arguments passed to it would look like this:
```kotlin
@Parcelize
data class DetailScreenRoute(
    val id: String,
) : NavRoute
```

!!!note

    For screens at the root of a back stack, like the start destination or a screen for a bottom navigation tab,
    there is a separate `NavRoot` interface. This nicely separates start destinations from regular destinations.


### NavDestination

The other part of the destination is represented by `NavDestination`.

If we take the `DetailScreenRoute` example from above, declaring the destination for it would look
like this:

```kotlin
val detailScreenDestination: NavDestination = ScreenDestination<DetailScreenRoute> { route: DetailScreenRoute ->
    DetailScreen(route)
}
```

The `ScreenDestination` function will return a new `NavDestination` which is linked to the route
that was passed as the generic type parameter. The lambda function then gets an instance of that
`NavRoute` and calls the `@Composable` function that should be shown.

There is also an `OverlayDestination` function to declare destinations that use a dialog or bottom
sheet as a container instead of being shown full screen.

```kotlin
val infoSheetDestination: NavDestination = OverlayDestination { route: InfoSheetRoute ->
    ModalBottomSheet(onDismissRequest = { /* TODO */ }) {
        InfoSheetContent(route)
    }
}

val confirmationDialogDestination: NavDestination = OverlayDestination { route: ConfirmationDialogRoute ->
    Dialog(onDismissRequest = { /* TODO */ }) {
        ConfirmationDialogContent(route)
    }
}
```

## Setup

To bring it all together there are 2 more core parts in the library.

### HostNavigator

To navigate between destinations `HostNavigator` is used. It's most important property is that
it does not have any `Activity` or other `Context` references and because of that can be referenced
in places where it survives configuration changes.

The `rememberHostNavigator(...)` method allows obtaining an instance of `HostNavigator` by passing
a start route and the set of all destinations to it:
```kotlin
val hostNavigator = rememberHostNavigator(
    startRoot = StartScreen,
    // set of destinations defined as above
    // in practice the set should be defined outside of Compose
    destinations = persistentSetOf(
        startDestination,
        detailScreenDestination,
        infoSheetDestination,
        confirmationDialogDestination,
    )
)
```

After that it's possible to navigate with these methods:
```kotlin
// navigate to the destination that the given route leads to
navigator.navigateTo(DetailScreenRoute("some-id"))
// navigate up in the hierarchy
navigator.navigateUp()
// navigate to the previous destination in the backstack
navigator.navigateBack()
// navigate back to the destination belonging to the referenced route and remove all destinations
// in between from the back stack, depending on inclusive the destination
navigator.navigateBackTo<MainScreenRoute>(inclusive = false)
```

### NavHost

The last remaining part is showing the UI which is handled by `NavHost`.

```kotlin
    setContent {
        val hostNavigator = rememberHostNavigator(/* ... */)
        NavHost(
            hostNavigator = hostNavigator,
            modifier = Modifier,
        )
    }
```

## Scalability

For the simplicity of the examples above the destinations were just kept in a variable and
then used to manually create a set of all of them. In a modularized project the routes are
usually declared in a shared module and the destinations then in the individual feature modules
where the respective screen is implemented. The set would then mean that for each new feature module
a developer would need to remember to go to the app module and add the destination to the set.

In practice it makes more sense to use dagger multi bindings to declare and collect destinations:

```kotlin
@Module
object DetailScreenModule {
	@Provides
	@IntoSet
	fun provideDetailScreenDestinations() = ScreenDestination<DetailScreenRoute> {
		DetailScreen(it)
	}
}
```

Then an `Activity` or something else can simply inject a `Set<NavDestination>` and use that for the
set up:

```kotlin
class MainActivity : ComponentActivity() {
	@Inject
	lateinit var destinations: Set<NavDestination>

	override fun onCreate(savedInstanceState: Bundle) {
		super.onCreate()

        // inject the activity

		setContent{
            val hostNavigator = rememberHostNavigator(
                startRoot = StartScreen,
                destinations = destinations,
            )
			NavHost(
				hostNavigator = hostNavigator,
                modifier = Modifier,
			)
		}
	}
}
```


## NavEventNavigator


### Other functionality

There are various additional `NavEventNavigator` APIs to simplify common navigation related
tasks:

- [Back clicks](back.md) for custom back behavior
- [Destination result handling](results.md) to deliver and obtain results to
  a previous destination
- [Multiple back stack support](back-stacks.md) for supporting something like
  bottom navigation where each tab has its own separate back stack
- [Navigating to an Activity](activities.md) both inside the current app or
  in other apps
- [Activity result handling](activities.md#activity-results) like
  `startActivityForResult`/`onActivityResult` and `ActivityResultContract`
- [Permission result handling](activities.md#requesting-permissions) like
  `requestPermissions`/`onRequestPermissionsResult` and `ActivityResultContract.RequestPermissions`
- [Deep links](deeplinks.md) for sending deep links within the app and for handling deep links
  coming from the outside.
- [Test helpers](testing.md) to make testing navigation logic easier
