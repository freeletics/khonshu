package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.test.OtherRoute
import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.ThirdRoute
import com.freeletics.khonshu.navigation.test.destinations
import com.freeletics.khonshu.navigation.test.otherRouteDestination
import com.freeletics.khonshu.navigation.test.simpleRootDestination
import com.freeletics.khonshu.navigation.test.simpleRouteDestination
import com.freeletics.khonshu.navigation.test.thirdRouteDestination
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

internal class StackTest {

    private var nextId = 100
    private val idGenerator = { (nextId++).toString() }

    private val removed = mutableListOf<StackEntry.Id>()
    private val removedCallback: (StackEntry.Id) -> Unit = { removed.add(it) }

    @Test
    fun id() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)

        assertThat(stack.id).isEqualTo(simpleRootDestination.id)
    }

    @Test
    fun rootEntry() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)

        assertThat(stack.rootEntry)
            .isEqualTo(StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination))
    }

    @Test
    fun `isAtRoot after construction`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)

        assertThat(stack.isAtRoot).isTrue()
    }

    @Test
    fun `removed after construction`() {
        Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)

        assertThat(removed).isEmpty()
    }

    @Test
    fun `computeVisibleEntries after construction`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()
    }

    @Test
    fun `push with a screen destination`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with a dialog destination`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(OtherRoute(3))

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
                StackEntry(StackEntry.Id("101"), OtherRoute(3), otherRouteDestination),
            )
            .inOrder()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with a bottom sheet destination`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(ThirdRoute(4))

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
                StackEntry(StackEntry.Id("101"), ThirdRoute(4), thirdRouteDestination),
            )
            .inOrder()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `computeVisibleEntries with multiple screens, dialogs and bottom sheets`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("104"), SimpleRoute(5), simpleRouteDestination),
                StackEntry(StackEntry.Id("105"), OtherRoute(6), otherRouteDestination),
                StackEntry(StackEntry.Id("106"), ThirdRoute(7), thirdRouteDestination),
                StackEntry(StackEntry.Id("107"), OtherRoute(8), otherRouteDestination),
                StackEntry(StackEntry.Id("108"), OtherRoute(9), otherRouteDestination),
                StackEntry(StackEntry.Id("109"), ThirdRoute(10), thirdRouteDestination),
            )
            .inOrder()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `computeVisibleEntries with multiple screens, dialogs and bottom sheets 2`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(OtherRoute(5))
        stack.push(ThirdRoute(6))
        stack.push(SimpleRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("106"), SimpleRoute(7), simpleRouteDestination),
                StackEntry(StackEntry.Id("107"), OtherRoute(8), otherRouteDestination),
                StackEntry(StackEntry.Id("108"), OtherRoute(9), otherRouteDestination),
                StackEntry(StackEntry.Id("109"), ThirdRoute(10), thirdRouteDestination),
            )
            .inOrder()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `pop from the root`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        val exception = assertThrows(IllegalStateException::class.java) {
            stack.pop()
        }
        assertThat(exception).hasMessageThat().isEqualTo("Can't pop the root of the back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `pop from a screen`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()

        stack.pop()

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `pop from a screen and then opening that screen again`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("101"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()

        stack.pop()
        stack.push(SimpleRoute(2))

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("102"), SimpleRoute(2), simpleRouteDestination),
            )
            .inOrder()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `popUpTo with inclusive false`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.computeVisibleEntries()).hasSize(6)

        stack.popUpTo(simpleRouteDestination.id, isInclusive = false)

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("104"), SimpleRoute(5), simpleRouteDestination),
            )
            .inOrder()

        assertThat(removed).containsExactly(
            StackEntry.Id("109"),
            StackEntry.Id("108"),
            StackEntry.Id("107"),
            StackEntry.Id("106"),
            StackEntry.Id("105"),
        ).inOrder()
    }

    @Test
    fun `popUpTo with inclusive true`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.computeVisibleEntries()).hasSize(6)

        stack.popUpTo(simpleRouteDestination.id, isInclusive = true)

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("103"), SimpleRoute(4), simpleRouteDestination),
            )
            .inOrder()

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
    fun `popUpTo with root and inclusive false`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.computeVisibleEntries()).hasSize(6)

        stack.popUpTo(simpleRootDestination.id, isInclusive = false)

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()

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
    fun `popUpTo with root and inclusive true`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(OtherRoute(6))
        stack.push(ThirdRoute(7))
        stack.push(OtherRoute(8))
        stack.push(OtherRoute(9))
        stack.push(ThirdRoute(10))

        assertThat(stack.computeVisibleEntries()).hasSize(6)

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
    fun `popUpTo with route not present on the stack`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(ThirdRoute(6))
        stack.push(ThirdRoute(7))

        assertThat(stack.computeVisibleEntries()).hasSize(3)

        val exception = assertThrows(IllegalStateException::class.java) {
            stack.popUpTo(otherRouteDestination.id, isInclusive = false)
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
    fun `clear removes everything except for the root`() {
        val stack = Stack.createWith(SimpleRoot(1), destinations, removedCallback, idGenerator)
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(ThirdRoute(6))
        stack.push(ThirdRoute(7))

        assertThat(stack.computeVisibleEntries()).hasSize(3)

        stack.clear()

        assertThat(stack.computeVisibleEntries())
            .containsExactly(
                StackEntry(StackEntry.Id("100"), SimpleRoot(1), simpleRootDestination),
            )
            .inOrder()

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
