# Deep links

The core class for deep links is `DeepLink`. It accepts various combinations of route classes
to build a custom back stack on top of the app's default destination. After constructing a deep link
call `buildIntent`, `buildTaskStack` or `buildPendingIntent` to obtain something that can be
launched. By default the resulting `Intent` will target the app's launcher `Activity`. To
open other Activities pass an `action` to `DeepLink`. The `Intent` for deep links with a custom
action will still be limited to the current app's package name so that deep links can't be
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

To bring everything together an `ImmutableSet<DeepLinkHandler>` needs to be passed to `rememberHostNavigator`:


```kotlin
setContent {
    rememberHostNavigator(
        startRoot0 = ...,
        destinations = ...,
        deepLinkHandlers = persistentSetOf(
            HomeDeepLinkHandler(),
            UserProfileDeepLinkHandler(),
        ),
        prefix = persistentSetOf(
            Prefix("https://example.com"),
            Prefix("exampleapp://example.com"),
        )
    )
}
```

The `deepLinkPrefixes` are what is combined with the given path patterns to form the full uri
pattern that is used to check whether a deep link matches. In case a `DeepLinkHandler` needs
specific prefixes that should only be used for that specific handler the `prefixes` method can be
overridden to specify a different set, in this case the global prefixes will be ignored.


### Gradle plugin

For the uri based deep link handling to work regular `intent-filter`s need to be created
in the `AndroidManifest`. To automate this process there is a Gradle plugin that reads
deep link definitions from a `toml` file and automatically generates the `Intent` filters
in the `AndroidManifest`. There is then a test helper that allows writing a unit test
verify that the `toml` file and the defined `DeepLinkHandler` classes handle the same
patterns and are not out of sync.

```toml
# define any global prefix like the deepLinkPrefixes passed to rememberHostNavigator
[[prefixes]]
scheme = "https"
host = "www.example.com"
autoVerified = true

[[prefixes]]
scheme = "example"
host = "example.com"
autoVerified = false

# it's possible to define reusable placeholders that are used in multiple of the deep links
[[placeholders]]
key = "locale"
exampleValues = ["en", "de", "it", "fr", "pt", "es", "tr", "ja", "ru", "pl"]

[[placeholders]]
key = "userId"
exampleValues = ["113753919"]

# the actual deep link definitions
# there should be one entry per DeepLinkHandler
[deepLinks]

[deepLinks.home]
patterns = ["{locale}/home/"]

[deepLinks.profile]
patterns = ["{locale}/profile/{user_id}"]

[deepLinks.workout]
patterns = ["{locale}/workouts/{slug}"]
# it's also possible to have placeholders and prefixes that only apply to a specific deep link
placeholders = [{ key = "slug", exampleValues = ["aphrodite"] }]
prefixes = [{ scheme = "https", host = "example2.com", autoVerified = true }]
```

Adding the plugin to the build file:

```kotlin
plugins {
    id("com.freeletics.khonshu.deeplinks") version "..."

    deepLinks {
        deepLinkDefinitionsFile = project.file("src/test/resources/deeplinks.toml")
    }
}
```

Defining which `Activity` should handle deep links:
```xml
<activity
    android:name="com.example.MainActivity">
    <!-- The following comment will be automatically replaced with the right
         intent-filters based on deeplinks.toml. DO NOT REMOVE IT -->
    <!-- DEEPLINK INTENT FILTERS -->
</activity>
```

In the tests it's then possible to write something like this
```kotlin
class DeepLinksTest {
    @Test
    fun deepLinks() {
        val tomlFile = DeepLinksTest::class.java.classLoader!!.getResourceAsStream("deeplinks.toml")
        val toml = tomlFile.readAllBytes().decodeToString()
        val definitions = DeepLinkDefinitions.decodeFromString(toml)

        definitions.containsAllDeepLinks(deepLinkHandlers, defaultPrefixes)
    }
}
```
