package com.freeletics.mad.navigator.compose.internal

import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.compose.test.OtherRoot
import com.freeletics.mad.navigator.compose.test.OtherRoute
import com.freeletics.mad.navigator.compose.test.SimpleRoot
import com.freeletics.mad.navigator.compose.test.SimpleRoute
import com.freeletics.mad.navigator.compose.test.ThirdRoute
import com.freeletics.mad.navigator.compose.test.destinations
import com.freeletics.mad.navigator.compose.test.otherRootDestination
import com.freeletics.mad.navigator.compose.test.otherRouteDestination
import com.freeletics.mad.navigator.compose.test.simpleRootDestination
import com.freeletics.mad.navigator.compose.test.simpleRouteDestination
import com.freeletics.mad.navigator.compose.test.thirdRouteDestination
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

internal class MultiStackTest {

    private var nextId = 100
    private val idGenerator = { (nextId++).toString() }

    private val removed = mutableListOf<StackEntry.Id>()
    private val removedCallback: (StackEntry.Id) -> Unit = { removed.add(it) }

    private val defaultStack = stack(SimpleRoot(1))

    private fun stack(root: NavRoot): Stack {
        return Stack.createWith(root, destinations, removedCallback, idGenerator)
    }

    private fun underTest(
        startStack: Stack = defaultStack,
        currentStack: Stack = defaultStack,
    ) : MultiStack {
        return MultiStack(
            allStacks = listOfNotNull(startStack, currentStack).toMutableList(),
            startStack = startStack,
            currentStack = startStack,
            destinations = destinations,
            idGenerator = idGenerator,
            onStackEntryRemoved = removedCallback,
        )
    }

    @Test
    fun `startRoot matches given NavRoot`() {
        val stack = underTest()

        assertThat(stack.startRoot).isEqualTo(SimpleRoot(1))
    }

