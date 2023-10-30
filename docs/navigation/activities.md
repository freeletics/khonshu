# Navigating to Activities and other apps

It is also possible navigate to an `Activity` both inside and outside of the app. Similar to other
screens it is required to define a route and a destination for Activities. Instead of `NavRoute`
the route class needs to extend either `InternalActivtyRoute` for `Activity` instances in the
current app or `ExternalActivityRoute` if it is part of a different app. In both cases the
destination is then declared using the `ActivityDestination` function.

## Internal Activities

This is example shows the route and destination for a `SettingsActivity`:
```kotlin
@Parcelize
data class SettingsActivityRoute(
    val id: String,
) : InternalActivityRoute()

val extraActivityDestination: NavDestination = ActivityDestination<SettingsRoute>(
    intent = Intent(context, SettingsActivity::class)
)
```

`SettingsActivity` can obtain an instance of the route that was used to navigate to it by calling
`Activity.getRoute()` or `Activity.requireRoute()`.

## Other apps

Activities in other apps can be targeted with `ExternalActivityRoute`. This provides an additional
`fillInIntent` method that can optionally be overridden to dynamically add parameters to the
started `Intent`.

A very simple route would just be an object and the `Intent` is completely built as part of the
destination:

```kotlin
@Parcelize
data object PlayStoreRoute : ExternalActivityRoute

val playStoreDestination: NavDestination = ActivityDestination<PlayStoreRoute>(
    intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
)
```

A share `Intent` usually has changing messages so defining that statically when creating the
destination won't work. The dynamic parameters can be passed to the route and then added
to the `fillInIntent`. The `Intent` of the destination will then be combined internally
with a call to [`Intent.fillIn`](https://developer.android.com/reference/android/content/Intent#fillIn(android.content.Intent,%20int))

```kotlin
@Parcelize
class ShareRoute(
    private val title: String,
    private val message: String
) : ExternalActivityRoute {
    override fun fillInIntent() = Intent()
        .putExtra(Intent.EXTRA_TITLE, title)
        .putExtra(Intent.EXTRA_INTENT, Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        })
}

val shareDestination: NavDestination = ActivityDestination<ShareRoute>(
    // basic intent that is extended with the Intent above
    intent = Intent(Intent.ACTION_CHOOSER)
)
```

Another example which would open a browser with a given `Uri`:

```kotlin
// route with a data uri extra
@Parcelize
class BrowserRoute(
    uri: Uri,
) : ExternalActivityRoute {
    // the returned Intent is filled into the Intent of the destination by calling
    // destinationIntent.fillIn(fillInIntent())
    override fun fillInIntent() = Intent().setData(uri)
}

val browserDestination: NavDestination = ActivityDestination<BrowserRoute>(
    // basic intent that is extended with the Intent above
    intent = Intent(Intent.ACTION_VIEW)
)
```

All shown approaches can be combined where for example some extras are statically added to the
`Intent` when the destination is created and some others are dynamically provided through
`fillInIntent`.
