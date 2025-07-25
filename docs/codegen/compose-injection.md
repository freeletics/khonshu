# Injecting Composables

For the rare case that an object needs to be shared between UI and other components, Khonshu's Codegen
supports injecting objects into the annotated Composable. Any parameter of that function that
is not the `state` or `sendAction` parameter will be looked up through the generated
Metro graph.

Injecting the following `ExampleClass`

```kotlin
// could use @SingleIn(...) with the matching scope or a parent scope
@Inject
class ExampleClass {
    // implementation ...
}
```

would then look like this:

=== "Compose"

    ```kotlin
    @ComposeScreen(...)
    @Composable
    internal fun ExampleUi(
      exampleClass: ExampleClass,
      state: ExampleState,
      sendAction: (ExampleAction) -> Unit
    ) {
      // composable logic ...
    }
    ```

=== "Compose with Fragments"

    ```kotlin
    @ComposeFragment(...)
    @Composable
    internal fun ExampleUi(
      exampleClass: ExampleClass,
      state: ExampleState,
      sendAction: (ExampleAction) -> Unit
    ) {
      // composable logic ...
    }
    ```
