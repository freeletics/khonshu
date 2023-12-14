package com.freeletics.khonshu.navigation.androidx.internal

import android.os.Bundle
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.EXTRA_ROUTE
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi

@InternalNavigationApi
public fun <T : BaseRoute> Bundle?.requireRoute(): T {
    requireNotNull(this) {
        "Bundle is null. Can't extract Route data."
    }
    @Suppress("DEPRECATION")
    return requireNotNull(getParcelable(EXTRA_ROUTE)) {
        "Bundle doesn't contain Route data in \"$EXTRA_ROUTE\""
    }
}

@InternalNavigationApi
public fun BaseRoute.getArguments(): Bundle = Bundle().also {
    it.putParcelable(EXTRA_ROUTE, this)
}

@InternalNavigationApi
public fun ActivityRoute.getArguments(): Bundle = Bundle().also {
    it.putParcelable(EXTRA_FILL_IN_INTENT, fillInIntent())
}

internal const val EXTRA_FILL_IN_INTENT: String = "com.freeletics.khonshu.navigation.FILL_IN_INTENT"
