package com.freeletics.sample.main

import com.freeletics.khonshu.codegen.ActivityScope
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides

@ContributesTo(ActivityScope::class)
@BindingContainer
object MainGraph {
    /**
     * Default prefixes for all [DeepLinkHandler]. These match the
     * prefixes in `app/simple-android/src/test/resources/deeplinks.toml`.
     */
    @Provides
    @ElementsIntoSet
    fun provideDeepLinkPrefixes(): Set<DeepLinkHandler.Prefix> = setOf(
        DeepLinkHandler.Prefix("https://www.sample.com"),
        DeepLinkHandler.Prefix("khonshu://sample.com"),
    )
}
