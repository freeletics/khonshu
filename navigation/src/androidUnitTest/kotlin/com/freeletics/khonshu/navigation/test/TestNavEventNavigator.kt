package com.freeletics.khonshu.navigation.test

import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.khonshu.navigation.ActivityResultRequest
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.EventNavigationResultRequest
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.navigation.PermissionsResultRequest
import com.freeletics.khonshu.navigation.ResultNavigator.Companion.registerForNavigationResult

internal class TestNavEventNavigator : NavEventNavigator() {
    fun <I, O> testRegisterForActivityResult(contract: ActivityResultContract<I, O>): ActivityResultRequest<I, O> {
        return registerForActivityResult(contract)
    }

    fun testRegisterForPermissionResult(): PermissionsResultRequest {
        return registerForPermissionsResult()
    }

    inline fun <reified I : BaseRoute, reified O : Parcelable>
    testRegisterForNavigationResult(): EventNavigationResultRequest<O> {
        return registerForNavigationResult<I, O>() as EventNavigationResultRequest<O>
    }
}
