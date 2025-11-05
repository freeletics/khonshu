package com.freeletics.khonshu.navigation.activity

import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.activity.internal.RequestPermissionsContract
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@InternalNavigationTestingApi
public sealed class ActivityResultContractRequest<I, O, R> {
    internal abstract val contract: ActivityResultContract<I, O>

    private val _results = Channel<R>(capacity = Channel.UNLIMITED)

    /**
     * Emits any result received by the [ActivityResultContract].
     *
     * Results will only be delivered to one collector at a time.
     */
    public val results: Flow<R> = _results.receiveAsFlow()

    /**
     * Deliver a new [result] to [results]. This method should be called by [ActivityNavigatorEffect].
     */
    @InternalNavigationTestingApi
    public fun onResult(result: R) {
        val channelResult = _results.trySendBlocking(result)
        check(channelResult.isSuccess)
    }
}

/**
 * Class returned from [ActivityNavigator.registerForActivityResult].
 *
 * This class has two purposes:
 *  - It exposes a [results] `Flow` that can be used to observe incoming results
 *  - It can be passed to [ActivityNavigator.navigateForResult] to trigger the execution of a result
 *    request
 */
public class ActivityResultRequest<I, O> internal constructor(
    override val contract: ActivityResultContract<I, O>,
) : ActivityResultContractRequest<I, O, O>()

/**
 * Class returned from [ActivityNavigator.registerForPermissionsResult].
 *
 * This class has two purposes:
 *  - It exposes a `results` `Flow` that can be used to observe incoming permission results
 *  - It can be passed to [ActivityNavigator.requestPermissions] to trigger the execution of a
 *    permissions request
 *
 *  This provides extra functionality over [ActivityResultRequest] by also checking
 *  [android.app.Activity.shouldShowRequestPermissionRationale] for each permission that was denied.
 *  It returns a [PermissionResult] instead of just a simple boolean to also provide information
 *  about the whether a permission rationale should be shown.
 */
public class PermissionsResultRequest internal constructor() :
    ActivityResultContractRequest<List<String>, Map<String, Boolean>, Map<String, PermissionResult>>() {
        override val contract: RequestPermissionsContract = RequestPermissionsContract()

        /**
         * The status of the requested permission.
         */
        public sealed interface PermissionResult {
            /**
             * The app has access to the requested permission.
             */
            public data object Granted : PermissionResult

            /**
             * The app doesn't have access to the requested permission for one of the reasons requested
             * below. The provided [shouldShowRationale] will be `true` if the system suggests showing
             * the user an explanation of why the permission is requested before attempting to request
             * again.
             *
             * **Reasons:**
             * - the user denied the request
             *     - `shouldShowRationale` is `true`
             * - Android 10 and below: the user selected to not be asked again
             *     - `shouldShowRationale` is `false`
             * - Android 11+: the user denied the request twice already and the system won't ask again
             *     - `shouldShowRationale` is `false`
             * - Android 11+: the user dismissed the notification request without making a choice
             *     - `shouldShowRationale` is `false` if the user did not deny the permission before
             *     - `shouldShowRationale` is `true` if the user denied the permission before
             *
             * Until Android 11 `shouldShowRationale` being `false` can be interpreted as the permission
             * being denied forever. However on Android 11+ there is the edge case the user can dismiss
             * the prompt without making a choice.
             */
            @Poko
            public class Denied(
                public val shouldShowRationale: Boolean,
            ) : PermissionResult
        }
    }
