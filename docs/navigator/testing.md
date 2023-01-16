# Testing

The `NavEventNavigator` exposes a `navEvents` property that has `Flow<NavEvent>` as type. On any
of the navigation calls this `Flow` will emit a new `NavEvent`. To test logic that triggers
navigation this can be used together with the [Turbine library](https://github.com/cashapp/turbine)
to assert that the correct events are emitted.

```kotlin
navigator.navEvents.test {
    navigator.navigateTo(TestRoute())
    assertThat(awaitItem).isEqualTo(NavEvent.NavigateToEvent(TestRoute()))
    navigator.navigateBack()
    assertThat(awaitItem).isEqualTo(NavEvent.BackEvent)
}
```

This is a very simplified example, in reality the test would most likely not call the navigator
itself but some other code that uses the navigator internally.
