package com.freeletics.mad.navigator

import android.os.Parcelable

/**
 * This is similar to a [NavRoute] but represents the route to the start destination used in
 * a backstack. When you navigate to a [NavRoot] the current backstack is saved and removed
 * so that the [NavRoot] is right on top of the start destination.
 */
public interface NavRoot : Parcelable {
    /**
     * The destination this route leads to.
     */
    public val destinationId: Int
}
