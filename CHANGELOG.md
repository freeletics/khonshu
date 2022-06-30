Change Log
==========


Version 0.7.0 *(UNRELEASED)*
----------------------------

Updated to Kotlin 1.7.0 and Compose compiler 1.2.0.

### Whetstone

- Instead of generating full components Whetstone is now using subcomponents with Anvil's
  `@ContributesSubcomponent` with a `ParentComponent` interface that is automatically contributed
  to the `parentScope`. This allows to remove `kapt` from modules using Whetstone and let Anvil
  do all of the factory generation
- Because of that the `dependencies` parameter was removed from all annotations
- To avoid collisions between subcomponents the `SavedStateHandle` as well as the `NavRoute` or
  `Bundle` that are automatically available in generated `@NavEntryComponent`s now have a
  `NavEntry(ScopeOfNavEntryComponent::class)` qualifier on them
- The `rxJavaEnabled` and `coroutinesEnabled` parameters were removed from all annotations. For how
  to replace them, see the 0.5.0 changelog.


Version 0.6.0 *(2022-06-28)*
----------------------------

### Whetstone

- For components generated through `@NavEntryComponent` the automatically
  provided `SavedStateHandle`, `NavRoute`, `Set<Closeable>`, `CoroutineScope`
  and `CompositeDisposable` objects now use `@NavEntry(Scope::class)` as
  qualifier


Version 0.5.0 *(2022-06-21)*
----------------------------

### Navigator

- fix an issue where an `ActivityRoute` that is also `Parcelable` would not be added to the `Intent` extras

### Whetstone

- It is now possible to provide `Closeable` objects into a set and have the generated `ViewModel`
  close all of these when it is cleared. This is meant as a replacement for the `rxJavaEnabled`
  and `coroutinesEnabled` flags. You can replace the flags with the following snippet:

```kotlin
@ContributesTo(MyScreenScope::class)
object {
  @Provides
  @ScopeTo(MyScreenScope::class)
  fun provideCompositeDisposable() = CompositeDisposable()

  @Provides
  @IntoSet
  fun bindCompositeDisposable(disposable: CompositeDisposable) = Closeable { disposable.clear() }

  @Provides
  @ScopeTo(MyScreenScope::class)
  fun provideCoroutineScope() = MainScope()

  @Provides
  @IntoSet
  fun bindCoroutineScope(scope: CoroutineScope) = Closeable { scope.cancel() }
}
```

- removed `enableInsetHandling` parameter from `@ComposeFragment`, Compose 1.2.0 provides new inset
  APIs that work out of the box without any special integration and the Accompanist Inset library was
  deprecated


Version 0.4.0 *(2022-06-13)*
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
