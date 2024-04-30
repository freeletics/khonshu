package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.test.FakeCloseable
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class StoreViewModelTest {

    private val savedStateHandle = SavedStateHandle()
    private val underTest = StoreViewModel(savedStateHandle)

    @Test
    fun `StoreViewModel returns same store for same id`() {
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        val valueB = underTest.provideStore(StackEntry.Id("1"))
        assertThat(valueA).isSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel returns different stores for different ids`() {
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        val valueB = underTest.provideStore(StackEntry.Id("2"))
        assertThat(valueA).isNotSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel returns different store for same id after removeEntry`() {
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        underTest.removeEntry(StackEntry.Id("1"))
        val valueB = underTest.provideStore(StackEntry.Id("1"))
        assertThat(valueA).isNotSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel closes store after removeEntry`() {
        val closeable = FakeCloseable()
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        valueA.getOrCreate(FakeCloseable::class) { closeable }
        underTest.removeEntry(StackEntry.Id("1"))
        assertThat(closeable.closed).isTrue()
    }

    @Test
    fun `StoreViewModel returns same store for same id after removeEntry for different id`() {
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        underTest.removeEntry(StackEntry.Id("2"))
        val valueB = underTest.provideStore(StackEntry.Id("1"))
        assertThat(valueA).isSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel does not close store after removeEntry for different id`() {
        val closeable = FakeCloseable()
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        valueA.getOrCreate(FakeCloseable::class) { closeable }
        underTest.removeEntry(StackEntry.Id("2"))
        assertThat(closeable.closed).isFalse()
    }

    @Test
    fun `StoreViewModel returns different store for same id after onCleared`() {
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        underTest.onCleared()
        val valueB = underTest.provideStore(StackEntry.Id("1"))
        assertThat(valueA).isNotSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel closes store after onCleared`() {
        val closeable = FakeCloseable()
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        valueA.getOrCreate(FakeCloseable::class) { closeable }
        underTest.onCleared()
        assertThat(closeable.closed).isTrue()
    }

    @Test
    fun `StoreViewModel returns same handle for same id`() {
        val valueA = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        val valueB = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        assertThat(valueA).isSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel returns different handles for different ids`() {
        val valueA = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        val valueB = underTest.provideSavedStateHandle(StackEntry.Id("2"))
        assertThat(valueA).isNotSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel returns different handle for same id after removeEntry`() {
        val valueA = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        underTest.removeEntry(StackEntry.Id("1"))
        val valueB = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        assertThat(valueA).isNotSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel returns same handle for same id after removeEntry for different id`() {
        val valueA = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        underTest.removeEntry(StackEntry.Id("2"))
        val valueB = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        assertThat(valueA).isSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel returns different handle for same id after onCleared`() {
        val valueA = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        underTest.onCleared()
        val valueB = underTest.provideSavedStateHandle(StackEntry.Id("1"))
        assertThat(valueA).isNotSameInstanceAs(valueB)
    }
}
