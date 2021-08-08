package com.freeletics.mad.whetstone

import javax.inject.Qualifier
import kotlin.reflect.KClass

/**
 * A qualifier that should be used to provide a resource id that represents an androidx.navigation
 * destination. This navigation id will be the one that a [NavEntryComponent], that uses the same
 * scope as the given [value], is tied to.
 *
 * It is recommended to put [NavEntryComponent] onto the same method that uses this qualifier
 * annotation.
 */
@Qualifier
annotation class NavEntryId(val value: KClass<*>)
