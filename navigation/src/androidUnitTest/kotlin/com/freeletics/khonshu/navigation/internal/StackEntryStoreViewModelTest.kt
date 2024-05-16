package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.test.FakeCloseable
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class StackEntryStoreViewModelTest {

    private val savedStateHandle = SavedStateHandle()
    private val underTest = StackEntryStoreViewModel(savedStateHandle)

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
    fun `StoreViewModel returns different store for same id after store was closed`() {
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        valueA.close()
        val valueB = underTest.provideStore(StackEntry.Id("1"))
        assertThat(valueA).isNotSameInstanceAs(valueB)
    }

    @Test
    fun `StoreViewModel closes store after store was closed`() {
        val closeable = FakeCloseable()
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        valueA.getOrCreate(FakeCloseable::class) { closeable }
        valueA.close()
        assertThat(closeable.closed).isTrue()
    }

    @Test
    fun `StoreViewModel returns same store for same id after store was closed for same id`() {
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        val valueB = underTest.provideStore(StackEntry.Id("2"))
        valueB.close()
        val valueC = underTest.provideStore(StackEntry.Id("1"))
        assertThat(valueA).isSameInstanceAs(valueC)
    }

    @Test
    fun `StoreViewModel does not close store after closing store for different id`() {
        val closeable = FakeCloseable()
        val valueA = underTest.provideStore(StackEntry.Id("1"))
        valueA.getOrCreate(FakeCloseable::class) { closeable }
        val valueB = underTest.provideStore(StackEntry.Id("2"))
        valueB.close()
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
}
