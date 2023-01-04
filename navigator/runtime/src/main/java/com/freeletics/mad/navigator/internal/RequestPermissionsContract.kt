package com.freeletics.mad.navigator.internal

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale as shouldShowRationale
import android.annotation.SuppressLint
import com.freeletics.mad.navigator.PermissionsResultRequest.PermissionResult

/**
 * Wrapper for [ActivityResultContracts.RequestMultiplePermissions] so that a [List] is used
 * as input instead of [Array]. The reason for this is that [Array.equals] does not compare
 * the contents which makes testing [com.freeletics.mad.navigator.NavEvent.ActivityResultEvent]
 * painful.
 */
@InternalNavigatorApi
public class RequestPermissionsContract :
    ActivityResultContract<List<String>, Map<String, Boolean>>() {

    private val contract = ActivityResultContracts.RequestMultiplePermissions()

    override fun createIntent(context: Context, input: List<String>): Intent {
        return contract.createIntent(context, input.toTypedArray())
    }

    override fun getSynchronousResult(
        context: Context,
        input: List<String>
    ): SynchronousResult<Map<String, Boolean>>? {
        return contract.getSynchronousResult(context, input.toTypedArray())
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> {
        return contract.parseResult(resultCode, intent)
    }

    internal companion object {
        @SuppressLint("VisibleForTests") // VisibleForTests(otherwise = INTERNAL) does not exist
        internal fun enrichResult(
            activity: Activity,
            resultMap: Map<String, Boolean>
        ): Map<String, PermissionResult> {
            return resultMap.mapValues { (permission, granted) ->
                if (granted) {
                    PermissionResult.Granted
                } else {
                    PermissionResult.Denied(shouldShowRationale(activity, permission))
                }
            }
        }
    }
}
