package com.freeletics.mad.navigator

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable

public sealed interface BaseRoute : Parcelable

/**
 * Represents the route to a destination.
 *
 * The instance of this will be put into the navigation arguments as a [Parcelable] and is then
 * available to the target screens.
 */
public interface NavRoute : BaseRoute

/**
 * This is similar to a [NavRoute] but represents the route to the start destination used in
 * a backstack. When you navigate to a [NavRoot] the current backstack is saved and removed
 * so that the [NavRoot] is right on top of the start destination.
 *
 * The instance of this will be put into the navigation arguments as a [Parcelable] and is then
 * available to the target screens.
 */
public interface NavRoot : BaseRoute

/**
 * Represents the route to a destination.
 *
 * When the implementing class is [Parcelable], the instance of route will be put into the
 * navigation arguments and is then available to the target screens. Do not do this in case this
 * route leads to a different app.
 */
public interface ActivityRoute {
    public fun fillInIntent(): Intent = EMPTY_INTENT

    public companion object {
        private val EMPTY_INTENT = Intent()
    }
}
