package com.freeletics.khonshu.text

import android.content.Context
import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.drewhamilton.poko.Poko
import java.util.IllegalFormatException
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * A simple text representation that allows you to model text without the need
 * of an Android [Context].
 *
 * Use the various factory methods to create a new instance.
 * Use [format] with an Android context to get the proper formatted text.
 */
public sealed class TextResource : Parcelable {

    /**
     * Returns the formatted [String] represented by this `TextResource`.
     */
    public abstract fun format(context: Context): String

    @Composable
    @ReadOnlyComposable
    public abstract fun format(): String

    public companion object {
        /**
         * Create a `TextResource` for the given [String]. A common use case for this is
         * a string sent by the backend that is already localized and formatted.
         */
        @JvmName("fromString")
        public operator fun invoke(text: String): TextResource {
            return SimpleTextResource(text)
        }

        /**
         * Returns a `TextResource` for the given [String] or `null` if [text] was `null`.
         */
        @JvmName("fromNullableString")
        public operator fun invoke(text: String?): TextResource? {
            if (text == null) {
                return null
            }
            return invoke(text)
        }

        /**
         * Create a `TextResource` for the given Android string resource id. The [args] will be
         * when formatting the string to replace any placeholders.
         */
        @JvmName("fromStringResource")
        public operator fun invoke(
            @StringRes id: Int,
            vararg args: Any,
        ): TextResource {
            return StringTextResource(id, args)
        }

        /**
         * Create a `TextResource` for the given Android plurals resource id and the given
         * [quantity]. The [args] will be when formatting the string to replace any placeholders.
         */
        public fun createWithQuantity(
            @PluralsRes id: Int,
            quantity: Int,
            vararg args: Any,
        ): TextResource {
            return PluralTextResource(id, quantity, args)
        }

        /**
         * Join the given TextResources into one.
         */
        @JvmStatic
        @JvmOverloads
        public fun join(
            resources: List<TextResource>,
            separator: String = ", ",
        ): TextResource = CompositeTextResource(resources, separator)
    }
}

/**
 * A `TextResource` that represents not yet loaded text. This can not be formatted into a String
 * and is meant as a marker to for example show a placeholder graphic.
 */
@Parcelize
public data object LoadingTextResource : TextResource() {
    override fun format(context: Context): Nothing {
        throw UnsupportedOperationException("LoadingTextResource can not be formatted.")
    }

    @Composable
    @ReadOnlyComposable
    override fun format(): Nothing {
        throw UnsupportedOperationException("LoadingTextResource can not be formatted.")
    }
}

@Poko
@Parcelize
internal class SimpleTextResource(val text: String) : TextResource() {
    override fun format(context: Context): String {
        return text
    }

    @Composable
    @ReadOnlyComposable
    override fun format(): String {
        return text
    }
}

@Poko
@Parcelize
internal class StringTextResource(
    @StringRes val id: Int,
    val args: @RawValue Array<out Any>,
) : TextResource() {

    override fun format(context: Context): String = tryFormat(context) {
        return context.resources.getString(id, *args.formatRecursively(context))
    }

    @Composable
    @ReadOnlyComposable
    override fun format(): String {
        return stringResource(id, *args.formatRecursively())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringTextResource

        if (id != other.id) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + args.contentHashCode()
        return result
    }
}

@Poko
@Parcelize
internal class PluralTextResource(
    @PluralsRes val id: Int,
    val quantity: Int,
    val args: @RawValue Array<out Any>,
) : TextResource() {

    override fun format(context: Context): String = tryFormat(context) {
        return context.resources.getQuantityString(id, quantity, *args.formatRecursively(context))
    }

    @Composable
    @ReadOnlyComposable
    override fun format(): String {
        return LocalContext.current.resources.getQuantityString(id, quantity, *args.formatRecursively())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PluralTextResource

        if (id != other.id) return false
        if (quantity != other.quantity) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + quantity
        result = 31 * result + args.contentHashCode()
        return result
    }
}

@Poko
@Parcelize
internal class CompositeTextResource(
    val elements: List<TextResource>,
    val separator: String,
) : TextResource() {

    override fun format(context: Context): String {
        return elements.joinToString(separator = separator) { it.format(context) }
    }

    @Composable
    @ReadOnlyComposable
    override fun format(): String {
        val builder = StringBuilder()
        for ((count, element) in elements.withIndex()) {
            if (count + 1 > 1) builder.append(separator)
            builder.append(element.format())
        }
        return builder.toString()
    }
}

private fun Array<out Any>.formatRecursively(context: Context): Array<out Any> {
    return map {
        if (it is TextResource) it.format(context) else it
    }.toTypedArray()
}

@Composable
@ReadOnlyComposable
private fun Array<out Any>.formatRecursively(): Array<out Any> {
    return map {
        if (it is TextResource) it.format() else it
    }.toTypedArray()
}

private inline fun TextResource.tryFormat(context: Context, format: () -> String): String = try {
    format()
} catch (e: IllegalFormatException) {
    // wrap the original exception to get some better debug info
    throw TextResourceFormatException(this, context, e)
}

private class TextResourceFormatException(
    private val textResource: TextResource,
    private val context: Context,
    cause: Throwable,
) : Exception(cause) {

    override val message: String
        get() = "Failed to format TextResource! See exception cause for the original exception.\n\t" +
            "TextResource was: $textResource \n\t" +
            getAdditionalInfo()

    override fun fillInStackTrace(): Throwable = this

    private fun getAdditionalInfo(): String {
        val resNameInfo = { id: Int -> "Resource map: $id -> ${context.resources.getResourceEntryName(id)}" }

        return when (textResource) {
            is StringTextResource -> resNameInfo(textResource.id)
            is PluralTextResource -> resNameInfo(textResource.id)
            else -> ""
        }
    }
}
