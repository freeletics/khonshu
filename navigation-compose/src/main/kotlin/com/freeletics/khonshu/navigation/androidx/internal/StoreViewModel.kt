package com.freeletics.khonshu.navigation.androidx.internal

import androidx.lifecycle.ViewModel
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import com.freeletics.khonshu.navigation.internal.NavigationExecutorStore

internal class StoreViewModel private constructor(
    store: NavigationExecutorStore,
) : ViewModel(store), NavigationExecutor.Store by store {
    constructor() : this(NavigationExecutorStore())
}
