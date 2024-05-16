package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Immutable
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.OverlayDestination
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
@InternalNavigationCodegenApi
public class StackSnapshot internal constructor(
    private val entries: List<StackEntry<*>>,
    private val startStack: Boolean,
) {
    private var firstVisibleIndex: Int = -1

    internal val root: StackEntry<*>
        get() = entries.first()

    internal val current: StackEntry<*>
        get() = entries.last()

    internal val canNavigateBack
        get() = !startStack || entries.last().removable

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
            it.destination !is OverlayDestination<*>
        }
    }

    @Suppress("UNCHECKED_CAST")
    @InternalNavigationCodegenApi
    public fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T> {
        return entries.lastOrNull { it.destinationId == destinationId } as StackEntry<T>?
            ?: throw IllegalStateException("Route $destinationId not found on back stack")
    }
}
