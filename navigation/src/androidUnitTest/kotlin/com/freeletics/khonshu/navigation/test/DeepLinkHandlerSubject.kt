package com.freeletics.khonshu.navigation.test

import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Prefix
import com.freeletics.khonshu.navigation.deeplinks.createDeepLinkIfMatching
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

internal class DeepLinkHandlerSubject private constructor(
    failureMetadata: FailureMetadata,
    private val handlers: Set<DeepLinkHandler>,
) : Subject(failureMetadata, handlers) {
    companion object {
        private val SUBJECT_FACTORY: Factory<DeepLinkHandlerSubject, Set<DeepLinkHandler>> =
            Factory { metadata, actual -> DeepLinkHandlerSubject(metadata, actual!!) }

        @JvmStatic
        fun deepLinkHandler(): Factory<DeepLinkHandlerSubject, Set<DeepLinkHandler>> {
            return SUBJECT_FACTORY
        }

        @JvmStatic
        fun assertThat(actual: DeepLinkHandler): DeepLinkHandlerSubject {
            return assertAbout(deepLinkHandler()).that(setOf(actual))
        }

        @JvmStatic
        fun assertThat(actual: Set<DeepLinkHandler>): DeepLinkHandlerSubject {
            return assertAbout(deepLinkHandler()).that(actual)
        }
    }

    fun createDeepLinkIfMatching(string: String, prefixes: Set<Prefix> = emptySet()): Subject {
        return createDeepLinkIfMatching(Uri.parse(string), prefixes)
    }

    fun createDeepLinkIfMatching(uri: Uri, prefixes: Set<Prefix> = emptySet()): Subject {
        return check("createDeepLinkIfMatching()").that(handlers.createDeepLinkIfMatching(uri, prefixes))
    }
}
