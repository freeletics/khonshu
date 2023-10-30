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

Starting a new back stack is then as simple as calling `NavEventNavigator.navigateToRoot(LibraryTab)`.
This will save the state of the current back stack and remove it and then create a new one for
`LibraryTab`. Note that the nav hosts start route/destination will always remain on the back stack.
If `HomeTab` was used as start route, then calling `navigateToRoot(LibraryTab)` would always result
in a back stack of `HomeTab` -> `LibraryTab` and pressing the back button would show home again.
Back would only exit the app from `HomeTab`.

If the current back stack should not be saved `saveCurrentRootState = false` can be passed as an
additional parameter to `navigateToRoot`.

It is also possible to restore a previously as part of the `navigateToRoot` operation by passing
`restoreRootState = true` to it. This will then restore a previously saved back stack if there was
one. The saved back stack is identified by the `NavRoot`. If `LibraryTab` was open before and the
user was viewing `LibrarySubscreenA` when navigating to a different tab then the next
`navigateToRoot(LibraryTab, restoreRootState = true)` call would result in `HomeTab` -> `LibraryTab`
-> `LibrarySubscreenA` as back stack. Without the `restoreRootState = true` or when explicitly
passing `false` the previously saved back stack of `LibraryTab` would be cleared and `LibraryTab`
would be visible instead of `LibrarySubscreenA`.
