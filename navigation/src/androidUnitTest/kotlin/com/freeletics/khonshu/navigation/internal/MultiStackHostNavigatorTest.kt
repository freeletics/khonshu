package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.test.OtherRoot
import com.freeletics.khonshu.navigation.test.OtherRoute
import com.freeletics.khonshu.navigation.test.SimpleActivity
import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestStackEntryFactory
import com.freeletics.khonshu.navigation.test.ThirdRoute
import com.freeletics.khonshu.navigation.test.otherRouteDestination
import com.freeletics.khonshu.navigation.test.simpleRootDestination
import com.freeletics.khonshu.navigation.test.simpleRouteDestination
import com.freeletics.khonshu.navigation.test.visibleEntries
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

internal class MultiStackHostNavigatorTest {

    private val started = mutableListOf<ActivityRoute>()
    private val starter: (ActivityRoute) -> Unit = { route ->
        started.add(route)
    }

    private val factory = TestStackEntryFactory()
    private val removed get() = factory.closedEntries

    private val viewModel = StackEntryStoreViewModel(SavedStateHandle())

    private fun underTest(): MultiStackHostNavigator {
        return MultiStackHostNavigator(
            stack = MultiStack.createWith(SimpleRoot(1), factory::create),
            activityStarter = starter,
            viewModel = viewModel,
        )
    }

    @Test
    fun `removed is empty at the beginning`() {
        underTest()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `started is empty at the beginning`() {
        underTest()

        assertThat(started).isEmpty()
    }

    @Test
    fun `deep link with start root`() {
        val hostNavigator = underTest()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            hostNavigator.handleDeepLink(listOf(SimpleRoot(3)))
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
            hostNavigator.handleDeepLink(listOf(SimpleRoute(1), SimpleRoot(3)))
        }

        assertThat(exception).hasMessageThat()
            .isEqualTo("NavRoot can only be the first element of a deep link")
    }

