package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute

public expect class StackEntry<T : BaseRoute> {
    public val route: T
}
