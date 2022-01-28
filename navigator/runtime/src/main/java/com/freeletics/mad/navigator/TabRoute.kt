package com.freeletics.mad.navigator

import android.os.Bundle

/**
 * This is similar to a [NavRoute] but represents the route to the start destination used in
 * a backstack. When you navigate to a [TabRoute] the current backstack is saved and removed
 * so that the [TabRoute] is right on top of the start destination.
 *
 * [getArguments] can optionally be overridden to provide extra information to the destination.
 */
public interface TabRoute {
    /**
     * The destination this route leads to.
     */
    public val destinationId: Int

    /**
     * Arguments provided to the destination. For example an id.
     */
    public fun getArguments(): Bundle = Bundle.EMPTY
}