    @Test
    fun `deep links passed with a NavRoute`() {
        val hostNavigator = underTest()
        hostNavigator.handleDeepLink(listOf(SimpleRoute(2)))

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
    fun `deep links passed with a NavRoot`() {
        val hostNavigator = underTest()
        hostNavigator.handleDeepLink(listOf(OtherRoot(2)))

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
    fun `deep links passed with NavRoot and NavRoute`() {
        val hostNavigator = underTest()
        hostNavigator.handleDeepLink(listOf(OtherRoot(2), SimpleRoute(3)))

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
        hostNavigator.handleDeepLink(listOf(SimpleRoute(2), SimpleRoute(3), OtherRoute(4), ThirdRoute(5)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(3)),
                factory.create(StackEntry.Id("104"), OtherRoute(4)),
                factory.create(StackEntry.Id("105"), ThirdRoute(5)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()
    }

    @Test
    fun `deep links passed with an ActivityRoute`() {
        val hostNavigator = underTest()
        hostNavigator.handleDeepLink(listOf(SimpleActivity(2)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(started).containsExactly(SimpleActivity(2))
    }

    @Test
    fun `deep links passed with a NavRoute and an ActivityRoute`() {
        val hostNavigator = underTest()
        hostNavigator.handleDeepLink(listOf(SimpleRoute(2), SimpleActivity(3)))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(started).containsExactly(SimpleActivity(3))
    }

    @Test
    fun `visibleEntries contains entry for given root at the beginning`() {
        val hostNavigator = underTest()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `visibleEntries contains new entry after navigating to screen destination`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains original and new entry after navigating to dialog destination`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(OtherRoute(3))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
                factory.create(StackEntry.Id("101"), OtherRoute(3)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains original and new entry after navigating to bottom sheet destination`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(ThirdRoute(4))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
                factory.create(StackEntry.Id("101"), ThirdRoute(4)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains all entries starting from last screen`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))
        hostNavigator.navigateTo(SimpleRoute(3))
        hostNavigator.navigateTo(SimpleRoute(4))
        hostNavigator.navigateTo(SimpleRoute(5))
        hostNavigator.navigateTo(OtherRoute(6))
        hostNavigator.navigateTo(ThirdRoute(7))
        hostNavigator.navigateTo(OtherRoute(8))
        hostNavigator.navigateTo(OtherRoute(9))
        hostNavigator.navigateTo(ThirdRoute(10))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("104"), SimpleRoute(5)),
                factory.create(StackEntry.Id("105"), OtherRoute(6)),
                factory.create(StackEntry.Id("106"), ThirdRoute(7)),
                factory.create(StackEntry.Id("107"), OtherRoute(8)),
                factory.create(StackEntry.Id("108"), OtherRoute(9)),
                factory.create(StackEntry.Id("109"), ThirdRoute(10)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains all entries starting from last screen 2`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))
        hostNavigator.navigateTo(SimpleRoute(3))
        hostNavigator.navigateTo(SimpleRoute(4))
        hostNavigator.navigateTo(OtherRoute(5))
        hostNavigator.navigateTo(ThirdRoute(6))
        hostNavigator.navigateTo(SimpleRoute(7))
        hostNavigator.navigateTo(OtherRoute(8))
        hostNavigator.navigateTo(OtherRoute(9))
        hostNavigator.navigateTo(ThirdRoute(10))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("106"), SimpleRoute(7)),
                factory.create(StackEntry.Id("107"), OtherRoute(8)),
                factory.create(StackEntry.Id("108"), OtherRoute(9)),
                factory.create(StackEntry.Id("109"), ThirdRoute(10)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root and without clearing the target hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with same root twice`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)
        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)
        }

        assertThat(exception).hasMessageThat()
            .isEqualTo("OtherRoot(number=1) is already the current stack")
    }

    @Test
    fun `navigate with root multiple times without clearing the target hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateToRoot(SimpleRoot(1), restoreRootState = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root multiple times with clearing the target hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root and clearing the target hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateToRoot(SimpleRoot(1), restoreRootState = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `navigate with root and without clearing the target hostNavigator from within back hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root multiple times and without clearing the target hostNavigator from within back hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)
        hostNavigator.navigateToRoot(SimpleRoot(1), restoreRootState = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `resetToRoot with start root from start hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.resetToRoot(SimpleRoot(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `resetToRoot with start root from other hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)
        hostNavigator.resetToRoot(SimpleRoot(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `resetToRoot fails throws exception when root not on back hostNavigator`() {
        val hostNavigator = underTest()

        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.resetToRoot(OtherRoot(1))
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("OtherRoot(number=1) is not on the current back stack")
    }

    @Test
    fun `replaceAll with start root from start hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.replaceAll(SimpleRoot(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed)
            .containsExactly(
                StackEntry.Id("100"),
                StackEntry.Id("101"),
            )
    }

    @Test
    fun `replaceAll with start root from other hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)
        hostNavigator.replaceAll(SimpleRoot(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(
            StackEntry.Id("100"),
            StackEntry.Id("101"),
        )
    }

    @Test
    fun `replaceAll after navigating with root and without clearing the target hostNavigator from within back hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()
        assertThat(removed).isEmpty()

        hostNavigator.replaceAll(SimpleRoot(1))
        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()
        assertThat(removed)
            .containsExactly(
                StackEntry.Id("102"),
                StackEntry.Id("101"),
                StackEntry.Id("100"),
            )
    }

    @Test
    fun `navigateUp throws exception when start hostNavigator is at root`() {
        val hostNavigator = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.navigateUp()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains root entry after navigateUp`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateUp()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigating the same route again after navigateUp will result in different hostNavigator entries`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateUp()
        hostNavigator.navigateTo(SimpleRoute(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateUp from the root of a second hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(2), restoreRootState = false)

        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.navigateUp()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")
    }

    @Test
    fun `navigateUp in a second hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(2), restoreRootState = false)
        hostNavigator.navigateTo(SimpleRoute(3))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(3)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateUp()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()
    }

    @Test
    fun `pop throws exception when the current hostNavigator only contains the root`() {
        val hostNavigator = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.navigateBack()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't navigate back from the root of the start back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains root entry after pop`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigating the same route again after pop will result in different hostNavigator entries`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()
        hostNavigator.navigateTo(SimpleRoute(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack from a second root`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(2), restoreRootState = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack from a second root and navigating there again`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(2), restoreRootState = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()
        hostNavigator.navigateToRoot(OtherRoot(2), restoreRootState = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack in a second root`() {
        val hostNavigator = underTest()
        hostNavigator.navigateToRoot(OtherRoot(2), restoreRootState = false)
        hostNavigator.navigateTo(SimpleRoute(3))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(3)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `popUpTo removes all destinations until first matching entry, inclusive false`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))
        hostNavigator.navigateTo(SimpleRoute(3))
        hostNavigator.navigateTo(SimpleRoute(4))
        hostNavigator.navigateTo(SimpleRoute(5))
        hostNavigator.navigateTo(OtherRoute(6))
        hostNavigator.navigateTo(ThirdRoute(7))
        hostNavigator.navigateTo(OtherRoute(8))
        hostNavigator.navigateTo(OtherRoute(9))
        hostNavigator.navigateTo(ThirdRoute(10))

        assertThat(hostNavigator.snapshot.value.visibleEntries).hasSize(6)

        hostNavigator.navigateBackTo(simpleRouteDestination.id.route, inclusive = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("104"), SimpleRoute(5)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(
            StackEntry.Id("109"),
            StackEntry.Id("108"),
            StackEntry.Id("107"),
            StackEntry.Id("106"),
            StackEntry.Id("105"),
        ).inOrder()
    }

    @Test
    fun `popUpTo removes all destinations until first matching entry, inclusive true`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))
        hostNavigator.navigateTo(SimpleRoute(3))
        hostNavigator.navigateTo(SimpleRoute(4))
        hostNavigator.navigateTo(SimpleRoute(5))
        hostNavigator.navigateTo(OtherRoute(6))
        hostNavigator.navigateTo(ThirdRoute(7))
        hostNavigator.navigateTo(OtherRoute(8))
        hostNavigator.navigateTo(OtherRoute(9))
        hostNavigator.navigateTo(ThirdRoute(10))

        assertThat(hostNavigator.snapshot.value.visibleEntries).hasSize(6)

        hostNavigator.navigateBackTo(simpleRouteDestination.id.route, inclusive = true)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(4)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(
            StackEntry.Id("109"),
            StackEntry.Id("108"),
            StackEntry.Id("107"),
            StackEntry.Id("106"),
            StackEntry.Id("105"),
            StackEntry.Id("104"),
        ).inOrder()
    }

    @Test
    fun `popUpTo with root and inclusive false removes all destinations`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))
        hostNavigator.navigateTo(SimpleRoute(3))
        hostNavigator.navigateTo(SimpleRoute(4))
        hostNavigator.navigateTo(SimpleRoute(5))
        hostNavigator.navigateTo(OtherRoute(6))
        hostNavigator.navigateTo(ThirdRoute(7))
        hostNavigator.navigateTo(OtherRoute(8))
        hostNavigator.navigateTo(OtherRoute(9))
        hostNavigator.navigateTo(ThirdRoute(10))

        assertThat(hostNavigator.snapshot.value.visibleEntries).hasSize(6)

        hostNavigator.navigateBackTo(simpleRootDestination.id.route, inclusive = false)

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(
            StackEntry.Id("109"),
            StackEntry.Id("108"),
            StackEntry.Id("107"),
            StackEntry.Id("106"),
            StackEntry.Id("105"),
            StackEntry.Id("104"),
            StackEntry.Id("103"),
            StackEntry.Id("102"),
            StackEntry.Id("101"),
        ).inOrder()
    }

    @Test
    fun `popUpTo with root and inclusive true throws exception`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))
        hostNavigator.navigateTo(SimpleRoute(3))
        hostNavigator.navigateTo(SimpleRoute(4))
        hostNavigator.navigateTo(SimpleRoute(5))
        hostNavigator.navigateTo(OtherRoute(6))
        hostNavigator.navigateTo(ThirdRoute(7))
        hostNavigator.navigateTo(OtherRoute(8))
        hostNavigator.navigateTo(OtherRoute(9))
        hostNavigator.navigateTo(ThirdRoute(10))

        assertThat(hostNavigator.snapshot.value.visibleEntries).hasSize(6)

        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.navigateBackTo(simpleRootDestination.id.route, inclusive = true)
        }
        assertThat(exception).hasMessageThat().isEqualTo("Can't pop the root of the back stack")

