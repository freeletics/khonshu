package com.freeletics.mad.navigator.test

import com.eygraber.uri.Uri
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.internal.extractPathParameters
import com.google.common.truth.FailureMetadata
import com.google.common.truth.MapSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

public class DeepLinkHandlerPatternSubject private constructor(
    failureMetadata: FailureMetadata,
    private val handler: DeepLinkHandler.Pattern,
) : Subject(failureMetadata, handler) {

    public companion object {
        private val SUBJECT_FACTORY: Factory<DeepLinkHandlerPatternSubject, DeepLinkHandler.Pattern> =
            Factory { metadata, actual -> DeepLinkHandlerPatternSubject(metadata, actual) }

        @JvmStatic
        public fun deepLinkHandlerPattern(): Factory<DeepLinkHandlerPatternSubject, DeepLinkHandler.Pattern> {
            return SUBJECT_FACTORY
        }

        @JvmStatic
        public fun assertThat(actual: DeepLinkHandler.Pattern): DeepLinkHandlerPatternSubject {
            return assertAbout(deepLinkHandlerPattern()).that(actual)
        }
    }

    public fun extractPathParameters(uri: String): MapSubject {
        return extractPathParameters(Uri.parse(uri))
    }

    public fun extractPathParameters(uri: Uri): MapSubject {
        return check("extractPathParameters()").that(handler.extractPathParameters(uri))
    }
}