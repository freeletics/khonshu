package com.freeletics.khonshu.text

import androidx.compose.runtime.Composable
import dev.drewhamilton.poko.ArrayContentBased
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A simple text representation that allows you to model text without the need
 * of an Android [Context].
 *
 * Use the various factory methods to create a new instance.
 * Use [format] with an Android context to get the proper formatted text.
 */
public actual sealed class TextResource {
    public actual companion object {
        /**
         * Create a `TextResource` for the given [String]. A common use case for this is
         * a string sent by the backend that is already localized and formatted.
         */
        public actual operator fun invoke(text: String): TextResource {
            return SimpleTextResource(text)
        }

        /**
         * Returns a `TextResource` for the given [String] or `null` if [text] was `null`.
         */
        public actual operator fun invoke(text: String?): TextResource? {
            if (text == null) {
                return null
            }
            return invoke(text)
        }

        /**
         * Join the given TextResources into one.
         */
        public actual fun join(resources: List<TextResource>, separator: String): TextResource {
            return CompositeTextResource(resources, separator)
        }
    }
}

/**
 * A `TextResource` that represents not yet loaded text. This can not be formatted into a String
 * and is meant as a marker to for example show a placeholder graphic.
 */
public actual data object LoadingTextResource : TextResource()

@Poko
internal actual class SimpleTextResource actual constructor(
    val text: String
) : TextResource()

@Poko
internal actual class ComposeStringResource actual constructor(
    val key: StringResource,
    @ArrayContentBased
    val args: Array<out Any>,
) : TextResource()

@Poko
internal actual class ComposePluralStringResource actual constructor(
    val key: PluralStringResource,
    val quantity: Int,
    @ArrayContentBased
    val args: Array<out Any>,
) : TextResource()

@Poko
internal actual class CompositeTextResource actual constructor(
    val elements: List<TextResource>,
    val separator: String,
) : TextResource()

