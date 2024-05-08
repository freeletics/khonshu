package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.test.OtherRoute
import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.ThirdRoute
import com.freeletics.khonshu.navigation.test.otherRouteDestination
import com.freeletics.khonshu.navigation.test.simpleRootDestination
import com.freeletics.khonshu.navigation.test.simpleRouteDestination
import com.freeletics.khonshu.navigation.test.thirdRouteDestination
import com.freeletics.khonshu.navigation.test.visibleEntries
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class StackSnapshotTest {

    @Test
    fun `forEachVisibleDestination with just a root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
            ),
            startStack = false,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with just a root but is start stack`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
            ),
            startStack = true,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isFalse()
    }

    @Test
    fun `forEachVisibleDestination with just a route on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
                StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination),
            ),
            startStack = false,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with just multiple routes on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
                StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination),
                StackEntry(StackEntry.Id("c"), SimpleRoute(1), simpleRouteDestination),
                StackEntry(StackEntry.Id("d"), SimpleRoute(2), simpleRouteDestination),
            ),
            startStack = false,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("d"), SimpleRoute(2), simpleRouteDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with overlay on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
                StackEntry(StackEntry.Id("b"), OtherRoute(0), otherRouteDestination),
            ),
            startStack = false,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
            StackEntry(StackEntry.Id("b"), OtherRoute(0), otherRouteDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with multiple overlays on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
                StackEntry(StackEntry.Id("b"), OtherRoute(0), otherRouteDestination),
                StackEntry(StackEntry.Id("c"), ThirdRoute(0), thirdRouteDestination),
                StackEntry(StackEntry.Id("d"), OtherRoute(1), otherRouteDestination),
            ),
            startStack = false,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
            StackEntry(StackEntry.Id("b"), OtherRoute(0), otherRouteDestination),
            StackEntry(StackEntry.Id("c"), ThirdRoute(0), thirdRouteDestination),
            StackEntry(StackEntry.Id("d"), OtherRoute(1), otherRouteDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with overlay on top of route`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
                StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination),
                StackEntry(StackEntry.Id("c"), OtherRoute(0), otherRouteDestination),
            ),
            startStack = false,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination),
            StackEntry(StackEntry.Id("c"), OtherRoute(0), otherRouteDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with multiple overlays on top of route`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                StackEntry(StackEntry.Id("a"), SimpleRoot(0), simpleRootDestination),
                StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination),
                StackEntry(StackEntry.Id("c"), OtherRoute(0), otherRouteDestination),
                StackEntry(StackEntry.Id("d"), ThirdRoute(0), thirdRouteDestination),
                StackEntry(StackEntry.Id("e"), OtherRoute(1), otherRouteDestination),
            ),
            startStack = false,
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            StackEntry(StackEntry.Id("b"), SimpleRoute(0), simpleRouteDestination),
            StackEntry(StackEntry.Id("c"), OtherRoute(0), otherRouteDestination),
            StackEntry(StackEntry.Id("d"), ThirdRoute(0), thirdRouteDestination),
            StackEntry(StackEntry.Id("e"), OtherRoute(1), otherRouteDestination),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }
}
