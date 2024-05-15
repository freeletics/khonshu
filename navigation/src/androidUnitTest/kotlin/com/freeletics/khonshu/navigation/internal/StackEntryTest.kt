package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestStackEntryFactory
import com.freeletics.khonshu.navigation.test.simpleRouteDestination
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class StackEntryTest {

    private val factory = TestStackEntryFactory()

    @Test
    fun `StackEntry hashCode does not equal other's with the different id`() {
        val first = factory.create(StackEntry.Id("a"), SimpleRoute(0))
        val other = factory.create(StackEntry.Id("b"), SimpleRoute(0))
        assertThat(first.hashCode()).isNotEqualTo(other.hashCode())
    }

    @Test
    fun `StackEntry destinationId matches destination's id`() {
        val entry = factory.create(StackEntry.Id("a"), SimpleRoute(0))
        assertThat(entry.destinationId).isEqualTo(simpleRouteDestination.id)
    }

    @Test
    fun `StackEntry with a NavRoute is removable`() {
        val entry = factory.create(StackEntry.Id("a"), SimpleRoute(0))
        assertThat(entry.removable).isTrue()
    }

    @Test
    fun `StackEntry with a NavRoot is not removable`() {
        val entry = factory.create(StackEntry.Id("a"), SimpleRoot(0))
        assertThat(entry.removable).isFalse()
    }
}
