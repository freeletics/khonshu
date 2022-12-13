package com.freeletics.mad.navigator.test

import com.eygraber.uri.Uri
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.DeepLinkHandler.Prefix
import com.freeletics.mad.navigator.internal.matchesPattern
import com.google.common.truth.BooleanSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

public class DeepLinkHandlerSubject private constructor(
    failureMetadata: FailureMetadata,
    private val handler: DeepLinkHandler,
) : Subject(failureMetadata, handler) {

    public companion object {
        private val SUBJECT_FACTORY: Factory<DeepLinkHandlerSubject, DeepLinkHandler> =
            Factory { metadata, actual -> DeepLinkHandlerSubject(metadata, actual) }

        @JvmStatic
        public fun deepLinkHandler(): Factory<DeepLinkHandlerSubject, DeepLinkHandler> {
            return SUBJECT_FACTORY
        }

        @JvmStatic
        public fun assertThat(actual: DeepLinkHandler): DeepLinkHandlerSubject {
            return assertAbout(deepLinkHandler()).that(actual)
        }
    }

    public fun matchesPattern(string: String, prefixes: Set<Prefix> = emptySet()): BooleanSubject {
        return matchesPattern(Uri.parse(string), prefixes)
    }

    public fun matchesPattern(uri: Uri, prefixes: Set<Prefix> = emptySet()): BooleanSubject {
        return check("matchesPattern()").that(handler.matchesPattern(uri, prefixes))
    }
}
