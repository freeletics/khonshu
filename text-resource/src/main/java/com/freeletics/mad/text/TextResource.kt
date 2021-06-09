package com.freeletics.mad.text

import android.content.Context
import android.os.Parcelable
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java.util.Arrays
import java.util.IllegalFormatException
import kotlinx.parcelize.RawValue
import kotlinx.parcelize.Parcelize

/**
 * A simple text representation that allows you to model text without the need
 * of an Android [Context].
 *
 * Use the various factory methods to create a new instance.
 * Use [format] with an Android context to get the proper formatted text.
 */
sealed class TextResource : Parcelable {

    /**
     * Returns the formatted [String] represented by this `TextResource`.
     */
    abstract fun format(context: Context): String

    companion object {
        /**
         * Create a `TextResource` for the given [String]. A common use case for this is
         * a string sent by the backend that is already localized and formatted.
         */
        @JvmName("fromString")
        operator fun invoke(text: String): TextResource {
            return SimpleTextResource(text)
        }

        /**
         * Returns a `TextResource` for the given [String] or `null` if [text] was `null`.
         */
        @JvmName("fromNullableString")
        operator fun invoke(text: String?): TextResource? {
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
        operator fun invoke(
            @StringRes id: Int,
            vararg args: Any
        ): TextResource {
            return StringTextResource(id, args)
        }

        /**
         * Create a `TextResource` for the given Android plurals resource id and the given
         * [quantity]. The [args] will be when formatting the string to replace any placeholders.
         */
        @JvmName("fromPluralResource")
        operator fun invoke(
            @PluralsRes id: Int,
            quantity: Int,
            vararg args: Array<Any>
        ): TextResource {
            return PluralTextResource(id, quantity, args)
        }

        /**
         * Join the given TextResources into one.
         */
        @JvmStatic
        @JvmOverloads
        fun join(
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
object LoadingTextResource : TextResource() {
    override fun format(context: Context): String {
        throw UnsupportedOperationException("LoadingTextResource can not be formatted.")
    }
}

@Parcelize
internal data class SimpleTextResource(val text: String) : TextResource() {
    override fun format(context: Context): String {
        return text
    }
}

@Parcelize
internal data class StringTextResource(
    @StringRes val id: Int,
    val args: @RawValue Array<out Any>
) : TextResource() {

    override fun format(context: Context): String = tryFormat(context) {
        return context.resources.getString(id, *args.formatRecursively(context))
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

@Parcelize
internal data class PluralTextResource(
    @PluralsRes val id: Int,
    val quantity: Int,
    val args: @RawValue Array<out Any>
) : TextResource() {

    override fun format(context: Context): String = tryFormat(context) {
        return context.resources.getQuantityString(id, quantity, *args.formatRecursively(context))
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

@Parcelize
internal data class CompositeTextResource(
    val elements: List<TextResource>,
    val separator: String
) : TextResource() {

    override fun format(context: Context): String {
        return elements.joinToString(separator = separator) { it.format(context) }
    }
}

private fun Array<out Any>.formatRecursively(context: Context): Array<out Any> {
    return map {
        if (it is TextResource) it.format(context) else it
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
