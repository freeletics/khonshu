package com.freeletics.khonshu.text

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test
import kotlinx.serialization.json.Json

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
}
