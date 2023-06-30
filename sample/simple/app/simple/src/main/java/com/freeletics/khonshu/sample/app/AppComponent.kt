package com.freeletics.khonshu.sample.app

import android.app.Application
import com.freeletics.khonshu.codegen.AppScope
import com.freeletics.khonshu.codegen.ScopeTo
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
