# Helpers

Small libraries that provide utility functionality or a base for other libraries to build upon.

## StateMachine

`StateMachine` is a very simple interface to implement a StateMachine with the concept of emitting
state through a `kotlinx.coroutines.flow.Flow` and receiving input actions to mutate that state.

For an example on how to build such a state machine check out [FlowRedux][2]. When using Compose UI
the `StateMachine` can be observed with `produceState()` and actions can be dispatched
to it by simply launching them from a `CoroutineScope` created with `rememberCoroutineScope()`.

```groovy
implementation("com.freeletics.khonshu:state-machine:<latest-version>")
```

Additionally there is a test artifact that provides `StateMachine.test` and `StateMachine.testIn`
extension functions.

```groovy
testImplementation("com.freeletics.khonshu:state-machine-testing:<latest-version>")
```

## TextResource

`TextResource` is a domain specific model to represent text. Abstracts text
whether it a localized `String` sent by the backend, a simple Android `string`
resource (with or without formatting args) or an Android `plurals` resource.
This way business logic with text can be easily tested without requiring
`Context` and running on a device.

For more information about the motivation for this abstraction check out this
[blog post][1].

```groovy
implementation("com.freeletics.khonshu:text-resource:<latest-version>")
```
