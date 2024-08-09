package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.test.OtherRoot
import com.freeletics.khonshu.navigation.test.OtherRoute
import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestStackEntryFactory
import com.freeletics.khonshu.navigation.test.ThirdRoute
import com.freeletics.khonshu.navigation.test.visibleEntries
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class StackSnapshotTest {
    private val factory = TestStackEntryFactory()

    @Test
    fun `forEachVisibleDestination with just a root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
            ),
            factory.create(StackEntry.Id("z"), OtherRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("a"), SimpleRoot(0)),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with just a root but is start stack`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
            ),
            factory.create(StackEntry.Id("a"), SimpleRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("a"), SimpleRoot(0)),
        )
        assertThat(stackSnapshot.canNavigateBack).isFalse()
    }

    @Test
    fun `forEachVisibleDestination with just a route on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
                factory.create(StackEntry.Id("b"), SimpleRoute(0)),
            ),
            factory.create(StackEntry.Id("z"), OtherRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("b"), SimpleRoute(0)),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with just multiple routes on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
                factory.create(StackEntry.Id("b"), SimpleRoute(0)),
                factory.create(StackEntry.Id("c"), SimpleRoute(1)),
                factory.create(StackEntry.Id("d"), SimpleRoute(2)),
            ),
            factory.create(StackEntry.Id("z"), OtherRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("d"), SimpleRoute(2)),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with overlay on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
                factory.create(StackEntry.Id("b"), OtherRoute(0)),
            ),
            factory.create(StackEntry.Id("z"), OtherRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("a"), SimpleRoot(0)),
            factory.create(StackEntry.Id("b"), OtherRoute(0)),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with multiple overlays on top of root`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
                factory.create(StackEntry.Id("b"), OtherRoute(0)),
                factory.create(StackEntry.Id("c"), ThirdRoute(0)),
                factory.create(StackEntry.Id("d"), OtherRoute(1)),
            ),
            factory.create(StackEntry.Id("z"), OtherRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("a"), SimpleRoot(0)),
            factory.create(StackEntry.Id("b"), OtherRoute(0)),
            factory.create(StackEntry.Id("c"), ThirdRoute(0)),
            factory.create(StackEntry.Id("d"), OtherRoute(1)),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with overlay on top of route`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
                factory.create(StackEntry.Id("b"), SimpleRoute(0)),
                factory.create(StackEntry.Id("c"), OtherRoute(0)),
            ),
            factory.create(StackEntry.Id("z"), OtherRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("b"), SimpleRoute(0)),
            factory.create(StackEntry.Id("c"), OtherRoute(0)),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }

    @Test
    fun `forEachVisibleDestination with multiple overlays on top of route`() {
        val stackSnapshot = StackSnapshot(
            listOf(
                factory.create(StackEntry.Id("a"), SimpleRoot(0)),
                factory.create(StackEntry.Id("b"), SimpleRoute(0)),
                factory.create(StackEntry.Id("c"), OtherRoute(0)),
                factory.create(StackEntry.Id("d"), ThirdRoute(0)),
                factory.create(StackEntry.Id("e"), OtherRoute(1)),
            ),
            factory.create(StackEntry.Id("z"), OtherRoot(0)),
        )

        assertThat(stackSnapshot.visibleEntries).containsExactly(
            factory.create(StackEntry.Id("b"), SimpleRoute(0)),
            factory.create(StackEntry.Id("c"), OtherRoute(0)),
            factory.create(StackEntry.Id("d"), ThirdRoute(0)),
            factory.create(StackEntry.Id("e"), OtherRoute(1)),
        )
        assertThat(stackSnapshot.canNavigateBack).isTrue()
    }
}
