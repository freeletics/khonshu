package com.freeletics.khonshu.text

/**
 * An extension method to join TextResources mimicking the behavior of [joinToString] from
 * the Kotlin Standard Library.
 */
public fun <T> Iterable<T>.joinToTextResource(
    separator: String = ", ",
    transform: ((T) -> TextResource),
): TextResource {
    val textResources = map(transform)
    return TextResource.join(textResources, separator)
}

/**
 * Join this `TextResource` with the other one into a combined `TextResource.
 */
public operator fun TextResource.plus(other: TextResource): TextResource {
    return CompositeTextResource(listOf(this, other), separator = "")
}

/**
 * Join this `TextResource` with a `String`  into a combined `TextResource.
 */
public operator fun TextResource.plus(other: String): TextResource {
    return CompositeTextResource(listOf(this, TextResource(other)), separator = "")
}

public fun String.toTextResource(): TextResource = TextResource(this)
