Change Log
==========

## 0.22.0 **UNRELEASED**

### Navigation

- **Breaking**: `NavDestination`, `ScreenDestination`, `OverlayDestination`, `ActivityDestination`
  and `NavigationSetup` have been moved to the `com.freeletics.khonshu.navigation` package inside
  `navigation`. Previously these were duplicated between `navigation-compose` and `navigation-experimental`.
- **Breaking**: Moved `navigation-compose` `NavHost` to `com.freeletics.khonshu.navigation.androidx` and
  `navigation-experimental` `NavHost` to `com.freeletics.khonshu.navigation`. This now allows to use
  the AndroidX based and the experimental navigation implementation in the same app and switch between
  them with a feature flag.
- **Removed**: `navigation-fragment` and `Fragment` navigation support.
- **Removed**: `navigation-androidx` has been inlined into `navigation-compose`.

### Codegen

- **Breaking**: The `NavDestination` and `NavHostActivity` annotations as well as `SimpleNavHost`
  have been moved to `com.freeletics.khonshu.codegen`.
- **Added**: `NavHostActivity` has an `experimentalNavigation` boolean to generate code
  with a `navigation-experimental` `NavHost`.
- **Added**: `ActivityScope` and custom `Activity` scopes can now be used as `parentScope` for
  destinations.
- **Removed**: `@ComposeFragmentDestination` and `Fragment` codegen support.


## 0.21.0 *(2023-12-07)*

- **Note**: `Fragment` navigation and codegen have been deprecated and will be
  removed in the next release.
- Updated Kotlin to 1.9.21.

### Navigation

- **Breaking**: `navigateBackTo<...>(...)` is now an extension method and might need
  to be explicitly imported.
- **Breaking**: The `Set` parameters of `NavHost` have been replaced with `ImmutableSet`
  to allow the compose compiler to recognize these as immutable.
- **Breaking**: Removed `navController` parameter from `HavHost`. Passing a manually
  created `NavHostController` introduced issues like breaking deep link handling.
- **New**: `NavHost` (both the AndroidX and the experimental variant) now supports
  optionally passing a `NavEventNavigator`. This can be used instead of
  `navController` to navigate from outside the `NavHost` (e.g. for bottom navigation).
  The `NavHost` takes care of calling `NavigationSetup` for the passed navigator.
- **New**: `NavHost` from `navigation-experimental` now also supports passing a
  Modifier to it.
- **New**: The AndroidX `NavHost` will internally call `Navigation.setViewNavController`
  on the container `View`. This exists primarily for an easier migration from `Fragment`
  navigation to Compose navigation.

### Codegen

- **New**: The `NavHostActivity` codegen now supports passing a `Modifier` to `NavHost`.
- **New**: The `NavHostActivity` codegen automatically provides an `ImmutableSet` for
  destinations, deep link handlers and deep link prefixes.


## 0.20.0 *(2023-11-17)*

### Navigation

- **New** Add `Modifier` parameter to `NavHost` Composable.
- **New** Add `NavHost` overloaded function that accepts `NavRoute` instead of `NavRoot`
- **New** Add optional `transitionAnimations` parameter to `NavHost` Composable functions. Animations
can be overriden with `NavHostDefaults.transitionAnimations` or disabled with
`NavHostTransitionAnimations.noAnimations`. Default animations are the same as default animations
in AndroidX's `NavHost`.


## 0.19.0 *(2023-11-09)*

### Navigation

- **New**: Allow passing an already created `NavController` to `NavHost`. This allows controlling
  the navigation from outside the host, for example from a bottom bavigation or navigation drawer.
- **Fixed**: A crash that happened in `NavHost` on re-compositions.
- Improved how nav events are collected internally.

Thanks to @hoc081098 and @hoangchungk53qx1 for the contributions.

### Codegen

- **New**: Added an `Overlay` marker interface that can be added to routes to indicate
  to the code generation that this should use an `OverlayDestination` (`DialogDestination` for
  Fragments).
- **Breaking**: Removed `destinationType` in favor of the new interface.
- **Breaking**: Removed support for `@RendererDestination`.


## 0.18.0 *(2023-10-19)*

### Navigation

- **New**: `com.freeletics.khonshu.deeplinks` Gradle plugin that generates AndroidManifest
  `intent-filters` based on definitions from  a `toml` file
- **New**: The navigation-testing artifact now ships with
  `DeepLinkDefinitions.containsAllDeepLinks(Set<DeepLinkHandler>)` which checks that the
  deep links defined in the `toml` are match the given set of `DeepLinkHandlers`.
- **Breaking**: All deep link related classes have been moved to the
  `com.freeletics.khonshu.navigation.deeplinks` package.


## 0.17.0 *(2023-09-25)*

### Navigation

- `com.freeletics.khonshu:navigation` is now a multiplatform module with JVM and Android
  targets. The goal is not to support multiplatform navigation (for now at least), but to
  make it possible to reference `NavRoute` in Kotlin/JVM modules.


