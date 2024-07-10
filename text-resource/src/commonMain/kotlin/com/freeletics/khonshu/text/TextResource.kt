package com.freeletics.khonshu.text

import dev.drewhamilton.poko.ArrayContentBased
import dev.drewhamilton.poko.Poko
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

/**
 * A simple text representation that allows you to model text without the need
 * of an Android [Context].
 *
 * Use the various factory methods to create a new instance.
 * Use [format] with an Android context to get the proper formatted text.
 */
public expect sealed class TextResource {

    public companion object {
        /**
         * Create a `TextResource` for the given [String]. A common use case for this is
         * a string sent by the backend that is already localized and formatted.
         */
        @JvmName("fromString")
        public operator fun invoke(text: String): TextResource

        /**
         * Returns a `TextResource` for the given [String] or `null` if [text] was `null`.
         */
        @JvmName("fromNullableString")
        public operator fun invoke(text: String?): TextResource?

        /**
         * Join the given TextResources into one.
         */
        @JvmStatic
        @JvmOverloads
        public fun join(resources: List<TextResource>, separator: String = ", "): TextResource
    }
}

/**
 * A `TextResource` that represents not yet loaded text. This can not be formatted into a String
 * and is meant as a marker to for example show a placeholder graphic.
 */
public expect object LoadingTextResource : TextResource

internal expect class SimpleTextResource(
    text: String
) : TextResource

internal expect class ComposeStringResource(
    key: StringResource,
    args: Array<out Any>,
) : TextResource

internal expect class ComposePluralStringResource(
    key: PluralStringResource,
    quantity: Int,
    args: Array<out Any>,
) : TextResource

internal expect class CompositeTextResource(
    elements: List<TextResource>,
    separator: String,
) : TextResource

