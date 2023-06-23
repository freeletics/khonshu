package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.ViewModel

internal class StoreViewModel private constructor(
    store: NavigationExecutorStore,
) : ViewModel(store), NavigationExecutor.Store by store {
    constructor() : this(NavigationExecutorStore())
}