    @Test
    fun `removed is empty at the beginning`() {
        underTest()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `canNavigateBack is false at the beginning`() {
        val stack = underTest()

        assertThat(stack.canNavigateBack.value).isFalse()
    }

    @Test
    fun `visibleEntries is entry for given root at the beginning`() {
        val stack = underTest()

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isFalse()
    }

    @Test
    fun `visibleEntries is new entry after navigating to screen destination`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is original and new entry after navigating to dialog destination`() {
        val stack = underTest()
        stack.push(OtherRoute(3))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
                StackEntry(StackEntry.Id("101"), OtherRoute(3), otherRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is original and new entry after navigating to bottom sheet destination`() {
        val stack = underTest()
        stack.push(ThirdRoute(4))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
                StackEntry(StackEntry.Id("101"), ThirdRoute(4), thirdRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is all entries starting from last screen`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("104"), SimpleRoute(5), simpleRouteDestination),
                StackEntry(StackEntry.Id("105"), OtherRoute(6), otherRouteDestination),
                StackEntry(StackEntry.Id("106"), ThirdRoute(7), thirdRouteDestination),
                StackEntry(StackEntry.Id("107"), OtherRoute(8), otherRouteDestination),
                StackEntry(StackEntry.Id("108"), OtherRoute(9), otherRouteDestination),
                StackEntry(StackEntry.Id("109"), ThirdRoute(10), thirdRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is all entries starting from last screen 2`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(OtherRoute(5))
        stack.push(ThirdRoute(6))
        stack.push(SimpleRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("106"), SimpleRoute(7), simpleRouteDestination),
                StackEntry(StackEntry.Id("107"), OtherRoute(8), otherRouteDestination),
                StackEntry(StackEntry.Id("108"), OtherRoute(9), otherRouteDestination),
                StackEntry(StackEntry.Id("109"), ThirdRoute(10), thirdRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is new entry after navigating to root`() {
        val stack = underTest()
        stack.push(OtherRoot(1), restoreRootState = false, saveCurrentRootState = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(1), otherRootDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is new entry after navigating to root twice`() {
        val stack = underTest()
        stack.push(OtherRoot(1), restoreRootState = true, saveCurrentRootState = true)
        stack.push(SimpleRoot(1), restoreRootState = false, saveCurrentRootState = false)
        stack.push(OtherRoot(1), restoreRootState = true, saveCurrentRootState = true)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(1), otherRootDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `visibleEntries is new entry after navigating to root from other screen`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), restoreRootState = false, saveCurrentRootState = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(1), otherRootDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `visibleEntries is new entry after navigating to root from other screen, saves current state`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), restoreRootState = false, saveCurrentRootState = true)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(1), otherRootDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is new entry after navigating to root from other screen, restores current state`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), restoreRootState = false, saveCurrentRootState = true)
        stack.push(SimpleRoot(1), restoreRootState = true, saveCurrentRootState = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(1), simpleRouteDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `visibleEntries is new entry after navigating to root from other screen, restores2 current state`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), restoreRootState = false, saveCurrentRootState = true)
        stack.push(SimpleRoot(1), restoreRootState = true, saveCurrentRootState = true)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(1), simpleRouteDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is new entry after navigating to root from other screen, restores4 current state`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), restoreRootState = false, saveCurrentRootState = true)
        stack.push(OtherRoute(1))
        stack.push(SimpleRoot(1), restoreRootState = true, saveCurrentRootState = true)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(1), simpleRouteDestination)
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is new entry after navigating to root from other screen, restores5 current state`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), restoreRootState = false, saveCurrentRootState = true)
        stack.push(OtherRoute(1))
        stack.push(SimpleRoot(1), restoreRootState = true, saveCurrentRootState = true)
        stack.push(OtherRoot(1), restoreRootState = true, saveCurrentRootState = true)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(1), otherRootDestination),
                StackEntry(StackEntry.Id("103"), OtherRoute(1), otherRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).isEmpty()
    }

//    @Test
//    fun `visibleEntries does not change when navigating to activity`() {
//        val stack = underTest()
//
//        stack.push(SimpleActivity(1))
//        stack.push(SimpleActivity(2))
//        stack.push(OtherActivity(3))
//
//        assertThat(stack.visibleEntries.value)
//            .containsExactly(
//                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination)
//            )
//            .inOrder()
//        assertThat(stack.canNavigateBack.value).isFalse()
//
//        assertThat(started)
//            .containsExactly(
//                SimpleActivity(1) to simpleActivityDestination,
//                SimpleActivity(2) to simpleActivityDestination,
//                OtherActivity(3) to otherActivityDestination,
//            )
//    }

    @Test
    fun `navigateUp throws exception when start stack is at root`() {
        val stack = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            stack.popCurrentStack()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is root entry after navigateUp`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.popCurrentStack()

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigating the same route again after navigateUp will result in different stack entries`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.popCurrentStack()
        stack.push(SimpleRoute(2))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateUp from the root of a second stack`() {
        val stack = underTest()
        stack.push(OtherRoot(2), restoreRootState = false, saveCurrentRootState = false)

        val exception = assertThrows(IllegalStateException::class.java) {
            stack.popCurrentStack()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")
    }

    @Test
    fun `navigateUp in a second stack`() {
        val stack = underTest()
        stack.push(OtherRoot(2), restoreRootState = false, saveCurrentRootState = false)
        stack.push(SimpleRoute(3))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(3), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.popCurrentStack()

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()
    }

    @Test
    fun `pop throws exception when the current stack only contains the root`() {
        val stack = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            stack.pop()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't navigate back from the root of the start back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries is root entry after pop`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.pop()

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigating the same route again after pop will result in different stack entries`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.pop()
        stack.push(SimpleRoute(2))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack from a second root`() {
        val stack = underTest()
        stack.push(OtherRoot(2), restoreRootState = false, saveCurrentRootState = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.pop()

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack from a second root and navigating there again`() {
        val stack = underTest()
        stack.push(OtherRoot(2), restoreRootState = false, saveCurrentRootState = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.pop()
        stack.push(OtherRoot(2), restoreRootState = false, saveCurrentRootState = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `navigateBack in a second root`() {
        val stack = underTest()
        stack.push(OtherRoot(2), restoreRootState = false, saveCurrentRootState = false)
        stack.push(SimpleRoute(3))

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(3), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        stack.pop()

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("101"), OtherRoot(2), otherRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `popUpTo removes all destinations until first matching entry, inclusive false`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.visibleEntries.value).hasSize(6)

        stack.popUpTo(simpleRouteDestination.id, isInclusive = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("104"), SimpleRoute(5), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

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
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.visibleEntries.value).hasSize(6)

        stack.popUpTo(simpleRouteDestination.id, isInclusive = true)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("103"), SimpleRoute(4), simpleRouteDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isTrue()

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
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.visibleEntries.value).hasSize(6)

        stack.popUpTo(simpleRootDestination.id, isInclusive = false)

        assertThat(stack.visibleEntries.value)
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
        assertThat(stack.canNavigateBack.value).isFalse()

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
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.visibleEntries.value).hasSize(6)

        val exception = assertThrows(IllegalStateException::class.java) {
            stack.popUpTo(simpleRootDestination.id, isInclusive = true)
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
    fun `popUpTo with route not present on the stack throws exception`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(ThirdRoute(6))
        stack.push(ThirdRoute(7))

        assertThat(stack.visibleEntries.value).hasSize(3)

        val exception = assertThrows(IllegalStateException::class.java) {
            stack.popUpTo(otherRouteDestination.id, isInclusive = false)
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Route class com.freeletics.mad.navigator.compose.test.OtherRoute (Kotlin " +
                    "reflection is not available) not found on back stack")

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
