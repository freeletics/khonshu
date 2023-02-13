# Deep links

The core class for deep links is `DeepLink`. It accepts various combinations of route classes
to build a custom back stack on top of the app's default destination. After constructing a deep link
call `buildIntent`, `buildTaskStack` or `buildPendingIntent` to obtain something that can be 
launched. By default the resulting `Intent` will target the app's launcher `Activity`. To 
open other's pass an `action` to `DeepLink`. The `Intent` for deep links with a custom 
action will still be limited to the current app's packageName so that deep links can't be
hijacked by other apps.

## Uri based deep links

`DeepLink` provides facilities to launch deep links for the current app, but on it's own is 
not enough to handle deep links from the outside. For that `DeepLinkHandler` is needed. When 
implementing such a handler a `patterns` property and the `deepLink` method need to be overridden.

`patterns` is a `Set` of url path patterns like `users/search` for handling 
`https://example.com/users/search` or `home` for handling `https://example.com/home`. A pattern
should never start with a leading `/` and does not include query parameters or url fragments. It is
possible to specify placeholders in the pattern by using curly braces, for example 
`users/{id}/profile` would work for `https://example.com/users/123/profile` and 
`https://example.com/users/uuid/profile`. Multiple placeholders are supported as well.

The `deepLink` method is called by the library if a pattern of the handler matched an `Uri` from
`Intent.data` and is then supposed to return a `DeepLink`. To build a deep link based on parameters
from the uri the method gets a `Map<String, String>` that contains all extracted path placeholders
(for the `users/{id}/profile` example it would contain an `id` key) and a second map with any
query parameter.

```kotlin
class UserProfileDeepLinkHandler : DeepLinkHandler {

    override val patterns = setOf(Pattern("users/{id}/profile"))

    override fun deepLink(pathParameters: Map<String, String>, queryParameters: Map<String, String>): DeepLink {
        return DeepLink(
            routes = listOf(
                UserListRoute,
                UserProfileRoute(pathParameters["id"]),
            )
        )
    }
}
```

To bring everything together a `Set<DeepLinkHandler>` needs to be passed to the nav host configuration:

=== "Compose"

    ```kotlin
    setContent {
        NavHost(
            startRoute = ..., 
            destinations = ...,
            deepLinkHandlers = setOf(
                HomeDeepLinkHandler(),
                UserProfileDeepLinkHandler(),
            ),
            prefix = setOf(
                Prefix("https://example.com"),
                Prefix("exampleapp://example.com"),
            )
        )
    }
    ```

=== "Fragment"

    ```kotlin
    navHostFragment.setGraph(
        startRoute = ..., 
        destinations = ...,
        deepLinkHandlers = setOf(
            HomeDeepLinkHandler(),
            UserProfileDeepLinkHandler(),
        ),
        deepLinkPrefixes = setOf(
            Prefix("https://example.com"),
            Prefix("exampleapp://example.com"),
        )
    )
    ```

The `deepLinkPrefixes` are what is combined with the given path patterns to form the full uri 
pattern that is used to check whether a deep link matches. In case a `DeepLinkHandler` needs
specific prefixes that should only be used for that specific handler the `prefixes` method can be 
overridden to specify a different set, in this case the global prefixes will be ignored.

For the uri based deep link handling to work regular `intent-filter`s need to be created
in the `AndroidManifest`.
