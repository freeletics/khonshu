package com.freeletics.khonshu.navigation.test

import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Prefix
import com.freeletics.khonshu.navigation.deeplinks.matchesPattern
import com.google.common.truth.BooleanSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

internal class DeepLinkHandlerSubject private constructor(
    failureMetadata: FailureMetadata,
    private val handler: DeepLinkHandler,
) : Subject(failureMetadata, handler) {

    companion object {
        private val SUBJECT_FACTORY: Factory<DeepLinkHandlerSubject, DeepLinkHandler> =
            Factory { metadata, actual -> DeepLinkHandlerSubject(metadata, actual!!) }

        @JvmStatic
        fun deepLinkHandler(): Factory<DeepLinkHandlerSubject, DeepLinkHandler> {
            return SUBJECT_FACTORY
        }

        @JvmStatic
        fun assertThat(actual: DeepLinkHandler): DeepLinkHandlerSubject {
            return assertAbout(deepLinkHandler()).that(actual)
        }
    }

    fun matchesPattern(string: String, prefixes: Set<Prefix> = emptySet()): BooleanSubject {
        return matchesPattern(Uri.parse(string), prefixes)
    }

    fun matchesPattern(uri: Uri, prefixes: Set<Prefix> = emptySet()): BooleanSubject {
        return check("matchesPattern()").that(handler.matchesPattern(uri, prefixes))
    }
}
