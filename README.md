# Modern Android Development - The Freeletics Way

At Freeletics use many of the Jetpack/AndroidX libraries but we don't
always use them in the way they are advertised in Google's tutorials.
This project show cases our own flavored version of Modern Android Development
as well as some of our own utilties.

**This repository is a work in progress. More will be added over time.**

## StateMachine

`StateMachine` is a very simple interface to implement a StateMachine with the concept of emitting
state through a `StateFlow` and receiving input actions to mutate that state.

For an example on how to build such a state machine check out [FlowRedux][2]. To connect a
`StateMachine` to a user interface you can look at [Renderer][3] for the Android View world.
In compose the `StateMachine` can be observed using `collectAsState()` and actions can be dispatched
to it by simply launching them from a `CoroutineScope` created with `rememberCoroutineScope()`.

```groovy
implementation 'com.freeletics.mad:state-machine:0.2.0'
```

## TextResource

`TextResource` is a domain specific model to represent text. Abstracts text
whether it a localized `String` sent by the backend, a simple Android `string`
resource (with or without formatting args) or an Android `plurals` reosurce.
This way business logic with text can be easily tested without requiring
`Context` and running on a device.

For more information about the motivation for this abstraction check out this
[blog post][1].

```groovy
implementation 'com.freeletics.mad:text-resource:0.2.0'
```

# License

```
Copyright 2021 Freeletics GmbH.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

  [1]: https://freeletics.engineering/2021/01/22/abstraction-text-resource.html
  [2]: https://freeletics.github.io/FlowRedux/dsl/
  [3]: https://github.com/gabrielittner/renderer
