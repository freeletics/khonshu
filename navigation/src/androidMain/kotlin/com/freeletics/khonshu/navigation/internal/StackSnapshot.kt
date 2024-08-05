package com.freeletics.khonshu.navigation.internal

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import com.freeletics.khonshu.navigation.BaseRoute
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
@InternalNavigationCodegenApi
public class StackSnapshot internal constructor(
    @get:VisibleForTesting
    internal val entries: List<StackEntry<*>>,
    private val startStackRootEntry: StackEntry<*>,
) {
    private var firstVisibleIndex: Int = -1

    internal val root: StackEntry<*>
        get() = entries.first()

    internal val current: StackEntry<*>
        get() = entries.last()

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
}
