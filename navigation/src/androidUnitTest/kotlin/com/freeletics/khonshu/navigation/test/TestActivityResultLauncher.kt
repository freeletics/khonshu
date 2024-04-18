package com.freeletics.khonshu.navigation.test

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import app.cash.turbine.Turbine

internal class TestActivityResultLauncher : ActivityResultLauncher<Any>() {

    val launched = Turbine<Any?>()

    override val contract: ActivityResultContract<Any, *>
        get() = throw UnsupportedOperationException()

    override fun launch(input: Any, options: ActivityOptionsCompat?) {
        launched.add(input)
    }

    override fun unregister() {
        throw UnsupportedOperationException()
    }
}
