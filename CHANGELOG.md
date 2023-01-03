Change Log
==========

## 0.10.0 **UNRELEASED**

### Navigator
- `navigateToRoot` now has a `saveCurrentRootState` parameter which defaults to true (matches the previous implicit behavior). Can be set to false to clear the current back stack.
- `navigateBackTo` now does not allow passing a `NavRoot` as target anymore. Use `navigateToRoot<...>(false, false)` instead
- `ActivityRoute` is now  always `Parcelable` and has been split into 2 sub-classes/interfaces `InternalActivityRoute` for `Activity` classes inside the app and `ExternalActivityRoute` for Intents that leave the app
- `PermissionResult` is now a sealed class instead of an enum class. The `DENIED` and `DENIED_FOREVER` values were merged into a single `Denied` subclass that has a `showRationale` boolean property. This being `false` would match the old denied forever value. The reason for this change is that `Denied` with `showRationale` being `false` does not necessarily mean denied forever on newer platform versions, it could also mean that the first ever permission prompt was dismissed without making a choice.
- Make a few APIs that were not meant to be public internal or mark them as such
- Internal: share more code between the compose and fragment implementations
- Internal: unify handling of permission results and activity results

### Whetstone
- Remove dependency on AndroidX navigation. Whetstone’s navigation artifacts now only rely on our own navigator APIs
- Internal: Stop generating view models for each annotated screen. Instead use a runtime class to hold on to components

## 0.9.1 *(2022-11-23)*

### Whetstone
- Fix action lambda in Compose code generation


## 0.9.0 *(2022-11-22)*

### Whetstone
- Add support for injecting dependencies into Composable functions, which are annotated with
`@ComposeScreen` or `@ComposeFragment`

### Navigator
- Refactor runtime module to remove androidx navigation


## 0.8.1 *(2022-11-08)*

### Whetstone
- Update codegen to use new `Bundle.requireRoute` function

### Other
- New module for shared androidx navigation
- Deploy docs directly from Github Actions


## 0.8.0 *(2022-11-08)*

### Navigator

- Add `Activity.getRoute()` function than returns nullable `ActivityRoute`
- Use `requireNotNull` contracts for `requireRoute` functions
- Check for route to be not null before adding it to `Intent` in `CustomActivityNavigator`
- Move generated `DestinationComponent` to the nav entry generator. This reduces how often
we generate the contributed `DestinationComponent` by generating it only together with the
`NavEntryComponent` that needs it.

### Whetstone

- Rename `findDependencies` to `findComponentByScope`. After getting rid of component
dependencies in favor of subcomponents our find methods were not named correctly anymore.
- Fix `Closeable` typo
- Remove unused `SavedStateRegistryOwner`
- Explicitly require a renderer factory to extend `ViewRenderer.Factory`. We relied on some
custom factories internally. Those are not necessary anymore and we can add the constraint
to the annotation.

### Dependencies

- Update `kotlin` to v1.7.20
- Update dependency `app.cash.turbine:turbine` to v0.12.1
- Update dependency `androidx.fragment:fragment` to v1.5.4
- Update dependency `com.google.accompanist:accompanist-navigation-material` to v0.27.0
- Update `androidx-compose-runtime` to v1.3.0
- Update `androidx-navigation` to v2.5.3
- Update `androidx-activity` to v1.6.1
- Update dependency `org.jetbrains.kotlinx.binary-compatibility-validator` to v0.12.1
- Update dependency `com.android.library` to v7.3.1
- Update dependency `org.jetbrains.dokka` to v1.7.20
- Update dependency `com.google.dagger:dagger` to v2.44
- Update dependency `androidx.core:core` to v1.9.0
- Update dependency `com.vanniktech.maven.publish` to v0.22.0
- Update `androidx-navigation` to v2.5.2
- Update actions/setup-java action to v3
- Update actions/setup-python action to v4
- Update actions/checkout action to v3
- Update dependency `com.autonomousapps.dependency-analysis` to v1.13.1
- Update `anvil` to v2.4.2

### Other

- Migrate to Gradle ## catalog


## 0.7.2 *(2022-08-12)*

### Navigator

- Fragment: Fix that the start destination sometimes does not have any arguments
- Fix an issue that caused navigation results to be delivered multiple times


## 0.7.1 *(2022-07-12)*

- Sources are now visible in Android Studio again

### Navigator

- Fix a crash in the navigation result APIs


## 0.7.0 *(2022-07-04)*

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


### Navigator

- removed dependency on `LiveData`


## 0.6.0 *(2022-06-28)*

### Whetstone

- For components generated through `@NavEntryComponent` the automatically
  provided `SavedStateHandle`, `NavRoute`, `Set<Closeable>`, `CoroutineScope`
  and `CompositeDisposable` objects now use `@NavEntry(Scope::class)` as
  qualifier


## 0.5.0 *(2022-06-21)*

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


## 0.4.0 *(2022-06-13)*

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


## 0.3.1 *(2022-04-20)*

### Whetstone

- fix a crash when `@NavEntryComponent` is completely unused
- fix a crash when using Whetstone with Compose navigation


## 0.3.0 *(2022-04-13)*

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


## 0.2.0 *(2021-06-18)*

- update `StateMachine.state` to return `StateFlow<State>` instead of `Flow<State>`


## 0.1.1 *(2021-06-16)*

- fix windows artifact of `state-machine` not being published


## 0.1.0 *(2021-06-16)*

- initial release of the `state-machine` artifact
- initial release of the `text-resource` artifact
