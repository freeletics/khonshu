# Testing

The library has an optional test artifact that provides an additional test artifact that makes
it possible to test navigation logic through `HostNavigator` without running on an Android
device or emulator. The test artifact is heavily inspired by the
[Turbine library](https://github.com/cashapp/turbine) and is also using it internally.


## Dependency

```groovy
testImplementation("com.freeletics.khonshu:navigation-testing:<latest-version>")
```


## Standalone navigation tests

When testing a `HostNavigator` on its own call the `test` extension function on it

```kotlin

navigator.test {
    // assertions
}
```

This will start collecting events from the navigator and then calls the given block with
`NavigatorTurbine` as a receiver:

```kotlin

navigator.test {
    awaitNavigateTo(ExampleRoute("1"))
    awaitNavigateTo(ExampleRoute("2"))
    awaitNavigateBack()
}
```

The collection is automatically cancelled at the end of the `test` block.

## Navigation and other state

If the test does not just validate the navigation logic but also other things like for example
state changes a `NavigatorTurbine` can be obtained by calling `testIn`

```kotlin
runTest {
    val otherTurbine = someFlow.testIn(this)
    val navigatorTurbine = navigator.testIn(this)
    assertEquals(newState, otherTurbine.awaitItem())
    navigatorTurbine.awaitNavigateTo(ExampleRoute("1"))

    otherTurbine.cancel()
    navigatorTurbine.cancel()
}
```

Unlike `test`, the `testIn` extension function can not automatically clean up its coroutine, so it
is required to manually call `cancel` at the end of the test.

## Cancellation

While `test` and `testIn` have different ways of cancellation, both will validate that all
navigation events have been consumed upon cancellation. If there are any unconsumed events
that were not handled through one of the `await...` functions an `AssertionError` will be thrown
during the cancellation.


## Destination navigators

A navigator class for a single screen usually delegates to an injected `DestinationNavigator2`.
`TestHostNavigator` can create one that reports all events back to itself, so the same `test`/`testIn`
extensions can be used:

```kotlin
val hostNavigator = TestHostNavigator()
val navigator = ExampleNavigator(hostNavigator.destinationNavigator())

hostNavigator.test {
    navigator.showDetails("some-id")
    awaitNavigateTo(DetailScreenRoute("some-id"))
}
```

Because `DestinationNavigator2` ignores back navigation while its destination is not the current
destination, that behavior can be tested by passing `isCurrentDestination = false`:

```kotlin
val navigator = ExampleNavigator(hostNavigator.destinationNavigator(isCurrentDestination = false))

hostNavigator.test {
    navigator.close()
    // no event is emitted because the destination is not the current one anymore
}
```

## Result receivers

When testing code that deals with `Activity`, permission or navigation results it is often needed
to send fake results to the collector. The library provides a `sendResult` extension function for
each request type to do that.

For example if there would be the following navigator:

```kotlin
class MyNavigator : ActivityNavigator() {
    val permissionRequest = registerForPermissionsResult()
}
```

If the code under test collects `permissionRequest.results`, it would be possible to call
`navigator.permissionRequest.sendResult("permission", PermissionResult.Granted)` to simulate
the request succeeding.


## Navigation result senders

When testing a component that delivers navigation results a `NavigationResultRequest.Key` is usually
required. Tests can obtain such a key with the `fakeNavigationResultKey()` helper method.
