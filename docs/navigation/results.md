# Destination results

Khonshu has a built-in way to receive results from another screen inside the app. Call
`registerForNavigationResult<Route, Result>()` on the screen's `DestinationNavigator2` to register.
`Route` should be the route class for that navigator's own destination, while `Result` is the type of the
expected result which can be any `Serializable` class. The register method
will return a `NavigationResultRequest`, which has a `results: Flow<Result>` property to collect the results.

When the route type matches its own destination, `DestinationNavigator2` registers the request on
the exact back stack entry it belongs to, even if another entry has the same route type. The API
can also be called on `HostNavigator`; in that case it registers on the topmost entry matching the
route type. See [Getting started](get-started.md#destinationnavigator2) for injecting a
`DestinationNavigator2` with Khonshu's codegen.

The navigation to the screen from which the result should be returned is a regular call to
`navigateTo`. However the `NavRoute` class for that target destination should have
`NavigationResultRequest.Key<Result>` as a parameter. An instance of such a `Key` can be obtained
from the `key` property of the request object.

The target screen can then simply call `deliverNavigationResult(route.key, result)` on its own
navigator to send the result and afterwards remove itself from the back stack with `navigateBack`.

The logic for a hypothetical `FooScreen` that wants to receive a result from `BarScreen` would look
like this:
```kotlin
@Serializable
data class MessageResult(val message: String)

class FooScreenViewModel(
    private val navigator: DestinationNavigator2,
) {
    // use request.results somewhere to handle the results that BarScreen delivers
    val request = navigator.registerForNavigationResult<FooScreenRoute, MessageResult>()

    fun navigateToScreenB() {
        // if needed BarScreenRoute could also have additional parameters
        navigator.navigateTo(BarScreenRoute(request.key))
    }
}
```

`BarScreen` would then have the following route and logic:
```kotlin
@Serializable
data class BarScreenRoute(
    val key: NavigationResultRequest.Key<MessageResult>,
) : NavRoute

class BarScreenViewModel(
    private val navigator: DestinationNavigator2,
    val route: BarScreenRoute,
) {
    fun deliverMessage(message: String) {
        navigator.deliverNavigationResult(route.key, MessageResult(message))
    }
}
```
