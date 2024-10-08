package com.freeletics.khonshu.navigation

import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.RequestPermissionsContract
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

/**
 * A base class for anything that exposes a [Flow] of [results]. Results will only be delivered
 * to one collector at a time.
 */
public sealed interface ResultOwner<R> {
    public val results: Flow<R>
}

@InternalNavigationTestingApi
public sealed class ContractResultOwner<I, O, R> : ResultOwner<R> {
    internal abstract val contract: ActivityResultContract<I, O>

    /**
     * Emits any result passed to [onResult]. Results will only be delivered
     * to one collector at a time.
     */
    private val _results = Channel<R>(capacity = Channel.UNLIMITED)

    override val results: Flow<R> = _results.receiveAsFlow()

    /**
     * Deliver a new [result] to [results]. This method should be called by [NavigationSetup].
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
) : ContractResultOwner<I, O, O>()

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
    ContractResultOwner<List<String>, Map<String, Boolean>, Map<String, PermissionResult>>() {
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

/**
 * Class that exposes a [results] [Flow] that can be used to observe results for
 * the given [key].
 *
 * See [ResultOwner] and [ResultNavigator.registerForNavigationResult].
 */
public class NavigationResultRequest<R : Parcelable> @InternalNavigationTestingApi constructor(
    public val key: Key<R>,
    @property:InternalNavigationTestingApi
    public val savedStateHandle: SavedStateHandle,
) : ResultOwner<R> {
    override val results: Flow<R>
        get() = savedStateHandle.getStateFlow<Parcelable>(key.requestKey, InitialValue)
            .mapNotNull {
                if (it != InitialValue) {
                    savedStateHandle[key.requestKey] = InitialValue
                    @Suppress("UNCHECKED_CAST")
                    it as R
                } else {
                    null
                }
            }

    @Parcelize
    private object InitialValue : Parcelable

    /**
     * Use to identify where the result should be delivered to.
     */
    @Poko
    @Parcelize
    public class Key<R : Parcelable> @InternalNavigationTestingApi constructor(
        internal val destinationId: DestinationId<*>,
        @property:InternalNavigationTestingApi
        public val requestKey: String,
    ) : Parcelable {
        private companion object : Parceler<Key<*>> {
            override fun Key<*>.write(parcel: Parcel, flags: Int) {
                parcel.writeSerializable(destinationId.route.java)
                parcel.writeString(requestKey)
            }

            override fun create(parcel: Parcel): Key<*> {
                @Suppress("UNCHECKED_CAST", "DEPRECATION")
                val cls = (parcel.readSerializable() as Class<out BaseRoute>).kotlin
                return Key<Parcelable>(DestinationId(cls), parcel.readString()!!)
            }
        }
    }
}
