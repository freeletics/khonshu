package com.freeletics.khonshu.sample.app

import android.app.Application
import com.freeletics.khonshu.codegen.AppScope
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.BindsInstance
import dagger.Component

@SingleIn(AppScope::class)
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
