package com.freeletics.mad.navigator

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import androidx.core.app.ActivityCompat
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.GRANTED
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

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
 *  It returns a [PermissionResult] instead of just a simple boolean. This allows to detect
 *  permanent denials.
 */
public class PermissionsResultRequest internal constructor() :
    ResultOwner<Map<String, PermissionResult>>() {

    @InternalNavigatorApi
    public fun handleResult(resultMap: Map<String, Boolean>, activity: Activity) {
        val result = resultMap.mapValues { (permission, granted) ->
            when {
                granted -> GRANTED
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> DENIED
                else -> DENIED_PERMANENTLY
            }
        }
        onResult(result)
    }

    /**
     * The status of the requested permission.
     */
    public enum class PermissionResult {
        GRANTED,
        DENIED,
        DENIED_PERMANENTLY
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
