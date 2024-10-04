package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute

public actual class StackEntry<T : BaseRoute> {
    public actual val route: T
        get() = TODO()
}
