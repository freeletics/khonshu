package com.freeletics.khonshu.sample.app

import android.app.Application
import com.freeletics.khonshu.codegen.AppScope

class App : Application() {
    override fun getSystemService(name: String): Any? {
        if (name == AppScope::class.qualifiedName) {
            return component
        }
        return super.getSystemService(name)
    }

    private val component by lazy {
        AppComponent.factory().create(this)
    }
}
