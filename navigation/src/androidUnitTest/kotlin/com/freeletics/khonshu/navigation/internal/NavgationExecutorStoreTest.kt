package com.freeletics.khonshu.navigation.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class NavgationExecutorStoreTest {
    private val underTest = StackEntryStore {}

    @Test
    fun `store returns value from factory`() {
        var counter = 0
        val value = underTest.getOrCreate(Int::class) { ++counter }
        assertThat(value).isEqualTo(1)
    }

    @Test
    fun `store always returns the same value for the same key`() {
        var counter = 0
        val value1 = underTest.getOrCreate(Int::class) { ++counter }
        assertThat(value1).isEqualTo(1)
        val value2 = underTest.getOrCreate(Int::class) { ++counter }
        assertThat(value2).isEqualTo(1)
        val value3 = underTest.getOrCreate(Int::class) { ++counter }
        assertThat(value3).isEqualTo(1)
        val value4 = underTest.getOrCreate(Int::class) { ++counter }
        assertThat(value4).isEqualTo(1)
    }

    @Test
    fun `store returns new value after close`() {
        var counter = 0
        val value1 = underTest.getOrCreate(Int::class) { counter }
        assertThat(value1).isEqualTo(0)
        counter = 5
        val value2 = underTest.getOrCreate(Int::class) { counter }
        assertThat(value2).isEqualTo(0)

        underTest.close()
        val value3 = underTest.getOrCreate(Int::class) { counter }
        assertThat(value3).isEqualTo(5)
    }

    @Test
    fun `store keeps values for separate keys separate`() {
        var counter1 = 0
        var counter2 = 5L
        val value1 = underTest.getOrCreate(Int::class) { ++counter1 }
        assertThat(value1).isEqualTo(1)
        val value2 = underTest.getOrCreate(Long::class) { ++counter2 }
        assertThat(value2).isEqualTo(6)
        val value3 = underTest.getOrCreate(Int::class) { ++counter1 }
        assertThat(value3).isEqualTo(1)
        val value4 = underTest.getOrCreate(Long::class) { ++counter2 }
        assertThat(value4).isEqualTo(6)
    }

    @Test
    fun `store always returns the same object for key`() {
        val closeable = AutoCloseable { }
        val value1 = underTest.getOrCreate(AutoCloseable::class) { closeable }
        assertThat(value1).isEqualTo(closeable)
        val value2 = underTest.getOrCreate(AutoCloseable::class) { closeable }
        assertThat(value2).isEqualTo(closeable)
    }

    @Test
    fun `store closes stored Closeables on close`() {
        var closed = false
        val closeable = AutoCloseable { closed = true }
        underTest.getOrCreate(AutoCloseable::class) { closeable }

        underTest.close()
        assertThat(closed).isTrue()
    }
}
