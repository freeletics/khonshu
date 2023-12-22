package com.freeletics.khonshu.navigation.internal

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.test.OtherRoot
import com.freeletics.khonshu.navigation.test.OtherRoute
import com.freeletics.khonshu.navigation.test.SimpleActivity
import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.ThirdRoute
import com.freeletics.khonshu.navigation.test.destinations
import com.freeletics.khonshu.navigation.test.otherRootDestination
import com.freeletics.khonshu.navigation.test.otherRouteDestination
import com.freeletics.khonshu.navigation.test.simpleRootDestination
import com.freeletics.khonshu.navigation.test.simpleRouteDestination
import com.freeletics.khonshu.navigation.test.thirdRouteDestination
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

internal class MultiStackNavigationExecutorTest {

    private var nextId = 100
    private val idGenerator = { (nextId++).toString() }

    private val removed = mutableListOf<StackEntry.Id>()
    private val removedCallback: (StackEntry.Id) -> Unit = { removed.add(it) }

    private val started = mutableListOf<ActivityRoute>()
    private val starter: (ActivityRoute) -> Unit = { route ->
        started.add(route)
    }

    private val viewModel = StoreViewModel(SavedStateHandle())

    private fun underTest(
        deepLinkRoutes: List<Parcelable> = emptyList(),
    ): MultiStackNavigationExecutor {
        return MultiStackNavigationExecutor(
            activityStarter = starter,
            viewModel = viewModel,
            stack = MultiStack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator),
            deepLinkRoutes = deepLinkRoutes,
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
        val exception = assertThrows(IllegalArgumentException::class.java) {
            underTest(
                listOf(SimpleRoot(3)),
            )
        }

        assertThat(exception).hasMessageThat().isEqualTo(
            "SimpleRoot(number=3) is the start root" +
                " which is not allowed to be part of a deep link because it will always be on the" +
                " back stack",
        )
    }

