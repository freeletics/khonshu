# Results

`NavEventNavigator` provides some additional functionality around result handing. This can be used
to obtain results from other `Activities`, for permission requests or from other destinations.

## Activity results

External SDKs and the framework often provide an `Activity` that is supposed to be used with
`startActivityForResult`. AndroidX already introduced `ActivityResultContract` to simplify handling
this and `NavEventNavigator` uses them to also enable starting them from outside the UI layer
and receiving results there.

To use the API `registerForActivityResult` needs to be called with an instance of the wanted
`ActivityResultContract`. This needs to happen before `handleNavigation`/`NavigationSetup`
is called for the navigator, so it is recommended to do this during the construction of the
navigator. The method returns an `ActivityResultRequest` object that can be then used for two
things. It can be passed to `navigateForResult(request)` to launch the contract. It also has a
`results` property that returns a `Flow<O>`, where `O` is the contract's output type, to make it
possible to receive the returned results.

This is an example navigator that allow navigating to the camera or the system file picker to
take or pick an image:
```kotlin
class MyNavigator : NavEventNavigator() {
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
permission requests. `NavEventNavigator` provides a slightly higher level API for this.

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

## Destination results

The last result API of the library is for returning hand handling results from screens inside
the app that also use `NavEventNavigator`.

The setup is similar to Activity results and permissions. The
`registerForNavigationResult<Route, Result>` needs to be called during construction of the navigator
or shortly after. `Route` in this case should be the `NavRoute` class for the curren screen. While
`Result` is the type of the expected result which can be any `Parcelable` class. The register method
will like the others than return a request object, which has `results` property that returns a
`Flow<Result>` to collect the results.

The navigation to the screen from which the result should be returned is a regular call to
`navigateTo`. However the `NavRoute` class for that target destination should have
`NavigationResultRequest.Key<Result>` as a parameter. An instance of such a `Key` can be obtained
from the `key` property of the request object.

The target screen can then simply call `devliverNavigationResult(route.key, result)` on its own
navigator to send the result and afterwards remove itself from the back stack with `navigateBack`.

The navigator for a hypothetical `ScreenA` that wants to receive a result from `ScreenB` would look
like this
```kotlin
data class MessageResult(val message: String): Parcelable

class ScreenANavigator : NavEventNavigator() {
    // use request.results somewhere to handle the results that ScreenB delivers
    val request = registerForNavigationResult<ScreenARoute, MessageResult>

    fun navigateToScreenB() {
        // if needed ScreenBRoute could also have additional parameters
        navigateTo(ScreenBRoute(request.key))
    }
}
```

And then the navigator for `ScreenB`
```kotlin

class ScreenANavigator(
    val route: ScreenBRoute
) : NavEventNavigator() {

    fun deliverMessage(message: String) {
        deliverNavigationResult(route.key, MessageResult(message))
    }
}
```
