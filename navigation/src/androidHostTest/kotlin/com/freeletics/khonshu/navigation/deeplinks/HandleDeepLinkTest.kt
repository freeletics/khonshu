package com.freeletics.khonshu.navigation.deeplinks

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.internal.MultiStack
import com.freeletics.khonshu.navigation.internal.MultiStackHostNavigator
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.test.OtherRoot
import com.freeletics.khonshu.navigation.test.OtherRoute
import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestStackEntryFactory
import com.freeletics.khonshu.navigation.test.ThirdRoute
import com.freeletics.khonshu.navigation.test.visibleEntries
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.assertThrows
import org.junit.Test

internal class HandleDeepLinkTest {
    private val factory = TestStackEntryFactory()
    private val removed get() = factory.closedEntries

    private fun underTest(): MultiStackHostNavigator {
        return MultiStackHostNavigator(MultiStack.createWith(SimpleRoot(1), factory::create))
    }

    private fun HostNavigator.testHandleDeepLink(routes: List<BaseRoute>?): Boolean {
        return handleDeepLink(
            launchInfo = LaunchInfo(routes, null),
            deepLinkHandlers = persistentSetOf(),
            deepLinkPrefixes = persistentSetOf(),
        )
    }

    @Test
    fun `deep link with start root`() {
        val hostNavigator = underTest()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            hostNavigator.testHandleDeepLink(listOf(SimpleRoot(3)))
        }

        assertThat(exception).hasMessageThat().isEqualTo(
            "SimpleRoot(number=3) is the start root" +
                " which is not allowed to be part of a deep link because it will always be on the" +
                " back stack",
        )
    }

    @Test
    fun `deep links passed with a root at an index other than the first`() {
        val hostNavigator = underTest()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            hostNavigator.testHandleDeepLink(listOf(SimpleRoute(1), SimpleRoot(3)))
        }

        assertThat(exception).hasMessageThat()
            .isEqualTo("NavRoot can only be the first element of a deep link")
    }

    @Test
    fun `deep links passed with a NavRoute`() {
        val hostNavigator = underTest()
        hostNavigator.testHandleDeepLink(listOf(SimpleRoute(2)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `deep links passed with a NavRoute when not at root`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(OtherRoute(3))
        hostNavigator.testHandleDeepLink(listOf(SimpleRoute(2)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        // original entries removed
        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `deep links passed with a NavRoot`() {
        val hostNavigator = underTest()
        hostNavigator.testHandleDeepLink(listOf(OtherRoot(2)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `deep links passed with a NavRoot when not at start root`() {
        val hostNavigator = underTest()
        hostNavigator.switchBackStack(OtherRoot(1))
        hostNavigator.testHandleDeepLink(listOf(OtherRoot(2)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        // original entries removed
        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `deep links passed with NavRoot and NavRoute`() {
        val hostNavigator = underTest()
        hostNavigator.testHandleDeepLink(listOf(OtherRoot(2), SimpleRoute(3)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(3)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `deep links passed with multiple NavRoutes`() {
        val hostNavigator = underTest()
        hostNavigator.testHandleDeepLink(listOf(SimpleRoute(2), SimpleRoute(3), OtherRoute(4), ThirdRoute(5)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(3)),
                factory.create(StackEntry.Id("104"), OtherRoute(4)),
                factory.create(StackEntry.Id("105"), ThirdRoute(5)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()
    }
}
