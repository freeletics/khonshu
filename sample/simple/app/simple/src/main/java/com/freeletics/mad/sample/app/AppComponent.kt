package com.freeletics.mad.sample.app

import android.app.Application
import com.freeletics.mad.whetstone.AppScope
import com.freeletics.mad.whetstone.ScopeTo
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component

@ScopeTo(AppScope::class)
@MergeComponent(scope = AppScope::class)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

    companion object {
        fun factory(): Factory {
            return DaggerAppComponent.factory()
        }
    }
}
