package com.freeletics.mad.navigator.test

import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.mad.navigator.ActivityResultRequest
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavEventNavigator
import com.freeletics.mad.navigator.NavigationResultRequest
import com.freeletics.mad.navigator.PermissionsResultRequest

internal class TestNavigator : NavEventNavigator() {
    fun <I, O> testRegisterForActivityResult(contract: ActivityResultContract<I, O>): ActivityResultRequest<I, O> {
        return registerForActivityResult(contract)
    }

    fun testRegisterForPermissionResult(): PermissionsResultRequest {
        return registerForPermissionsResult()
    }

    inline fun <reified I : BaseRoute, reified O : Parcelable>
    testRegisterForNavigationResult(): NavigationResultRequest<O> {
        return registerForNavigationResult<I, O>()
    }
}
