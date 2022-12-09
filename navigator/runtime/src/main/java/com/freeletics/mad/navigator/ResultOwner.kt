package com.freeletics.mad.navigator

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.RequestPermissionsContract.Companion.enrichResult
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A base class for anything that exposes a [Flow] of [results]. Results will only be delivered
 * to one collector at a time.
 */
public sealed class ResultOwner<O> {
    private val _results = Channel<O>(capacity = Channel.UNLIMITED)

    /**
     * Emits any result passed to [onResult]. Results will only be delivered
     * to one collector at a time.
     */
    public val results: Flow<O> = flow {
        for (result in _results) {
            emit(result)
        }
    }

    /**
     * Deliver a new [result] to [results]. This method should be called by a
     * `NavEventNavigationHandler`.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public fun onResult(result: O) {
        val channelResult = _results.trySendBlocking(result)
        check(channelResult.isSuccess)
    }
}

/**
 * Class returned from [NavEventNavigator.registerForActivityResult].
 *
 * This class has two purposes:
 *  - It exposes a [results] `Flow` that can be used to observe incoming results
 *  - It can be passed to [NavEventNavigator.navigateForResult] to trigger the execution of a result
 *    request
 */
public class ActivityResultRequest<I, O> internal constructor(
    @property:InternalNavigatorApi public val contract: ActivityResultContract<I, O>
) : ResultOwner<O>() {

    @InternalNavigatorApi
    public fun handleResult(result: O) {
        onResult(result)
    }
}

/**
 * Class returned from [NavEventNavigator.registerForPermissionsResult].
 *
 * This class has two purposes:
 *  - It exposes a [results] `Flow` that can be used to observe incoming permission results
 *  - It can be passed to [NavEventNavigator.requestPermissions] to trigger the execution of a
 *    permissions request
 *
 *  This provides extra functionality over [ActivityResultRequest] by also checking
 *  [android.app.Activity.shouldShowRequestPermissionRationale] for each permission that was denied.
 *  It returns a [PermissionResult] instead of just a simple boolean to also provide information
 *  about the whether a permission rationale should be shown.
 */
public class PermissionsResultRequest internal constructor() :
    ResultOwner<Map<String, PermissionResult>>() {

    @InternalNavigatorApi
    public fun handleResult(resultMap: Map<String, Boolean>, context: Context) {
        val result = enrichResult(context, resultMap)
        onResult(result)
    }

    /**
     * The status of the requested permission.
     */
    public sealed interface PermissionResult {
        /**
         * The app has access to the requested permission.
         */
        public object Granted : PermissionResult

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
         *     - `shouldShowRationale` is `false` if the user dit not deny the permission before
         *     - `shouldShowRationale` is `true` if the user denied the permission before
         *
         * Until Android 11 `shouldShowRationale` being `false` can be interpreted as the permission
         * being denied forever. However on Android 11+ there is the edge case the user can dismiss
         * the prompt without making a choice.
         */
        public class Denied(
            public val shouldShowRationale: Boolean
        ) : PermissionResult
    }
}


/**
 * Class that exposes a [results] [Flow] that can be used to observe results for
 * the given [key].
 *
 * See [ResultOwner] and [NavEventNavigator.registerForNavigationResult].
 */
public class NavigationResultRequest<O : Parcelable> internal constructor(
    public val key: Key<O>
) : ResultOwner<O>() {

    @InternalNavigatorApi
    public fun handleResult(result: O) {
        onResult(result)
    }

    /**
     * Use to identify where the result should be delivered to.
     */
    public data class Key<@Suppress("unused") O : Parcelable> internal constructor(
        @property:InternalNavigatorApi
        public val route: KClass<out BaseRoute>,
        @property:InternalNavigatorApi
        public val requestKey: String
    ) : Parcelable {
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        public constructor(parcel: Parcel) : this(
            (parcel.readSerializable() as Class<out BaseRoute>).kotlin,
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeSerializable(route.java)
            parcel.writeString(requestKey)
        }

        override fun describeContents(): Int {
            return 0
        }

        public companion object CREATOR : Parcelable.Creator<Key<*>> {
            override fun createFromParcel(parcel: Parcel): Key<*> {
                return Key<Parcelable>(parcel)
            }

            override fun newArray(size: Int): Array<Key<*>?> {
                return arrayOfNulls(size)
            }
        }
    }
}
