package com.freeletics.mad.navigator

import androidx.activity.result.contract.ActivityResultContract

/**
 * A marker interface for anything that can be passed into [NavEventNavigator.navigateForResult]
 * to launch a result request.
 *
 * [I] is the type of input that needs to be provided at the time of launching.
 */
public sealed interface ResultLauncher<I>

/**
 * Class returned from [NavEventNavigator.registerForActivityResult].
 *
 * This class has two purposes:
 *  - It exposes a [results] `Flow` that can be used to observe incoming results
 *  - It can be passed to [NavEventNavigator.navigateForResult] to trigger the execution of a result
 *    request
 */
public class ActivityResultRequest<I, O> internal constructor(
    public val contract: ActivityResultContract<I, O>
) : ResultOwner<O>(), ResultLauncher<I>

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
    ResultOwner<Map<String, PermissionsResultRequest.PermissionResult>>(),
    ResultLauncher<List<String>> {

    /**
     * The status of the requested permission.
     */
    public enum class PermissionResult {
        GRANTED,
        DENIED,
        DENIED_PERMANENTLY
    }
}
