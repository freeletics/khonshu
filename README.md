# Modern Android Development - The Freeletics Way

At Freeletics we use many of the Jetpack/AndroidX libraries but not always in the way 
they are advertised and recommended in Google's tutorials.
This project showcases our own flavored version of Modern Android Development
as well as some of our own utilities.

**This repository is a work in progress. More will be added over time.**


## Navigator

The navigator library is a wrapper around AndroidX navigation that allows to separate navigation
logic from the UI layer and provides a scalable approach to type safe navigation in a highly
modularized code base.

For more information check out its [README][4].

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/freeletics/mad?logo=github&sort=semver)](https://github.com/freeletics/mad/releases)

```groovy
implementation 'com.freeletics.mad:navigator:<latest-version>'
// when using composables for navigation
implementation 'com.freeletics.mad:navigator-compose:<latest-version>'
// when using fragments for navigation (even if these contain composables)
implementation 'com.freeletics.mad:navigator-fragment:<latest-version>'
```


## Whetstone

Whetstone is a plugin for [Anvil][5] that helps with
generating dependency injection related code and common boilerplate for feature screens.

For more information check out its [README][6].

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/freeletics/mad?logo=github&sort=semver)](https://github.com/freeletics/mad/releases)

```groovy
anvil  'com.freeletics.mad:whetstone-compiler:<latest-version>'

implementation 'com.freeletics.mad:whetstone-runtime:<latest-version>'
// when using Compose UI
implementation 'com.freeletics.mad:whetstone-runtime-compose:<latest-version>'
// when using fragments (even if these contain Compose UI)
implementation 'com.freeletics.mad:whetstone-runtime-fragment:<latest-version>'

// for the integration with Navigator
implementation 'com.freeletics.mad:whetstone-navigation:<latest-version>'
// for the integration with Navigator - when using Compose UI
implementation 'com.freeletics.mad:whetstone-navigation-compose:<latest-version>'
// for the integration with Navigator - when using fragments (even if these contain Compose UI)
implementation 'com.freeletics.mad:whetstone-navigation-fragment:<latest-version>'
```


## StateMachine

`StateMachine` is a very simple interface to implement a StateMachine with the concept of emitting
state through a `kotlinx.coroutines.flow.Flow` and receiving input actions to mutate that state.

For an example on how to build such a state machine check out [FlowRedux][2]. To connect a
`StateMachine` to a user interface you can look at [Renderer][3] for the Android View world.
When using Compose UI the `StateMachine` can be observed with `produceState()` and actions can be dispatched
to it by simply launching them from a `CoroutineScope` created with `rememberCoroutineScope()`.

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/freeletics/mad?logo=github&sort=semver)](https://github.com/freeletics/mad/releases)

```groovy
implementation 'com.freeletics.mad:state-machine:<latest-version>'
```


## TextResource

`TextResource` is a domain specific model to represent text. Abstracts text
whether it a localized `String` sent by the backend, a simple Android `string`
resource (with or without formatting args) or an Android `plurals` resource.
This way business logic with text can be easily tested without requiring
`Context` and running on a device.

For more information about the motivation for this abstraction check out this
[blog post][1].

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/freeletics/mad?logo=github&sort=semver)](https://github.com/freeletics/mad/releases)

```groovy
implementation 'com.freeletics.mad:text-resource:<latest-version>'
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
  [4]: navigator/README.md
  [5]: https://github.com/square/anvil
  [6]: whetstone/README.md
