# Destination results

Khonshu has a built-in way to receive results from another screen inside the app. For that
it is required to call `HostNavigator.registerForNavigationResult<Route, Result>`. `Route` in this
case should be the `NavRoute` class for the current screen, while `Result` is the type of the
expected result which can be any `Parcelable` class. The register method
will return a `NavigationResultRequest`, which has a `results` property that returns a
`Flow<Result>` to collect the results.

The navigation to the screen from which the result should be returned is a regular call to
`navigateTo`. However the `NavRoute` class for that target destination should have
`NavigationResultRequest.Key<Result>` as a parameter. An instance of such a `Key` can be obtained
from the `key` property of the request object.

The target screen can then simply call `deliverNavigationResult(route.key, result)` on its own
navigator to send the result and afterwards remove itself from the back stack with `navigateBack`.

The logic for a hypothetical `FooScreen` that wants to receive a result from `BarScreen` would look
like this:
```kotlin
data class MessageResult(val message: String): Parcelable

class FooScreenViewModel(
    private val hostNavigator: HostNavigator
) {
    // use request.results somewhere to handle the results that BarScreen delivers
    val request = hostNavigator.registerForNavigationResult<FooScreenRoute, MessageResult>()

    fun navigateToScreenB() {
        // if needed BarScreenRoute could also have additional parameters
        hostNavigator.navigateTo(BarScreenRoute(request.key))
    }
}
```

`BarScreen` would then have the following route and logic:
```kotlin
data class BarScreenRoute(
    val key: NavigationResultRequest.Key<MessageResult>,
) : NavRoute

class BarScreenViewModel(
    private val hostNavigator: HostNavigator,
    val route: BarScreenRoute,
) {
    fun deliverMessage(message: String) {
        hostNavigator.deliverNavigationResult(route.key, MessageResult(message))
    }
}
```
