package com.freeletics.mad.navigator.internal

import androidx.lifecycle.ViewModel
import java.io.Closeable
import kotlin.reflect.KClass

internal class StoreViewModel private constructor(
    store: NavigationExecutorStore
) : ViewModel(store), NavigationExecutor.Store by store {
    constructor() : this(NavigationExecutorStore())
}
