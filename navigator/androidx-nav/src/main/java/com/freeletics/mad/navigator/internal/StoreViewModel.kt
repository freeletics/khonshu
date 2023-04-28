package com.freeletics.mad.navigator.internal

import androidx.lifecycle.ViewModel

internal class StoreViewModel private constructor(
    store: NavigationExecutorStore,
) : ViewModel(store), NavigationExecutor.Store by store {
    constructor() : this(NavigationExecutorStore())
}
