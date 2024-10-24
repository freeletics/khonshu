package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.Navigator.Companion.navigateBackTo
import com.freeletics.khonshu.navigation.test.OtherRoot
import com.freeletics.khonshu.navigation.test.OtherRoute
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
    private val factory = TestStackEntryFactory()
    private val removed get() = factory.closedEntries

    private val viewModel = StackEntryStoreViewModel(SavedStateHandle())

    private fun underTest(): MultiStackHostNavigator {
        return MultiStackHostNavigator(
            stack = MultiStack.createWith(SimpleRoot(1), factory::create),
            viewModel = viewModel,
        )
    }

    @Test
    fun `removed is empty at the beginning`() {
        underTest()

        assertThat(removed).isEmpty()
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
    fun `switchBackStack with a different root`() {
        val hostNavigator = underTest()
        hostNavigator.switchBackStack(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `switchBackStack to the same root twice`() {
        val hostNavigator = underTest()
        hostNavigator.switchBackStack(OtherRoot(1))

        val rootId = hostNavigator.snapshot.value.visibleEntries[0].id

        hostNavigator.switchBackStack(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                // entry id being 101 means second switch didn't create a new entry
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.visibleEntries.get(0).id).isEqualTo(rootId)
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()
    }

    @Test
    fun `switchBackStack between 2 different roots twice`() {
        val hostNavigator = underTest()
        hostNavigator.switchBackStack(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.switchBackStack(SimpleRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        hostNavigator.switchBackStack(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `switchBackStack from a non root destination`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.switchBackStack(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `switchBackStack from a non root destination and back`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.switchBackStack(OtherRoot(1))
        hostNavigator.switchBackStack(SimpleRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `showRoot with the same root`() {
        val hostNavigator = underTest()
        hostNavigator.showRoot(SimpleRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"))
    }

    @Test
    fun `showRoot with a different root`() {
        val hostNavigator = underTest()
        hostNavigator.showRoot(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `showRoot multiple times`() {
        val hostNavigator = underTest()
        hostNavigator.showRoot(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.showRoot(SimpleRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        hostNavigator.showRoot(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `showRoot with start root from a non root destination`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.showRoot(SimpleRoot(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `showRoot with different root from a non root destination`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.showRoot(OtherRoot(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `replaceAllBackStacks with start root`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.replaceAllBackStacks(SimpleRoot(2))

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
    fun `replaceAllBackStacks with start root from other root`() {
        val hostNavigator = underTest()
        hostNavigator.switchBackStack(OtherRoot(1))
        hostNavigator.replaceAllBackStacks(SimpleRoot(2))

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
    fun `replaceAllBackStacks after navigating multiple times`() {
        val hostNavigator = underTest()
        hostNavigator.navigateTo(SimpleRoute(1))
        hostNavigator.switchBackStack(OtherRoot(1))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()
        assertThat(removed).isEmpty()

        hostNavigator.replaceAllBackStacks(SimpleRoot(1))
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
        hostNavigator.showRoot(OtherRoot(2))

        val exception = assertThrows(IllegalStateException::class.java) {
            hostNavigator.navigateUp()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")
    }

    @Test
    fun `navigateUp in a second hostNavigator`() {
        val hostNavigator = underTest()
        hostNavigator.showRoot(OtherRoot(2))
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
        hostNavigator.showRoot(OtherRoot(2))

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
        hostNavigator.showRoot(OtherRoot(2))

        assertThat(hostNavigator.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(hostNavigator.snapshot.value.canNavigateBack).isTrue()

        hostNavigator.navigateBack()
        hostNavigator.showRoot(OtherRoot(2))

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
        hostNavigator.showRoot(OtherRoot(2))
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

    @Test
    fun `navigate with block only updates state at the end`() {
        val hostNavigator = underTest()

        assertThat(hostNavigator.snapshot.value.entries).hasSize(1)

        hostNavigator.navigate {
            navigateTo(SimpleRoute(2))
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)
            navigateTo(OtherRoute(3))
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)
            navigateTo(ThirdRoute(4))
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)
            navigateTo(ThirdRoute(5))
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)
            navigateTo(ThirdRoute(6))
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)
            navigateBack()
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)
            navigateUp()
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)

            navigateBackTo<OtherRoute>(inclusive = true)
            assertThat(hostNavigator.snapshot.value.entries).hasSize(1)
        }

        assertThat(hostNavigator.snapshot.value.entries).hasSize(2)
    }
}
