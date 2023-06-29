package com.freeletics.mad.navigation.test

import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.mad.navigation.ActivityResultRequest
import com.freeletics.mad.navigation.BaseRoute
import com.freeletics.mad.navigation.NavEventNavigator
import com.freeletics.mad.navigation.NavigationResultRequest
import com.freeletics.mad.navigation.PermissionsResultRequest

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
