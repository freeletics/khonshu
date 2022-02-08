package com.freeletics.mad.navigator

import android.os.Parcelable

/**
 * Represents the route to a destination.
 */
public interface NavRoute : Parcelable {
    /**
     * The destination this route leads to.
     */
    public val destinationId: Int
}
