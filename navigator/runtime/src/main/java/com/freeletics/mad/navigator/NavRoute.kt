package com.freeletics.mad.navigator

import android.os.Parcelable

/**
 * Represents the route to a destination.
 *
 * When the implementing class is [Parcelable], the instance of route will be put into the
 * navigation arguments and is then available to the target screens.
 */
public interface NavRoute {
    /**
     * The destination this route leads to.
     */
    public val destinationId: Int
}
