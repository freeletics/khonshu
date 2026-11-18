# Migrating to DestinationNavigator2

`DestinationNavigator` is deprecated and will be removed in a future release.
Its two jobs are split into two types:

- `DestinationNavigator2` for back stack navigation and navigation results. It
  is available on all platforms.
- `PlatformNavigator` for activity and permission navigation. On Android it is
  an `ActivityNavigator`.

Follow the migration steps as indicated below:

- [Navigator without platform
  navigation](#navigator-without-platform-navigation)
- [Navigator with platform navigation and
  results](#navigator-with-platform-navigation-and-results)

## Behavior changes

- `navigateBack`, `navigateUp`, `navigateBackTo` and `navigate { ... }` are
  ignored while the destination is not the current one. With
  `DestinationNavigator` they always ran. **Code that relies on these calls
  while another destination is on top will stop working.** For example, closing
  the screen in reaction to a `Flow` or another async event while a dialog or
  bottom sheet is shown on top of it. The calls are not queued, so they do not
  run later either. Check such code before migrating.
- `registerForNavigationResult` with the destination's own route type no longer
  throws when the destination has already left the back stack. Previously this
  could cause a crash when a result was received after the destination left the
  back stack. Workarounds for that crash, like `by lazy`, `try`/`catch` or
  nullable result requests, can be removed.

## Navigator without platform navigation

This covers navigators that only use back stack navigation and navigation
results.

Before:

```kotlin
@SingleIn(ExampleRoute::class)
@ForScope(ExampleRoute::class)
@ContributesBinding(ExampleRoute::class)
class ExampleNavigator(
    hostNavigator: HostNavigator,
) : DestinationNavigator(hostNavigator) {
    val colorResult = registerForNavigationResult<ExampleRoute, Color>()

    fun showColorPicker() {
        navigateTo(ColorPickerRoute(colorResult.key))
    }

    fun close() {
        navigateBack()
    }
}
```

After:

```kotlin
@Inject
class ExampleNavigator(
    navigator: DestinationNavigator2,
) : DestinationNavigator2 by navigator {
    val colorResult = registerForNavigationResult<ExampleRoute, Color>()

    fun showColorPicker() {
        navigateTo(ColorPickerRoute(colorResult.key))
    }

    fun close() {
        navigateBack()
    }
}
```

Steps:

1. Replace the `HostNavigator` parameter with `DestinationNavigator2` and
   delegate to it with `by`.
2. Remove `@ContributesBinding` and `@ForScope`. A `DestinationNavigator`
   binding is no longer needed. Keep `@SingleIn(ExampleRoute::class)`
   only if the navigator holds state that must be shared.

Tests:

```kotlin
val hostNavigator = TestHostNavigator()
val navigator = ExampleNavigator(hostNavigator.destinationNavigator())

hostNavigator.test {
    navigator.close()
    awaitNavigateBack()
}
```

Before, tests passed the `TestHostNavigator` to the constructor and called
`navigator.test { }`. Now call `hostNavigator.test { }`.

## Navigator with platform navigation and results

This covers navigators that also navigate to activities or request permissions.
Move that part into a separate class that injects `PlatformNavigator`.

Before:

```kotlin
@SingleIn(ExampleRoute::class)
@ForScope(ExampleRoute::class)
@ContributesBinding(ExampleRoute::class)
class ExampleNavigator(
    hostNavigator: HostNavigator,
) : DestinationNavigator(hostNavigator) {
    val colorResult = registerForNavigationResult<ExampleRoute, Color>()

    val cameraPermissionRequest = registerForPermissionsResult()

    fun showColorPicker() {
        navigateTo(ColorPickerRoute(colorResult.key))
    }

    fun requestCameraPermission() {
        requestPermissions(cameraPermissionRequest, Manifest.permission.CAMERA)
    }

    fun openAppSettings() {
        navigateTo(AppSettingsRoute)
    }
}
```

After:

```kotlin
@SingleIn(ExampleRoute::class)
@Inject
class ExampleActivityNavigator(
    @ForScope(ExampleRoute::class)
    private val platformNavigator: PlatformNavigator,
) {
    val cameraPermissionRequest =
        platformNavigator.registerForPermissionsResult()

    fun requestCameraPermission() {
        platformNavigator.requestPermissions(
            cameraPermissionRequest,
            Manifest.permission.CAMERA,
        )
    }

    fun openAppSettings() {
        platformNavigator.navigateTo(AppSettingsRoute)
    }
}

@Inject
class ExampleNavigator(
    navigator: DestinationNavigator2,
    val activityNavigator: ExampleActivityNavigator,
) : DestinationNavigator2 by navigator {
    val colorResult = registerForNavigationResult<ExampleRoute, Color>()

    fun showColorPicker() {
        navigateTo(ColorPickerRoute(colorResult.key))
    }
}
```

Steps:

1. Do the steps from the previous section for `ExampleNavigator`.
2. Move `registerForActivityResult`, `registerForPermissionsResult` and every
   `navigateTo`, `navigateForResult` or `requestPermissions` call for an
   activity route or permission into a new class that injects
   `@ForScope(ExampleRoute::class) PlatformNavigator`.
3. Annotate the new class with `@SingleIn(ExampleRoute::class)`. It must be
   scoped to the destination to avoid registering its requests again when the
   UI is recreated.
4. Inject the new class directly into `ExampleNavigator` or the state machine,
   as shown above. It must be created before the screen is shown, because it
   registers its requests when it is created. Do not inject it as `Lazy` or
   `Provider`, which delays creating it.

The state machine can keep using `ExampleNavigator` only. It calls
`navigator.activityNavigator.requestCameraPermission()`, or `ExampleNavigator`
can forward the call with its own function.

Tests:

```kotlin
val hostNavigator = TestHostNavigator()
val platformNavigator = hostNavigator.platformNavigator()
val navigator = ExampleNavigator(
    hostNavigator.destinationNavigator(),
    ExampleActivityNavigator(platformNavigator),
)

platformNavigator.test {
    navigator.activityNavigator.requestCameraPermission()
    awaitRequestPermissions(
        navigator.activityNavigator.cameraPermissionRequest,
        Manifest.permission.CAMERA,
    )
}

hostNavigator.test {
    navigator.showColorPicker()
    awaitNavigateTo(ColorPickerRoute(navigator.colorResult.key))
}
```

Use `platformNavigator.test { }` for activity and permission events and
`hostNavigator.test { }` for back stack events. `hostNavigator.test { }` does
not see activity or permission events. Results are sent the same way as before,
for example
`navigator.activityNavigator.cameraPermissionRequest.sendResult(...)`.
