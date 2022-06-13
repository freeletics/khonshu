Change Log
==========

Version 0.4.0 *(2022-05-13)*
----------------------------

### Navigator

- New `ActivityRoute` interface that has to be used for `Activity` destinations. This
  allows creating a `fillInIntent` with parameters to the routes which will be merged
  with the destination `Intent`. Before this it was not possible to dynamically add
  values to the `Intent`. See the [README](navigator/README.md#activity-destinations)
- New `Activity.requireRoute` extension function that allows obtaining the `ActivityRoute`
  that was used to navigate to this `Activity`.
- `NavHost` now has a `destinationChangedCallback` parameter to receive destination
  changes for debugging purposes
- `NavHost` now has parameters to change the styling of its bottom sheet

### Whetstone

- Added README with documentation
- Fix code generation in Kotlin 1.6.20


Version 0.3.1 *(2022-04-20)*
----------------------------

### Whetstone

- fix a crash when `@NavEntryComponent` is completely unused
- fix a crash when using Whetstone with Compose navigation


Version 0.3.0 *(2022-04-13)*
----------------------------

### Navigator

A new library that is a wrapper around AndroidX navigation that allows to separate navigation
logic from the UI layer and provides a scalable approach to type safe navigation in a highly
modularized code base. For more information check out it's [README](navigator/README.md).

```groovy
implementation 'com.freeletics.mad:navigator:0.3.0'
// when using composables for navigation
implementation 'com.freeletics.mad:navigator-compose:0.3.0'
// when using fragments for navigation (even if these contain composables)
implementation 'com.freeletics.mad:navigator-fragment:0.3.0'
```

### Whetstone

Experimental release of an Anvil plugin that generates components and more for a screen. The
experimental status only means that this will have breaking changes in the future and
functionality might change significantly and that the documentation is missing (other than
comments on the code). It is already being used in production at Freeletics.


Version 0.2.0 *(2021-06-18)*
----------------------------

- update `StateMachine.state` to return `StateFlow<State>` instead of `Flow<State>`


Version 0.1.1 *(2021-06-16)*
----------------------------

- fix windows artifact of `state-machine` not being published


Version 0.1.0 *(2021-06-16)*
----------------------------

- initial release of the `state-machine` artifact
- initial release of the `text-resource` artifact
