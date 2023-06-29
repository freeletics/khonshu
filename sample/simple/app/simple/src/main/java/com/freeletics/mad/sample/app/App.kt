package com.freeletics.mad.sample.app

import android.app.Application
import com.freeletics.mad.codegen.AppScope

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
