package com.freeletics.khonshu.sample.app

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.createGraphFactory

class App : Application() {
    private val component by lazy {
        createGraphFactory<AppComponent.Factory>().create(this)
    }

    override fun getSystemService(name: String): Any? {
        if (name == AppScope::class.qualifiedName) {
            return component
        }
        return super.getSystemService(name)
    }
}
