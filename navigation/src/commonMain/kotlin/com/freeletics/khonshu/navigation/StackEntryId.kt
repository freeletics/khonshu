package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * Identifies a single entry on the navigation stack.
 *
 * This id is stable for the lifetime of the stack entry and can be used by destination-scoped
 * APIs to target the exact entry they were created for.
 */
@JvmInline
@Serializable
public value class StackEntryId internal constructor(
    internal val value: String,
)

@OptIn(InternalNavigationTestingApi::class)
internal fun StackEntry.Id.toStackEntryId(): StackEntryId {
    return StackEntryId(value)
}

@OptIn(InternalNavigationTestingApi::class)
internal fun StackEntryId.toInternalStackEntryId(): StackEntry.Id {
    return StackEntry.Id(value)
}
