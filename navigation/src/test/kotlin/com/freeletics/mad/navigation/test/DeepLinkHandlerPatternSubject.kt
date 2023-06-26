package com.freeletics.mad.navigation.test

import com.eygraber.uri.Uri
import com.freeletics.mad.navigation.DeepLinkHandler
import com.freeletics.mad.navigation.internal.extractPathParameters
import com.google.common.truth.FailureMetadata
import com.google.common.truth.MapSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

internal class DeepLinkHandlerPatternSubject private constructor(
    failureMetadata: FailureMetadata,
    private val handler: DeepLinkHandler.Pattern,
) : Subject(failureMetadata, handler) {

    companion object {
        private val SUBJECT_FACTORY: Factory<DeepLinkHandlerPatternSubject, DeepLinkHandler.Pattern> =
            Factory { metadata, actual -> DeepLinkHandlerPatternSubject(metadata, actual!!) }

        @JvmStatic
        fun deepLinkHandlerPattern(): Factory<DeepLinkHandlerPatternSubject, DeepLinkHandler.Pattern> {
            return SUBJECT_FACTORY
        }

        @JvmStatic
        fun assertThat(actual: DeepLinkHandler.Pattern): DeepLinkHandlerPatternSubject {
            return assertAbout(deepLinkHandlerPattern()).that(actual)
        }
    }

    fun extractPathParameters(uri: String): MapSubject {
        return extractPathParameters(Uri.parse(uri))
    }

    fun extractPathParameters(uri: Uri): MapSubject {
        return check("extractPathParameters()").that(handler.extractPathParameters(uri))
    }
}
