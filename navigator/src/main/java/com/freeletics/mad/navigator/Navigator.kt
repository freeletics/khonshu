package com.freeletics.mad.navigator

import androidx.navigation.NavController

/**
 * [Navigator] can be used to encapsulate navigation logic together with a [NavigationHandler].
 *
 * An example usage would be that there is an implementation of [Navigator] that exposes a
 * Kotlin Coroutines `Flow` of navigation events. An implementation of [NavigationHandler]
 * could then `collect` that `Flow` in it's `handle` method and then execute the correct navigation
 * methods on the [NavController] based on the received event.
 *
 * This set up allows to have the general navigation logic (when action x happens navigate to
 * screen y) in your business logic layer, for example as a side effect in your state machine.
 * The business logic would only interact with [Navigator] which does not require a reference
 * to Android framework components. That avoids the risk of leaking an `Activity` or `Fragment` and
 * makes the navigation related logic easily testable on the JVM.
 */
interface Navigator

