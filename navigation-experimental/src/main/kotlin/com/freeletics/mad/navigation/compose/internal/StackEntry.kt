package com.freeletics.mad.navigation.compose.internal

import androidx.compose.runtime.Immutable
import com.freeletics.mad.navigation.BaseRoute
import com.freeletics.mad.navigation.NavRoot
import com.freeletics.mad.navigation.NavRoute
import com.freeletics.mad.navigation.compose.ContentDestination
import com.freeletics.mad.navigation.internal.destinationId
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
internal class StackEntry<T : BaseRoute>(
    val id: Id,
    val route: T,
    val destination: ContentDestination<T>,
) {
    val destinationId
        get() = route.destinationId

    val removable
        // cast is needed for the compiler to recognize that the when is exhaustive
        @Suppress("USELESS_CAST")
        get() = when (route as BaseRoute) {
            is NavRoute -> true
            is NavRoot -> false
        }

    @JvmInline
    value class Id(val value: String)
}
