package com.freeletics.khonshu.navigation

public sealed interface BaseRoute

/**
 * Represents the route to a destination.
 *
 * The instance of this will be put into the navigation arguments as a [kotlinx.serialization.Serializable] and is then
 * available to the target screens.
 */
public interface NavRoute : BaseRoute

/**
 * This is similar to a [NavRoute] but represents the route to the start destination used in
 * a backstack. When you navigate to a [NavRoot] the current backstack is saved and removed
 * so that the [NavRoot] is right on top of the start destination.
 *
 * The instance of this will be put into the navigation arguments as a [kotlinx.serialization.Serializable] and is then
 * available to the target screens.
 */
public interface NavRoot : BaseRoute