    @Test
    fun `deep links passed with a root at an index other than the first`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            underTest(
                listOf(SimpleRoute(1), SimpleRoot(3)),
            )
        }

        assertThat(exception).hasMessageThat()
            .isEqualTo("NavRoot can only be the first element of a deep link")
    }

    @Test
    fun `deep links passed with a NavRoute`() {
        val executor = underTest(
            listOf(SimpleRoute(2)),
        )

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()
    }

    @Test
    fun `deep links passed with a NavRoot`() {
        val executor = underTest(
            listOf(OtherRoot(2)),
        )

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()
    }

    @Test
    fun `deep links passed with NavRoot and NavRoute`() {
        val executor = underTest(
            listOf(OtherRoot(2), SimpleRoute(3)),
        )

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("103"), SimpleRoute(3), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()
    }

    @Test
    fun `deep links passed with multiple NavRoutes`() {
        val executor = underTest(
            listOf(SimpleRoute(2), SimpleRoute(3), OtherRoute(4), ThirdRoute(5)),
        )

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("103"), SimpleRoute(3), simpleRouteDestination),
                StackEntry(StackEntry.Id("104"), OtherRoute(4), otherRouteDestination),
                StackEntry(StackEntry.Id("105"), ThirdRoute(5), thirdRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()
    }

    @Test
    fun `deep links passed with an ActivityRoute`() {
        val executor = underTest(
            listOf(SimpleActivity(2)),
        )

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(started).containsExactly(SimpleActivity(2))
    }

    @Test
    fun `deep links passed with a NavRoute and an ActivityRoute`() {
        val executor = underTest(
            listOf(SimpleRoute(2), SimpleActivity(3)),
        )

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(started).containsExactly(SimpleActivity(3))
    }

    @Test
    fun `visibleEntries contains entry for given root at the beginning`() {
        val executor = underTest()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()
    }

    @Test
    fun `visibleEntries contains new entry after navigating to screen destination`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains original and new entry after navigating to dialog destination`() {
        val executor = underTest()
        executor.navigateTo(OtherRoute(3))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
                StackEntry(StackEntry.Id("101"), OtherRoute(3), otherRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains original and new entry after navigating to bottom sheet destination`() {
        val executor = underTest()
        executor.navigateTo(ThirdRoute(4))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
                StackEntry(StackEntry.Id("101"), ThirdRoute(4), thirdRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains all entries starting from last screen`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))
        executor.navigateTo(SimpleRoute(3))
        executor.navigateTo(SimpleRoute(4))
        executor.navigateTo(SimpleRoute(5))
        executor.navigateTo(OtherRoute(6))
        executor.navigateTo(ThirdRoute(7))
        executor.navigateTo(OtherRoute(8))
        executor.navigateTo(OtherRoute(9))
        executor.navigateTo(ThirdRoute(10))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("104"), SimpleRoute(5), simpleRouteDestination),
                StackEntry(StackEntry.Id("105"), OtherRoute(6), otherRouteDestination),
                StackEntry(StackEntry.Id("106"), ThirdRoute(7), thirdRouteDestination),
                StackEntry(StackEntry.Id("107"), OtherRoute(8), otherRouteDestination),
                StackEntry(StackEntry.Id("108"), OtherRoute(9), otherRouteDestination),
                StackEntry(StackEntry.Id("109"), ThirdRoute(10), thirdRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains all entries starting from last screen 2`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))
        executor.navigateTo(SimpleRoute(3))
        executor.navigateTo(SimpleRoute(4))
        executor.navigateTo(OtherRoute(5))
        executor.navigateTo(ThirdRoute(6))
        executor.navigateTo(SimpleRoute(7))
        executor.navigateTo(OtherRoute(8))
        executor.navigateTo(OtherRoute(9))
        executor.navigateTo(ThirdRoute(10))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("106"), SimpleRoute(7), simpleRouteDestination),
                StackEntry(StackEntry.Id("107"), OtherRoute(8), otherRouteDestination),
                StackEntry(StackEntry.Id("108"), OtherRoute(9), otherRouteDestination),
                StackEntry(StackEntry.Id("109"), ThirdRoute(10), thirdRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root and without clearing the target executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with same root twice`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)
        val exception = assertThrows(IllegalStateException::class.java) {
            executor.navigateToRoot(OtherRoot(1), restoreRootState = true)
        }

        assertThat(exception).hasMessageThat()
            .isEqualTo("OtherRoot(number=1) is already the current stack")
    }

    @Test
    fun `navigate with root multiple times without clearing the target executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateToRoot(SimpleRoot(1), restoreRootState = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root multiple times with clearing the target executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(1), restoreRootState = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root and clearing the target executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(1), restoreRootState = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateToRoot(SimpleRoot(1), restoreRootState = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        executor.navigateToRoot(OtherRoot(1), restoreRootState = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("103"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `navigate with root and without clearing the target executor from within back executor`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(1))
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `navigate with root multiple times and without clearing the target executor from within back executor`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(1))
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)
        executor.navigateToRoot(SimpleRoot(1), restoreRootState = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(1), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `resetToRoot with start root from start executor`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(1))
        executor.resetToRoot(SimpleRoot(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoot(2), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `resetToRoot with start root from other executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)
        executor.resetToRoot(SimpleRoot(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoot(2), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `resetToRoot fails throws exception when root not on back executor`() {
        val executor = underTest()

        val exception = assertThrows(IllegalStateException::class.java) {
            executor.resetToRoot(OtherRoot(1))
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("OtherRoot(number=1) is not on the current back stack")
    }

    @Test
    fun `replaceAll with start root from start executor`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(1))
        executor.replaceAll(SimpleRoot(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(
                    StackEntry.Id("102"),
                    SimpleRoot(2),
                    simpleRootDestination,
                ),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(removed)
            .containsExactly(
                StackEntry.Id("100"),
                StackEntry.Id("101"),
            )
    }

    @Test
    fun `replaceAll with start root from other executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)
        executor.replaceAll(SimpleRoot(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(
                    StackEntry.Id("102"),
                    SimpleRoot(2),
                    simpleRootDestination,
                ),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(
            StackEntry.Id("100"),
            StackEntry.Id("101"),
        )
    }

    @Test
    fun `replaceAll after navigating with root and without clearing the target executor from within back executor`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(1))
        executor.navigateToRoot(OtherRoot(1), restoreRootState = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(1), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()
        assertThat(removed).isEmpty()

        executor.replaceAll(SimpleRoot(1))
        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("103"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()
        assertThat(removed)
            .containsExactly(
                StackEntry.Id("102"),
                StackEntry.Id("101"),
                StackEntry.Id("100"),
            )
    }

    @Test
    fun `navigateUp throws exception when start executor is at root`() {
        val executor = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            executor.navigateUp()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains root entry after navigateUp`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateUp()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigating the same route again after navigateUp will result in different executor entries`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateUp()
        executor.navigateTo(SimpleRoute(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateUp from the root of a second executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(2), restoreRootState = false)

        val exception = assertThrows(IllegalStateException::class.java) {
            executor.navigateUp()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")
    }

    @Test
    fun `navigateUp in a second executor`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(2), restoreRootState = false)
        executor.navigateTo(SimpleRoute(3))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(3), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateUp()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()
    }

    @Test
    fun `pop throws exception when the current executor only contains the root`() {
        val executor = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            executor.navigateBack()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't navigate back from the root of the start back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries contains root entry after pop`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigating the same route again after pop will result in different executor entries`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()
        executor.navigateTo(SimpleRoute(2))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack from a second root`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(2), restoreRootState = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack from a second root and navigating there again`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(2), restoreRootState = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()
        executor.navigateToRoot(OtherRoot(2), restoreRootState = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack in a second root`() {
        val executor = underTest()
        executor.navigateToRoot(OtherRoot(2), restoreRootState = false)
        executor.navigateTo(SimpleRoute(3))

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(3), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        executor.navigateBack()

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `popUpTo removes all destinations until first matching entry, inclusive false`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))
        executor.navigateTo(SimpleRoute(3))
        executor.navigateTo(SimpleRoute(4))
        executor.navigateTo(SimpleRoute(5))
        executor.navigateTo(OtherRoute(6))
        executor.navigateTo(ThirdRoute(7))
        executor.navigateTo(OtherRoute(8))
        executor.navigateTo(OtherRoute(9))
        executor.navigateTo(ThirdRoute(10))

        assertThat(executor.visibleEntries.value).hasSize(6)

        executor.navigateBackToInternal(simpleRouteDestination.id, inclusive = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("104"), SimpleRoute(5), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

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
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))
        executor.navigateTo(SimpleRoute(3))
        executor.navigateTo(SimpleRoute(4))
        executor.navigateTo(SimpleRoute(5))
        executor.navigateTo(OtherRoute(6))
        executor.navigateTo(ThirdRoute(7))
        executor.navigateTo(OtherRoute(8))
        executor.navigateTo(OtherRoute(9))
        executor.navigateTo(ThirdRoute(10))

        assertThat(executor.visibleEntries.value).hasSize(6)

        executor.navigateBackToInternal(simpleRouteDestination.id, inclusive = true)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("103"), SimpleRoute(4), simpleRouteDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isTrue()

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
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))
        executor.navigateTo(SimpleRoute(3))
        executor.navigateTo(SimpleRoute(4))
        executor.navigateTo(SimpleRoute(5))
        executor.navigateTo(OtherRoute(6))
        executor.navigateTo(ThirdRoute(7))
        executor.navigateTo(OtherRoute(8))
        executor.navigateTo(OtherRoute(9))
        executor.navigateTo(ThirdRoute(10))

        assertThat(executor.visibleEntries.value).hasSize(6)

        executor.navigateBackToInternal(simpleRootDestination.id, inclusive = false)

        assertThat(executor.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(executor.canNavigateBack.value).isFalse()

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
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))
        executor.navigateTo(SimpleRoute(3))
        executor.navigateTo(SimpleRoute(4))
        executor.navigateTo(SimpleRoute(5))
        executor.navigateTo(OtherRoute(6))
        executor.navigateTo(ThirdRoute(7))
        executor.navigateTo(OtherRoute(8))
        executor.navigateTo(OtherRoute(9))
        executor.navigateTo(ThirdRoute(10))

        assertThat(executor.visibleEntries.value).hasSize(6)

        val exception = assertThrows(IllegalStateException::class.java) {
            executor.navigateBackToInternal(simpleRootDestination.id, inclusive = true)
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
    fun `popUpTo with route not present on the executor throws exception`() {
        val executor = underTest()
        executor.navigateTo(SimpleRoute(2))
        executor.navigateTo(SimpleRoute(3))
        executor.navigateTo(SimpleRoute(4))
        executor.navigateTo(SimpleRoute(5))
        executor.navigateTo(ThirdRoute(6))
        executor.navigateTo(ThirdRoute(7))

        assertThat(executor.visibleEntries.value).hasSize(3)

        val exception = assertThrows(IllegalStateException::class.java) {
            executor.navigateBackToInternal(otherRouteDestination.id, inclusive = false)
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
