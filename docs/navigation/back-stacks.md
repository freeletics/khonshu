# Multiple back stacks

The library provides support for AndroidX Navigation's multi back stack feature. This is most
commonly used in apps that use bottom navigation to separate the back stack of each tab. for example
going from tab 1 to tab 2 will save what was shown before and display the start screen of tab 2.
When selecting tab 1 again that previous state will be restored showing the screen the user
previously navigated to instead of showing the start destination of tab 1.

## NavRoot

To use this feature the start screen of each back stack (each tab) needs to use `NavRoot` instead
of `NavRoute` as parent type for their route:

```kotlin
@Parcelize
data object HomeTab : NavRoot

@Parcelize
data class SearchTab(
    val initialQuery: String = "",
) : NavRoot

@Parcelize
data object LibraryTab : NavRoot
```

Like for a regular `NavRoute` these are then passed to `ScreenDestination` to create destinations
for each screen.

## Setup

When using multiple back stacks the start route of the nav host should be a `NavRoot`.

## Navigation

Starting a new back stack is then as simple as calling `HostNavigator.switchBackStack(LibraryTab)`.
This will save the state of the current back stack and remove it and then create a new one for
`LibraryTab`. If `LibraryTab` was shown before it will automatically show the existing back stack instead of
creating a new one. If the back stack was `LibraryTab` -> `Subscreen1` before, calling `switchBackStack(LibraryTab)`
will result in `Subscreen1` being shown.

To reset a back stack or open it at its root without any restoration of previous state there is `showRoot`. In
the example above calling `HostNavigator.showRoot(LibraryTab)` would result in the back stack of `LibraryTab` being
cleared, so that `LibraryTab` is shown instead of `Subscreen1`.

Note that the nav hosts start route/destination will always remain on the back stack. If `HomeTab` was used as
start route, then calling `switchBackStack(LibraryTab)` would always result in a back stack of `HomeTab` ->
`LibraryTab` and pressing the back button would show home again. Back would only exit the app from `HomeTab`.

The last back stack related navigation method is `replaceAllBackStacks(root: NavRoot)`. This will clear all back
stacks including the start back stack and then create a new one with `root`. This method should be used very rarely.
The primary use case is to change the start destination, for example when the user logs in or out.
