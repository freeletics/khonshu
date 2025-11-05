package com.freeletics.khonshu.navigation.internal

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
@InternalNavigationCodegenApi
public class StackSnapshot internal constructor(
    @get:VisibleForTesting
    internal val entries: List<StackEntry<*>>,
    private val startStackRootEntry: StackEntry<out NavRoot>,
) {
    private var firstVisibleIndex: Int = -1

    @Suppress("UNCHECKED_CAST")
    internal val startRoot: StackEntry<out NavRoot>
        get() = startStackRootEntry

    @Suppress("UNCHECKED_CAST")
    internal val root: StackEntry<out NavRoot>
        get() = entries.first() as StackEntry<NavRoot>

    internal val current: StackEntry<*>
        get() = entries.last()

    internal val size: Int
        get() = if (root.id != startStackRootEntry.id) {
            entries.size + 1
        } else {
            entries.size
        }

    internal val previous: StackEntry<*>?
        get() = entries.getOrNull(entries.lastIndex - 1)
            ?: startStackRootEntry.takeIf { current.id != it.id }

    internal val canNavigateBack
        get() = entries.last().removable || startStackRootEntry.destinationId != root.destinationId

    internal inline fun forEachVisibleDestination(block: (StackEntry<*>) -> Unit) {
        if (firstVisibleIndex < 0) {
            computeFirstVisibleDestination()
        }

        for (i in firstVisibleIndex..<entries.size) {
            block(entries[i])
        }
    }

    private fun computeFirstVisibleDestination() {
        firstVisibleIndex = entries.indexOfLast {
            !it.isOverlay
        }
    }

    @Suppress("UNCHECKED_CAST")
    @InternalNavigationCodegenApi
    public fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T> {
        return entries.lastOrNull { it.destinationId == destinationId } as StackEntry<T>?
            ?: startStackRootEntry.takeIf { it.destinationId == destinationId } as StackEntry<T>?
            ?: throw IllegalStateException("Route $destinationId not found on back stack")
    }

    internal fun entryFor(id: StackEntry.Id): StackEntry<*> {
        return entries.lastOrNull { it.id == id }
            ?: startStackRootEntry.takeIf { it.id == id }
            ?: throw IllegalStateException("Entry $id not found on back stack")
    }
}
