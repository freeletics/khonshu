package com.freeletics.khonshu.navigation

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import org.junit.Test

class StackEntryWriterAndroidTest {
    @Test
    fun `platform values can be written`() {
        assertThat(canWriteToParcel(null)).isTrue()
        assertThat(canWriteToParcel("test")).isTrue()
        assertThat(canWriteToParcel(listOf("test", null))).isTrue()
        assertThat(canWriteToParcel(arrayOf("test"))).isTrue()
        assertThat(canWriteToParcel(mapOf("key" to "test"))).isTrue()
    }

    @Test
    fun `values requiring serializer can not be written`() {
        assertThat(canWriteToParcel(TestClass(2))).isFalse()
        assertThat(canWriteToParcel(listOf(TestClass(2)))).isFalse()
        assertThat(canWriteToParcel(listOf(listOf(TestClass(2))))).isFalse()
        assertThat(canWriteToParcel(arrayOf(TestClass(2)))).isFalse()
        assertThat(canWriteToParcel(mapOf("key" to TestClass(2)))).isFalse()
    }
}

@Serializable
private data class TestClass(val value: Int)
