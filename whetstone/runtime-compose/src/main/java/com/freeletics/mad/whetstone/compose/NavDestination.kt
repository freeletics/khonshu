package com.freeletics.mad.whetstone.compose

/**
 * This annotation can be used in combination with [ComposeScreen] to
 * enable the integration of a `FragmentNavEventNavigator` into the generated code. The navigator
 * will automatically be set up so that it's ready to handle events.
 *
 * The `NavEventNavigator` subclass is expected to be bound to `NavEventNavigator` and available
 * in the [ComposeScreen.scope] scoped generated component. This can be achieved by adding
 * `@ContributesBinding(TheScope::class, NavEventNavigator::class)` to it.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NavDestination
