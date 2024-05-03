package com.freeletics.khonshu.navigation

import android.os.Parcelable
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
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: ActivityRoute)

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
}

public interface ActivityResultNavigator {
    /**
     * Launches the given [request].
     */
    public fun navigateForResult(request: ActivityResultRequest<Void?, *>)

    /**
     * Launches the given [request] with the given [input].
     */
    public fun <I> navigateForResult(request: ActivityResultRequest<I, *>, input: I)

    /**
     * Launches the [request] for the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     */
    public fun requestPermissions(request: PermissionsResultRequest, vararg permissions: String)

    /**
     * Launches the [request] for the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     */
    public fun requestPermissions(request: PermissionsResultRequest, permissions: List<String>)
}

public interface BackInterceptor {
    /**
     * Returns a [Flow] that will emit [Unit] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    public fun backPresses(): Flow<Unit>

    /**
     * Returns a [Flow] that will emit [value] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    public fun <T> backPresses(value: T): Flow<T>
}
