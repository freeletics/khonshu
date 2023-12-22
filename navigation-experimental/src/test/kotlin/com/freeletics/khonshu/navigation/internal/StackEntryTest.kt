package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.simpleRootDestination
import com.freeletics.khonshu.navigation.test.simpleRouteDestination
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class StackEntryTest {

    @Test
    fun `StackEntry hashCode does not equal other's with the different id`() {
        val first = StackEntry(StackEntry.Id("a"), SimpleRoute(0), simpleRouteDestination)
        val other = StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination)
        assertThat(first.hashCode()).isNotEqualTo(other.hashCode())
    }

    @Test
    fun `StackEntry destinationId matches destination's id`() {
        val entry = StackEntry(StackEntry.Id("a"), SimpleRoute(0), simpleRouteDestination)
        assertThat(entry.destinationId).isEqualTo(simpleRouteDestination.id)
    }

    @Test
    fun `StackEntry with a NavRoute is removable`() {
        val entry = StackEntry(StackEntry.Id("a"), SimpleRoute(0), simpleRouteDestination)
        assertThat(entry.removable).isTrue()
    }

    @Test
    fun `StackEntry with a NavRoot is not removable`() {
        val entry = StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination)
        assertThat(entry.removable).isFalse()
    }
}
