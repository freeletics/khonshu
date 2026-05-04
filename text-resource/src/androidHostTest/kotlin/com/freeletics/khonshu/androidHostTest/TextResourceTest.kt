package com.freeletics.khonshu.text

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

class TextResourceTest {
    val textResource = TextResource.join(
        listOf(
            TextResource(""),
            TextResource(0, "a", 0, 0L, 0.0f, 0.0, TextResource("")),
            TextResource.createWithQuantity(0, 1, "a", 0, 0L, 0.0f, 0.0, TextResource("")),
        ),
    )

    val json = "{\"type\":\"com.freeletics.khonshu.text.CompositeTextResource\",\"elements\":[" +
        "{\"type\":\"com.freeletics.khonshu.text.SimpleTextResource\",\"text\":\"\"}," +
        "{\"type\":\"com.freeletics.khonshu.text.StringTextResource\",\"id\":0,\"args\":[" +
        "{\"type\":\"com.freeletics.khonshu.text.StringArg\",\"value\":\"a\"}," +
        "{\"type\":\"com.freeletics.khonshu.text.IntArg\",\"value\":0}," +
        "{\"type\":\"com.freeletics.khonshu.text.LongArg\",\"value\":0}," +
        "{\"type\":\"com.freeletics.khonshu.text.FloatArg\",\"value\":0.0}," +
        "{\"type\":\"com.freeletics.khonshu.text.DoubleArg\",\"value\":0.0}," +
        "{\"type\":\"com.freeletics.khonshu.text.TextResourceArg\",\"value\":" +
        "{\"type\":\"com.freeletics.khonshu.text.SimpleTextResource\",\"text\":\"\"}}]}," +
        "{\"type\":\"com.freeletics.khonshu.text.PluralTextResource\",\"id\":0,\"quantity\":1,\"args\":[" +
        "{\"type\":\"com.freeletics.khonshu.text.StringArg\",\"value\":\"a\"}," +
        "{\"type\":\"com.freeletics.khonshu.text.IntArg\",\"value\":0}," +
        "{\"type\":\"com.freeletics.khonshu.text.LongArg\",\"value\":0}," +
        "{\"type\":\"com.freeletics.khonshu.text.FloatArg\",\"value\":0.0}," + "" +
        "{\"type\":\"com.freeletics.khonshu.text.DoubleArg\",\"value\":0.0}," +
        "{\"type\":\"com.freeletics.khonshu.text.TextResourceArg\",\"value\":" +
        "{\"type\":\"com.freeletics.khonshu.text.SimpleTextResource\",\"text\":\"\"}}]}],\"separator\":\", \"}"

    @Test
    fun serialize() {
        assertThat(Json.encodeToString(textResource)).isEqualTo(json)
    }

    @Test
    fun deserialize() {
        assertThat(Json.decodeFromString<TextResource>(json)).isEqualTo(textResource)
    }

    @Test
    fun `serialize uses the actual element index for each arg`() {
        val encoder = RecordingEncoder()

        AnyArraySerializer.serialize(
            encoder,
            arrayOf<Any>(
                "string",
                1,
                2L,
                3.0f,
                4.0,
                TextResource("nested"),
            ),
        )

        assertThat(encoder.indices).containsExactly(0, 1, 2, 3, 4, 5).inOrder()
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class RecordingEncoder : AbstractEncoder() {
    val indices = mutableListOf<Int>()

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int,
    ): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) = Unit

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        indices += index
    }
}
