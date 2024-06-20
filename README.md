# Khonshu

Khonshu is the foundation of how we build apps at Freeletics. It provides the general set up
for screens and navigation. The project grew over time based on our needs, issues we ran into
and common pitfalls we wanted to avoid.

## Navigation

A navigation library that allows to separate navigation logic from the UI layer and provides
a scalable approach to type safe navigation in a highly modularized code base.

For more information check out the [docs][4].


## Codegen

Codegen provides a plugin for [Anvil][5] that helps with generating dependency injection
related code and common boilerplate for screens as well as handling most of the set up
of the navigation library.

For more information check out the [docs][6].


## Helpers

Small libraries that provide utility functionality or a base for other libraries to build upon.

For more information check out the [docs][7].


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
[4]: https://freeletics.github.io/khonshu/navigation/get-started/
[5]: https://github.com/square/anvil
[6]: https://freeletics.github.io/khonshu/codegen/get-started/
[7]: https://freeletics.github.io/khonshu/helpers/
