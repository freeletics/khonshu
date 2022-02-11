package com.freeletics.mad.navigator

import android.os.Parcelable

/**
 * This is similar to a [NavRoute] but represents the route to the start destination used in
 * a backstack. When you navigate to a [NavRoot] the current backstack is saved and removed
 * so that the [NavRoot] is right on top of the start destination.
 *
 * When the implementing class is [Parcelable], the instance of route will be put into the
 * navigation arguments and is then available to the target screens.
 */
public interface NavRoot
