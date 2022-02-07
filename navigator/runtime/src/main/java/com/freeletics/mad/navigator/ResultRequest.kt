package com.freeletics.mad.navigator

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult.GRANTED
import com.freeletics.mad.navigator.internal.InternalNavigatorApi

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
                shouldShowRequestPermissionRationale(activity, permission) -> DENIED
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