        assertThat(removed).containsExactly(
            StackEntry.Id("109"),
            StackEntry.Id("108"),
            StackEntry.Id("107"),
            StackEntry.Id("106"),
            StackEntry.Id("105"),
            StackEntry.Id("104"),
            StackEntry.Id("103"),
            StackEntry.Id("102"),
            StackEntry.Id("101"),
        ).inOrder()
    }

    @Test
    fun `popUpTo with route not present on the hostNavigator throws exception`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(2))
        hostNavigator.navigateTo(SimpleRoute(3))
        hostNavigator.navigateTo(SimpleRoute(4))
        hostNavigator.navigateTo(SimpleRoute(5))
        hostNavigator.navigateTo(ThirdRoute(6))
        hostNavigator.navigateTo(ThirdRoute(7))

        assertThat(hostNavigator.snapshot.value.visibleEntries).hasSize(3)

        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.navigateBackTo(otherRouteDestination.id.route, inclusive = false)
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo(
                "Route class com.freeletics.khonshu.navigation.test.OtherRoute (Kotlin " +
                    "reflection is not available) not found on back stack",
            )

        assertThat(removed).containsExactly(
            StackEntry.Id("106"),
            StackEntry.Id("105"),
            StackEntry.Id("104"),
            StackEntry.Id("103"),
            StackEntry.Id("102"),
            StackEntry.Id("101"),
        ).inOrder()
    }
}
