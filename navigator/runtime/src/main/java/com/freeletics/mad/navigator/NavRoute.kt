package com.freeletics.mad.navigator

import android.os.Bundle

/**
 * Represents the route to a destination represented by [destinationId]. [getArguments] can
 * optionally be overridden to provide extra information to the destination.
 */
public interface NavRoute {
    /**
     * The destination this route leads to.
     */
    public val destinationId: Int

    /**
     * Arguments provided to the destination. For example an id.
     */
    public fun getArguments(): Bundle = Bundle.EMPTY
}
