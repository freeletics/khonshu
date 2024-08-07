package com.freeletics.khonshu.navigation

import android.os.Parcelable
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

public interface Navigator {
    /**
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: NavRoute)

    /**
     * Triggers navigation to the given [root]. The current back stack will be removed
     * and saved. Whether the backstack of the given `root` is restored depends on
     * [restoreRootState].
     */
    public fun navigateToRoot(
        root: NavRoot,
        restoreRootState: Boolean = false,
    )

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
     * Reset the back stack to the given [root]. The current back stack will cleared and if
     * root was already on it it will be recreated.
     */
    public fun resetToRoot(root: NavRoot)

    /**
     * Replace the current back stack with the given [root].
     * The current back stack will cleared and the given [root] will be recreated.
     * After this call the back stack will only contain the given [root].
     *
     * This differs from [resetToRoot] in that [resetToRoot] does not pop the start route (exclusive)
     * whereas this does.
     */
    public fun replaceAll(root: NavRoot)

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
