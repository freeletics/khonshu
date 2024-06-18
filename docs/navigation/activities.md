# Navigating to Activities and other apps

It is also possible navigate to an `Activity` both inside and outside of the app. Similar to other
screens it is required to define a route for Activities. Instead of `NavRoute`
the route class needs to extend either `InternalActivtyRoute` for `Activity` instances in the
current app or `ExternalActivityRoute` if it is part of a different app.

## Setup

Navigating to an `Activity` is not directly possible with `HostNavigator`. Instead an
`ActivityNavigator` is needed. This can just be a simple class that extends `ActivityNavigator`
or `DestinationNavigator`. The latter combines the functionality of `HostNavigator` and
`ActivityNavigator` so that one class can be used for all navigation related actions of a
screen.

From the `Composable` it is then required to call `NavigationSetup(navigator)` which
will do the required setup to make the `Activity` related navigation actions work.

It's recommended to make `ActivityNavigator`/`DestinationNavigator` classes specific to
one screen where they are needed instead of having a global instance.

## Internal Activities

This is example shows the route for a `SettingsActivity`:
```kotlin
@Parcelize
data class SettingsActivityRoute(
    val id: String,
) : InternalActivityRoute() {
    override fun buildIntent() = Intent("com.example.SETTINGS")
}
```

An instance of the route will automatically be added to the `Intent` which is why it's not needed to
manually put the `id` into it. `SettingsActivity` can then obtain an instance of the route that was used
to navigate to it by calling `Activity.getRoute()` or `Activity.requireRoute()` and access the
argument through it.

To avoid having to reference a specific `Activity` `class` it's possible to use customt `Intent` actions.
Khonshu will automatically make sure that the created `Intent` is always routed to the current app
and can not be hijacked.

## Other apps

Activities in other apps can be targeted with `ExternalActivityRoute`. The only differences
to `InternalActivityRoute` is that the route itself won't be added to the `Intent` and that
the `Intent` isn't limited to the current app.

A very simple route would just be an object without any parameters:

```kotlin
@Parcelize
data object SystemLocationSettings : ExternalActivityRoute {
    override fun buildIntent() = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
}
```

For opening an external `Activity` that requires arguments, those arguments can be
part of the route and then be used in `buildIntent` to fill in the extras:

```kotlin
@Parcelize
class ShareRoute(
    private val title: String,
    private val message: String
) : ExternalActivityRoute {
    override fun fillInIntent() = Intent(Intent.ACTION_CHOOSER)
        .putExtra(Intent.EXTRA_TITLE, title)
        .putExtra(Intent.EXTRA_INTENT, Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        })
}

// route with a data uri extra
@Parcelize
class BrowserRoute(
    private val uri: Uri,
) : ExternalActivityRoute {
    // the returned Intent is filled into the Intent of the destination by calling
    // destinationIntent.fillIn(fillInIntent())
    override fun fillInIntent() = Intent(Intent.ACTION_VIEW).setData(uri)
}
```

## Activity results

External SDKs and the framework often provide an `Activity` that is supposed to be used with
`startActivityForResult`. AndroidX already introduced `ActivityResultContract` to simplify handling
this and `ActivityNavigator` uses them to also enable starting them from outside the UI layer
and receiving results there.

To use the API `registerForActivityResult` needs to be called with an instance of the wanted
`ActivityResultContract`. This needs to happen before `NavigationSetup` is called for the navigator,
so it is recommended to do this during the construction of the navigator. The method returns an
`ActivityResultRequest` object that can be then used for two things. It can be passed to
`navigateForResult(request)` to launch the contract. It also has a `results` property that returns
a `Flow<O>`, where `O` is the contract's output type, to make it
possible to receive the returned results.

This is an example navigator that allow navigating to the camera or the system file picker to
take or pick an image:
```kotlin
class MyNavigator : ActivityNavigator() {
    val cameraImageRequest = registerForActivityResult(ActivityResultContracts.TakePicture())
    val galleryImageRequest = registerForActivityResult(ActivityResultContracts.GetContent())


    fun takePicture(uri: Uri) {
        // the uri here is the parameter that the TakePicture contract expects
        navigateForResult(cameraImageRequest, uri)
    }

    fun pickPicture() {
        navigateForResult(galleryImageRequest, "image/*")
    }
}
```

In the example above `cameraImageRequest.results` returns a `Flow<Boolean>` and
`galleryImageRequest.results` a `Flow<Uri?>` which can both be collected to handle the results.

## Requesting permissions

The Activity result APIs can already be used with `ActivityResultContracts.RequestPermission` or
`ActivityResultContracts.RequestMultiplePermissions` to also handle requesting Android runtime
permission requests. `HostNavigator` provides a slightly higher level API for this.

To use this call `registerForPermissionResult`, which should be done during the construction
of the navigator or shortly after. This can then be passed to `requestPermissions` with one or
more permission to request to launch the request. Results can be collected through the
`Flow<Map<String, PermissionResult>>` that is returned by the `results` property of request.

The `PermissionResult` is the main advantage of using this API instead for the Activity result APIs.
Instead of being a simple `Boolean` for granted/denied it is a sealed class with `Granted` and
`Denied` where `Denied` has an extra `shouldShowRationale` property. After it receives the result
from the contract, the library will internally use `Activity.shouldShowRequestPermissionRationale(permission)`
to make it possible to handle denials more granularly without needing a reference to an `Activity`.

An example usage can look like this:
```kotlin
class MyNavigator : NavEventNavigator() {
    // use permissionRequest.results somewhere to handle results
    val permissionRequest = registerForPermissionsResult()

    fun requestContactsPermission(uri: Uri) {
        requestPermissions(permissionRequest, Manifest.permission.CAMERA)
    }

    fun requestLocationPermissions() {
        requestPermissions(
            permissionRequest,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}
```
