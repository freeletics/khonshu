Change Log
==========

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

- Kotlin 1.5.30 and Compose 1.0.4
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
