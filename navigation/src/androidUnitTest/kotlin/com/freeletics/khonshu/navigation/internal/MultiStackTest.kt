package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.NavRoot
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

internal class MultiStackTest {

    private var nextId = 100
    private val idGenerator = { StackEntry.Id((nextId++).toString()) }

    private val removed = mutableListOf<StackEntry.Id>()
    private val removedCallback: (StackEntry.Id) -> Unit = { removed.add(it) }

    private val defaultStack get() = stack(SimpleRoot(1))

    private val factory = TestStackEntryFactory()

    private fun stack(root: NavRoot): Stack {
        return Stack.createWith(root, factory::create, removedCallback)
    }

    private fun underTest(
        startStack: Stack = defaultStack,
    ): MultiStack {
        return MultiStack(
            allStacks = arrayListOf(startStack),
            startStack = startStack,
            currentStack = startStack,
            createEntry = factory::create,
            onStackEntryRemoved = removedCallback,
            inputRoot = startStack.rootEntry.route as NavRoot,
        )
    }

    @Test
    fun startRoot() {
        val stack = underTest()

        assertThat(stack.startRoot).isEqualTo(SimpleRoot(1))
    }

    @Test
    fun `removed after construction`() {
        underTest()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `canNavigateBack after construction`() {
        val stack = underTest()

        assertThat(stack.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `visibleEntries after construction`() {
        val stack = underTest()

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()
    }

    @Test
    fun `push with a screen destination`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with a dialog destination`() {
        val stack = underTest()
        stack.push(OtherRoute(3))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
                factory.create(StackEntry.Id("101"), OtherRoute(3)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with a bottom sheet destination`() {
        val stack = underTest()
        stack.push(ThirdRoute(4))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
                factory.create(StackEntry.Id("101"), ThirdRoute(4)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries with multiple screens, dialogs and bottom sheets`() {
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

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("104"), SimpleRoute(5)),
                factory.create(StackEntry.Id("105"), OtherRoute(6)),
                factory.create(StackEntry.Id("106"), ThirdRoute(7)),
                factory.create(StackEntry.Id("107"), OtherRoute(8)),
                factory.create(StackEntry.Id("108"), OtherRoute(9)),
                factory.create(StackEntry.Id("109"), ThirdRoute(10)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `visibleEntries with multiple screens, dialogs and bottom sheets 2`() {
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

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("106"), SimpleRoute(7)),
                factory.create(StackEntry.Id("107"), OtherRoute(8)),
                factory.create(StackEntry.Id("108"), OtherRoute(9)),
                factory.create(StackEntry.Id("109"), ThirdRoute(10)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with root and without clearing the target stack`() {
        val stack = underTest()
        stack.push(OtherRoot(1), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with same root twice`() {
        val stack = underTest()
        stack.push(OtherRoot(1), clearTargetStack = false)
        val exception = assertThrows(IllegalStateException::class.java) {
            stack.push(OtherRoot(1), clearTargetStack = false)
        }

        assertThat(exception).hasMessageThat()
            .isEqualTo("OtherRoot(number=1) is already the current stack")
    }

    @Test
    fun `push with root multiple times without clearing the target stack`() {
        val stack = underTest()
        stack.push(OtherRoot(1), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        stack.push(SimpleRoot(1), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        stack.push(OtherRoot(1), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with root multiple times with clearing the target stack`() {
        val stack = underTest()
        stack.push(OtherRoot(1), clearTargetStack = true)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with root and clearing the target stack`() {
        val stack = underTest()
        stack.push(OtherRoot(1), clearTargetStack = true)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        stack.push(SimpleRoot(1), clearTargetStack = true)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        stack.push(OtherRoot(1), clearTargetStack = true)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `push with root and without clearing the target stack from within back stack`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `push with root multiple times and without clearing the target stack from within back stack`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), clearTargetStack = false)
        stack.push(SimpleRoot(1), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), SimpleRoute(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `resetToRoot with start root from start stack`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.resetToRoot(SimpleRoot(2))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `resetToRoot with start root from other stack`() {
        val stack = underTest()
        stack.push(OtherRoot(1), clearTargetStack = false)
        stack.resetToRoot(SimpleRoot(2))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("100"), StackEntry.Id("101"))
    }

    @Test
    fun `resetToRoot fails throws exception when root not on back stack`() {
        val stack = underTest()

        val exception = assertThrows(IllegalStateException::class.java) {
            stack.resetToRoot(OtherRoot(1))
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("OtherRoot(number=1) is not on the current back stack")
    }

    @Test
    fun `replaceAll with start root from start executor`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.replaceAll(SimpleRoot(2))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed)
            .containsExactly(
                StackEntry.Id("100"),
                StackEntry.Id("101"),
            )
    }

    @Test
    fun `replaceAll with start root from other executor`() {
        val stack = underTest()
        stack.push(OtherRoot(1), clearTargetStack = false)
        stack.replaceAll(SimpleRoot(2))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoot(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(
            StackEntry.Id("100"),
            StackEntry.Id("101"),
        )
    }

    @Test
    fun `replaceAll after navigating with root and without clearing the target executor from within back executor`() {
        val stack = underTest()
        stack.push(SimpleRoute(1))
        stack.push(OtherRoot(1), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()
        assertThat(removed).isEmpty()

        stack.replaceAll(SimpleRoot(1))
        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()
        assertThat(removed)
            .containsExactly(
                StackEntry.Id("102"),
                StackEntry.Id("101"),
                StackEntry.Id("100"),
            )
    }

    @Test
    fun `popCurrentStack throws exception when start stack is at root`() {
        val stack = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            stack.popCurrentStack()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `popCurrentStack from a screen`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.popCurrentStack()

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `popCurrentStack from a screen and then opening that screen again`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.popCurrentStack()
        stack.push(SimpleRoute(2))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `popCurrentStack from the root of a second stack`() {
        val stack = underTest()
        stack.push(OtherRoot(2), clearTargetStack = false)

        val exception = assertThrows(IllegalStateException::class.java) {
            stack.popCurrentStack()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't pop the root of the back stack")
    }

    @Test
    fun `popCurrentStack in a second stack`() {
        val stack = underTest()
        stack.push(OtherRoot(2), clearTargetStack = false)
        stack.push(SimpleRoute(3))
        stack.popCurrentStack()

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `popCurrentStack in a second stack and then opening that screen again`() {
        val stack = underTest()
        stack.push(OtherRoot(2), clearTargetStack = false)
        stack.push(SimpleRoute(3))
        stack.popCurrentStack()
        stack.push(SimpleRoute(3))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(3)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `pop from the start stack at its root`() {
        val stack = underTest()
        val exception = assertThrows(IllegalStateException::class.java) {
            stack.pop()
        }
        assertThat(exception).hasMessageThat()
            .isEqualTo("Can't navigate back from the root of the start back stack")

        assertThat(removed).isEmpty()
    }

    @Test
    fun `pop from a screen`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.pop()

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `pop from a screen and then opening that screen again`() {
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.pop()
        stack.push(SimpleRoute(2))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), SimpleRoute(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `pop from the root of a second stack`() {
        val stack = underTest()
        stack.push(OtherRoot(2), clearTargetStack = false)
        stack.pop()

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `pop from the root of a second stack and then opening that stack again`() {
        val stack = underTest()
        stack.push(OtherRoot(2), clearTargetStack = false)
        stack.pop()
        stack.push(OtherRoot(2), clearTargetStack = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("102"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("101"))
    }

    @Test
    fun `pop in a second stack`() {
        val stack = underTest()
        stack.push(OtherRoot(2), clearTargetStack = false)
        stack.push(SimpleRoute(3))
        stack.pop()

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("101"), OtherRoot(2)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `pop in a second stack and then opening that screen again`() {
        val stack = underTest()
        stack.push(OtherRoot(2), clearTargetStack = false)
        stack.push(SimpleRoute(3))
        stack.pop()
        stack.push(SimpleRoute(3))

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(3)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

        assertThat(removed).containsExactly(StackEntry.Id("102"))
    }

    @Test
    fun `popUpTo with inclusive false`() {
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

        assertThat(stack.snapshot.value.visibleEntries).hasSize(6)

        stack.popUpTo(simpleRouteDestination.id, isInclusive = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("104"), SimpleRoute(5)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

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

        assertThat(stack.snapshot.value.visibleEntries).hasSize(6)

        stack.popUpTo(simpleRouteDestination.id, isInclusive = true)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("103"), SimpleRoute(4)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isTrue()

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
    fun `popUpTo with root and inclusive`() {
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

        assertThat(stack.snapshot.value.visibleEntries).hasSize(6)

        stack.popUpTo(simpleRootDestination.id, isInclusive = false)

        assertThat(stack.snapshot.value.visibleEntries)
            .containsExactly(
                factory.create(StackEntry.Id("100"), SimpleRoot(1)),
            )
            .inOrder()
        assertThat(stack.snapshot.value.canNavigateBack).isFalse()

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

        assertThat(stack.snapshot.value.visibleEntries).hasSize(6)

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
        val stack = underTest()
        stack.push(SimpleRoute(2))
        stack.push(SimpleRoute(3))
        stack.push(SimpleRoute(4))
        stack.push(SimpleRoute(5))
        stack.push(ThirdRoute(6))
        stack.push(ThirdRoute(7))

        assertThat(stack.snapshot.value.visibleEntries).hasSize(3)

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
}
