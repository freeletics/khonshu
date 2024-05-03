package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Immutable
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
        for (i in entries.indices.reversed()) {
            if (entries[i].destination !is OverlayDestination<*>) {
                firstVisibleIndex = i
                return
            }
        }
    }
}
