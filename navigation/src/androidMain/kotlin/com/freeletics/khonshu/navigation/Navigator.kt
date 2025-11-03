package com.freeletics.khonshu.navigation

import android.os.Parcelable
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

public interface Navigator {
    /**
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: NavRoute)

    /**
     * Triggers up navigation.
     */
    public fun navigateUp()

    /**
     * Removes the top entry from the backstack to show the previous destination.
     */
    public fun navigateBack()

    /**
     * Removes all entries from the backstack until [T]. If [inclusive] is
     * `true` [T] itself will also be removed.
     */
    public fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean = false)

    /**
     * Show the existing back stack for [root] or create a new back stack if none exists. If an existing back stack
     * is shown it will be shown in it's previous state. If [root] is the root of the current back stack this is a
     * no-op.
     *
     * The back stack that is shown at the time of calling this method will not back stack will not be modified.
     */
    public fun switchBackStack(root: NavRoot)

    /**
     * Show [root] as the current destination. If there already is a back stack for [root], regardless of whether
     * it is the current back stack or not, the stack will be cleared.
     *
     * The back stack that is shown at the time of calling this method, if it isn't the back stack of [root],
     * will not back stack will not be modified.
     */
    public fun showRoot(root: NavRoot)

    /**
     * Remove all back stacks and create a new back stack with the given [root].
     *
     * This should only be used when changing the start destination of the app. For all other cases [showRoot] should
     * be used.
     */
    public fun replaceAllBackStacks(root: NavRoot)

    @InternalNavigationApi
    public fun getTopEntryFor(destinationId: DestinationId<*>): StackEntry<*>

    @InternalNavigationApi
    public fun getEntryFor(id: StackEntry.Id): StackEntry<*>

    public companion object {
        /**
         * Removes all entries from the backstack until [T]. If [inclusive] is
         * `true` [T] itself will also be removed.
         */
        public inline fun <reified T : NavRoute> Navigator.navigateBackTo(inclusive: Boolean = false) {
            navigateBackTo(T::class, inclusive)
        }
    }
}

public interface ResultNavigator {
    /**
     * Delivers the [result] to the destination that created [key].
     */
    public fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O)

    /**
     * Register for receiving navigation results that were delivered through
     * [deliverNavigationResult]. [T] is expected to be the [BaseRoute] to the current destination.
     *
     * The returned [NavigationResultRequest] has a [NavigationResultRequest.Key]. This `key` should
     * be passed to the target destination which can then use it to call [deliverNavigationResult].
     */
    @InternalNavigationApi
    public fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O>

    public companion object {
        /**
         * Register for receiving navigation results that were delivered through
         * [deliverNavigationResult]. [T] is expected to be the [BaseRoute] to the current destination.
         *
         * The returned [NavigationResultRequest] has a [NavigationResultRequest.Key]. This `key` should
         * be passed to the target destination which can then use it to call [deliverNavigationResult].
         */
        public inline fun <reified T : BaseRoute, reified O : Parcelable> ResultNavigator.registerForNavigationResult():
            NavigationResultRequest<O> {
            return registerForNavigationResultInternal(DestinationId(T::class), O::class.qualifiedName!!)
        }
    }
}

public interface BackInterceptor {
    /**
     * Returns a [Flow] that will emit [Unit] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    public fun backPresses(): Flow<Unit> = backPresses(Unit)

    /**
     * Returns a [Flow] that will emit [value] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    public fun <T> backPresses(value: T): Flow<T>
}
