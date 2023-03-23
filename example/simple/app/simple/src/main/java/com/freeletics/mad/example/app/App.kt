package com.freeletics.mad.example.app

import android.app.Application
import com.freeletics.mad.whetstone.AppScope

class App : Application() {
    private var component: AppComponent? = null

    override fun getSystemService(name: String): Any? {
        if (name == AppScope::class.qualifiedName) {
            return component()
        }
        return super.getSystemService(name)
    }

    private fun component(): AppComponent {
        var component = this.component
        if (component == null) {
            component = AppComponent.factory().create(this)
            this.component = component
        }
        return component
    }
}
