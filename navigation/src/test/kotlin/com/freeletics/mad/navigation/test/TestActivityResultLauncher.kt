package com.freeletics.mad.navigation.test

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import app.cash.turbine.Turbine

internal class TestActivityResultLauncher : ActivityResultLauncher<Any>() {

    val launched = Turbine<Any?>()

    override fun launch(input: Any?, options: ActivityOptionsCompat?) {
        launched.add(input)
    }

    override fun unregister() {
        throw UnsupportedOperationException()
    }

    override fun getContract(): ActivityResultContract<Any, *> {
        throw UnsupportedOperationException()
    }
}