### Codegen

- **Breaking**: Renamed the Compose `@ComposeDestnation` annotation to `@NavDestination` and
  the Fragment `@ComposeDestination` to `@ComposeFragmentDestination`.
- **Breaking**: Removed `@ScopeTo` and `@ForScope` annotations. They have been replaced by
  Anvil's `@SingleIn` and `@ForScope` from `com.squareup.anvil:annotations-optional`.
- **New**: The code generation now supports KSP. The functionality is generally the same
- **New**: A new `@NavHostActivity` annotation was added and will generate an `Activity` and the
  related boilerplate. Check out [the docs](https://freeletics.github.io/khonshu/codegen/) for
  more details.
- It's now possible to use both `@NavDestination` and `@ComposeFragmentDestination` at the same
  time on the same composable. This allows transitioning from Fragments to Compose more easily.
- **Removed**: `@RendererScreen` and `@ComposeScreen` annotations. These allowed generating Fragments
  and Composables without relying on Khonshu navigation. We've only ever used this for Activities
  which now have a better solution. In the beginning we saw the codegen and navigation as 2
  separate things but now the codegen is more of an add-on, so we decided to remove the standalone
  mode (codegen without navigation) for codegen.
- The following 3 artifacts are now empty and will not be published anymore from the next release
  onwards. They have been merged into `com.freeletics.khonshu:codegen-runtime` and depend on it
  to make updating easier.
    - `com.freeletics.khonshu:codegen-scope`
    - `com.freeletics.khonshu:codegen-compose`
    - `com.freeletics.khonshu:codegen-fragment`
- `com.freeletics.khonshu:codegen-runtime` is now a multiplatform module with JVM and Android
  targets. The code generation still only supports Android but a few classes like `AppScope`
  can now be referenced from Kotlin/JVM modules.


## 0.16.1 *(2023-08-07)*

### Navigation

- Added `awaitNavigate` method to `NavigatorTurbine` that takes a lambda as parameter. It verifies that one nav event,
  containing all navigation actions from the lambda, is being received.


## 0.16.0 *(2023-08-01)*

- Now uses Kotlin 1.9.0 and Anvil 2.4.7.

### Navigation

- Added `navigate` method to `NavEventNavigator` that takes a lambda as parameter. That lambda can
  contain multiple navigation actions that will end up being bundled into one event that

### Codegen

- Added general `ForScope` annotation.
- It's now possible to use the `scope` of another screen using codegen as `parentScope`.
- The above replace the need to `@NavEntryComponent` and `@NavEntry` which have both been removed.
- To enable the parent scope mechanism the following the 3 types now need a `ForScope` qualifier:
    - `NavEventNavigator`
    - `SavedStateHandle`
    - anything provided into the `Set` of `Closeables`


## 0.15.0 *(2023-07-11)*

MAD has beed renamed to Khonshu and Whetstone is now just codegen.

|**Old**|**New**|
|-|-|
|`com.freeletics.mad:navigator-runtime`|`com.freeletics.khonshu:navigation`|
|`com.freeletics.mad:navigator-compose`|`com.freeletics.khonshu:navigation-compose`|
|`com.freeletics.mad:navigator-experimental`|`com.freeletics.khonshu:navigation-experimental`|
|`com.freeletics.mad:navigator-fragment`|`com.freeletics.khonshu:navigation-fragment`|
|`com.freeletics.mad:navigator-testing`|`com.freeletics.khonshu:navigation-testing`|
|`com.freeletics.mad:whetstone-compiler`|`com.freeletics.khonshu:codegen-compiler`|
|`com.freeletics.mad:whetstone-scope`|`com.freeletics.khonshu:codegen-scope`|
|`com.freeletics.mad:whetstone-runtime`|`com.freeletics.khonshu:codegen-runtime`|
|`com.freeletics.mad:whetstone-runtime-compose`|`com.freeletics.khonshu:codegen-compose`|
|`com.freeletics.mad:whetstone-runtime-fragment`|`com.freeletics.khonshu:codegen-fragment`|
|`com.freeletics.mad:whetstone-navigation`|Merged into `com.freeletics.khonshu:codegen-runtime`|
|`com.freeletics.mad:whetstone-navigation-compose`|Merged into `com.freeletics.khonshu:codegen-compose`|
|`com.freeletics.mad:whetstone-navigation-fragment`|Merged into `com.freeletics.khonshu:codegen-fragment`|
|`com.freeletics.mad:state-machine`|`com.freeletics.khonshu:state-machine`|
|`com.freeletics.mad:state-machine-testing`|`com.freeletics.khonshu:state-machine-testing`|
|`com.freeletics.mad:text-resource`|`com.freeletics.khonshu:text-resource`|

### Navigation

- Compose: The `Dialog` and `BottomSheet` destination types haven been replaced by a new `Overlay` destination.
  This new type generally behaves like the old ones except for not automically wrapping the given content in
  a `Dialog` or `ModalBottomSheetLayout` composable. This gives more flexibility and avoids some issues in the
  default implementations, like not being able to show multiple bottom sheet destinations on top of each other.
  It is recommended to use something like Material 3's `ModalBottomSheet` to display a bottom sheet.
- Compose: Removed dependency on Accompanist and usages of experimental APIs.

### Codegen

- Updated `DestinationType` for the navigation change above.
- Renamed `DestinationComponent` to `NavDestinationComponent`.


## 0.14.1 *(2023-05-26)*

### Whetstone

- Generated composables now you remember when retrieving objects from a component
  to avoid creating new instances on each recomposition.


## 0.14.0 *(2023-05-10)*

### Navigator

- Removed `saveCurrentRootState` from `navigate(NavRoot, ...)` method.
- Added `resetToRoot(NavRoot)` as a replacement.
- Added `com.freeletics.mad:navigator-experimental` which is an experimental alternative implementation of `navigator-compose`
  without a dependency on AndroidX navigation. The artifact is source and binary compatible with `navigator-compose` so it can
  be easily tested by added the following to `settings.gradle`:
```
gradle.beforeProject {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.freeletics.mad" && requested.name == "navigator-compose") {
                useTarget("com.freeletics.mad:navigator-experimental:${requested.version}")
            }
        }
    }
}
```

### StateMachine

- Added support for all [tier 1, 2 and 3 Kotlin/Native targets](https://kotlinlang.org/docs/native-target-support.html)


## 0.13.3 *(2023-03-24)*

### Whetstone

- Fix calls `asComposeState()` leading to a crash.


## 0.13.2 *(2023-03-23)*

### Whetstone

- Fix `whetstone-scope` artifact being published as `aar` instead of as `jar`.


## 0.13.1 *(2023-03-23)*

- Updated to Compose 1.4.0 and Accompanist 0.30.0.

### StateMachine

- Added `tvosSimulatorArm64` and `watchosX64` targets.


## 0.13.0 *(2023-03-14)*

### Navigator

- Added proguard rules to not obfuscate the names of `NavRoute`, `NavRoot` and `ActivityRoute`
  subclasses.

### Whetstone

- The `state` and `sendAction` parameters in annotated composables are now optional and only need
  to be specified if they are needed
- The `state` and `sendAction` parameters of annotated composables can now have different names
- Automatically discover `ViewRenderer.Factory` subclass nested inside the annotated `ViewRenderer`
  and removed the now obsolete `rendererFactory` parameter


## 0.12.0 *(2023-02-13)*

### Navigator

- Added [docs](https://freeletics.github.io/mad/navigator/deeplinks/) for deep link support.
- Internal changes and refactorings.

### Whetstone

- **BREAKING** simplified integration with the navigator library
  - removed the `@NavDestination` annotation
  - added `@ComposeDestination` and `@RendererDestination` as replacements
  - these new annotations also replace `@ComposeScreen`/`@ComposeFragment`/`@RendererFragment` so
    that always just one Whetstone annotation is needed per screen
  - `route` and `scope` are combined into a `route` parameter which removes the need to define scope
    classes, the `NavRoute` or `NavRoot` serves both purposes
  - the previous point means that the same `route` will also need to be used in any place that that
    requires scope markers like `@ScopeTo`, `@ContributesTo` or `@ContributesBinding`
- New `AppScope` scope marker class. This can be used as the scope marker for an app level component.
  All whetstone annotations use it as default value for `parentScope` and `destinationScope`, so those
  two don't need to be explicitly specified anymore after adopint `AppScope`.
- Fixed compiler warning produced by generated code.
- Generated Fragments for compose now use `DisposeOnViewTreeLifecycleDestroyed`.

### StateMachine

- added Kotlin/JS as target


## 0.11.0 *(2023-02-06)*

### Navigator

- new `navigator-testing` artifact to test `NavEventNavigator`, [see docs](https://freeletics.github.io/mad/navigator/testing/)
- `NavEvent` and other APIs that were marked as visible for testing are now marked as internal
- compose navigation APIs are not annotated with `ExperimentalMaterialNavigationApi` anymore

### StateMachine

- new `state-machine-testing` artifact, [see docs](https://freeletics.github.io/mad/helpers/#statemachine)

### Whetstone

- support for Anvil 2.4.4
- injecting into `Composable` functions now supports generic types
- added [docs](https://freeletics.github.io/mad/whetstone/closeable-support/) for `Closeable` support which allows cleaning up resources
- Internal: clean up and streamline code in the code generator


## 0.10.1 *(2023-01-13)*

### Navigator

- fix crash when `DeepLink` contains an `ActivityRoute`


## 0.10.0 *(2023-01-10)*

### Navigator
- added support for handling deep links, see `DeepLink` and `DeepLinkHandler` (more docs coming soon)
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

- Migrate to Gradle version catalog


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
