Change Log
==========

Version 0.3.0-alpha27 *(2022-03-??)*
----------------------------

- Whetstone: Updated to Anvil 2.4.0-M1
- Whetstone: Use `@ContributesMultibinding` for the generated `NavEntryComponentGetter` instead of
generating an extra `@Module` class
- Whetstone: Use `@ContributesSubcomponent` instead of `@MergeSubcomponent` for the generated
`NavEntryComponent`. This delays the merging of the subcomponent to when the parent scope
is processed and allows to use `@NavEntryComponent` in modules other than the app module.
- Whetstone: Because of the above, the generated FactoryProvider interface is now nested in the
component and called `ParentComponent` like Anvil expects.


Version 0.3.0-alpha26 *(2022-02-14)*
----------------------------

- Navigator: remove `destinationId` from `NavRoute`/`NavRoot`, the route class itself is now the identifier
- Navigator: add `BaseRoute` interface to reduce API duplication between `NavRoute` and `NavRoot`
- Navigator: `NavEventNavigator` is now `open` instead of `abstract`
- Navigator: remove `navigateTo` with `popUpTo`, use `navigateBackTo` followed by a `navigateTo` instead
- Navigator: fix legacy compose `NavController` API


Version 0.3.0-alpha25 *(2022-02-11)*
----------------------------

- Whetstone: fix codegen for `@RootNavDestination`


Version 0.3.0-alpha24 *(2022-02-11)*
----------------------------

- Navigator: set arguments for the start destination to avoid a crash
- Whetstone: fix codegen for `@RootNavDestination`


Version 0.3.0-alpha23 *(2022-02-10)*
----------------------------

- Whetstone: make it possible to use `@RootNavDestination` on classes for renderer fragments


Version 0.3.0-alpha22 *(2022-02-10)*
----------------------------

- Whetstone: fix missing import in Fragment codegen


Version 0.3.0-alpha21 *(2022-02-09)*
----------------------------

- Whetstone: add `@NavDestination` annotation to enable navigator integration
- Whetstone: generated code will now directly use a `NavRoute` instead of a `Bundle`
- Navigator: removed `common`, `common-compose` and `common-fragment` artifacts
- Navigator: Make `NavRoute` and `NavRoot` are not parcelable anymore but keep
  the same behavior if an implementation of them is


Version 0.3.0-alpha20 *(2022-02-08)*
----------------------------

- Navigator: Make `NavRoute` and `NavRoot` parcelable, remove `getArguments`
- Navigator: Remove `NavController` parameter from navigation handler
- Navigator: fix bottom sheet layout missing for the compose `NavHost`
- Navigator: remove AndroidX Navigation from public API
- Navigator: clean up public API
- Whetstone: merge dialog annotations into main annotations


Version 0.3.0-alpha19 *(2022-02-03)*
----------------------------

- Navigator: Added `NavDestination` for both fragments and compose to support
  declaring destinations.
- Navigator: `NavHost` composable to support creating an AndroidX `NavHost` with
  a set of `NavDestination` objects
- Navigator: `NavHostFragment.setGraph` extension method to support creating
  setting a graph with a set of `NavDestination` objects


Version 0.3.0-alpha18 *(2022-02-02)*
----------------------------

- Whetstone: Fixed import in generated nav entry code.


Version 0.3.0-alpha17 *(2022-02-02)*
----------------------------

- Whetstone: Fixed package name mix-match.


Version 0.3.0-alpha16 *(2022-02-02)*
----------------------------

- Whetstone: Fixed that `whetstone-runtime-compose` and `whetstone-runtime-fragment` contain a class
  with the same fully qualified name and can not co exist in the same app.


Version 0.3.0-alpha15 *(2022-01-31)*
----------------------------

- Whetstone: Added `NavEntryComponents` as higher level API to retrieve a generated `NavEntryComponent`.
It abstracts away the `Map` of component getters.
- Whetstone: `NavEntryComponentGetter` is now marked as internal API, use `NavEntryComponents` instead
- Whetstone: `NavEntryComponentGetter` will now use the class as map key instead of the fully qualified
name as string. This resolves issues with proguard obfuscation. The only downside is that
`NavEntryComponents` needs to do a one time mapping from `Class` to `String` during construction.


Version 0.3.0-alpha14 *(2022-01-28)*
----------------------------

- AndroidX Navigation 2.4.0 and Fragment 1.4.1
- Navigator: add multi back stack support through `NavRoot`, `navigateToRoot` and `NavigateToRootEvent`


Version 0.3.0-alpha13 *(2022-01-24)*
----------------------------

- Navigator: add `handleNavEvent(NavEvent)` methods to NavEventNavigationHandler to allow easy extension
- fix duplicate manifest package names


Version 0.3.0-alpha12 *(2022-01-24)*
----------------------------

- fix `navigator-common-compose` and `navigator-common-fragment` artifact ids


Version 0.3.0-alpha11 *(2022-01-21)*
----------------------------

- Whetstone: split `runtime` into `runtime`, `runtime-compose` and `runtime-fragment`
- Navigator: split existing module into `common`, `common-compose` and `common-fragment`
- Navigator: new  `runtime` into `runtime`, `runtime-compose` and `runtime-fragment` modules which provide `Navigator` and `NavEventNavigationHandler` implementations


Version 0.3.0-alpha10 *(2021-10-20)*
----------------------------

- Whetstone: fix compilation issue in generated compose code


Version 0.3.0-alpha09 *(2021-10-20)*
----------------------------

- Whetstone: fix compilation issue in generated compose code


Version 0.3.0-alpha08 *(2021-10-20)*
----------------------------

- StateMachine: `state` is now a `Flow` instead of `StateFlow`
- Whetstone: adapt generated code for state machine change


Version 0.3.0-alpha07 *(2021-09-29)*
----------------------------

- Whetstone: add missing `@OptIn` in generated code


Version 0.3.0-alpha06 *(2021-09-29)*
----------------------------

- Kotlin 1.5.30 and Compose 1.1.0
- add Apple Silicon targets to `state-machine`
- change generated Whetstone code to be compatible with Kotlin 1.5.30


Version 0.2.1 *(2021-09-03)*
----------------------------

- update `state-machine` to Kotlin 1.5.30 and add Apple Silicon targets


Version 0.3.0-alpha05 *(2021-08-26)*
----------------------------

- Whetstone: add `ComposeDialogFragment` annotation
- Whetstone: call `setViewCompositionStrategy` from generated compose fragments


Version 0.3.0-alpha04 *(2021-08-19)*
----------------------------

- Whetstone: add `RendererDialogFragment` annotation
- Whetstone: support providing compose `ProvidedValue` into a set and automatically adding those to a `CompositionLocalProvider`


Version 0.3.0-alpha03 *(2021-08-16)*
----------------------------

- Whetstone: merge `RetainedComponent` annotation into the others
- Whetstone: support inset handling in generated `@ComposeFragment` classes
- Whetstone: generatec composables don't need `OnBackPressedDispatcher` in their ctor anymore
- Navigator: remove `CoroutineScope` parameter from `FragmentNavigationHandler`


Version 0.3.0-alpha02 *(2021-08-08)*
----------------------------

- Whetstone: added support to generate components tied to a `NavBackStackEntry` through `@NavEntryComponent`
- `LoadingTextResource.format` now returns `Nothing`


Version 0.3.0-alpha01 *(2021-08-05)*
----------------------------

- initial experimental release of the `whetstone-runtime` and `whetstone-compiler` artifacts
- initial experimental release of the `navigator` artifact


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
