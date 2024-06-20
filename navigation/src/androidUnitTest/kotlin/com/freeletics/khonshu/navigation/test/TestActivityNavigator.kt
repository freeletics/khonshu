package com.freeletics.khonshu.navigation.test

import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.khonshu.navigation.ActivityNavigator
import com.freeletics.khonshu.navigation.ActivityResultRequest
import com.freeletics.khonshu.navigation.PermissionsResultRequest

internal class TestActivityNavigator : ActivityNavigator() {
    fun <I, O> testRegisterForActivityResult(contract: ActivityResultContract<I, O>): ActivityResultRequest<I, O> {
        return registerForActivityResult(contract)
    }

    fun testRegisterForPermissionResult(): PermissionsResultRequest {
        return registerForPermissionsResult()
    }
}
