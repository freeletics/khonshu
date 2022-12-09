package com.freeletics.mad.navigator.internal

import android.os.Bundle
import android.os.Parcelable
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.EXTRA_ROUTE

@InternalNavigatorApi
public fun <T : BaseRoute> Bundle?.requireRoute(): T {
    requireNotNull(this) {
        "Bundle is null. Can't extract Route data."
    }
    @Suppress("DEPRECATION")
    return requireNotNull(getParcelable(EXTRA_ROUTE)) {
        "Bundle doesn't contain Route data in \"$EXTRA_ROUTE\""
    }
}

@InternalNavigatorApi
public fun BaseRoute.getArguments(): Bundle = Bundle().also {
    it.putParcelable(EXTRA_ROUTE, this)
}

@InternalNavigatorApi
public fun ActivityRoute.getArguments(): Bundle = Bundle().also {
    it.putParcelable(EXTRA_FILL_IN_INTENT, fillInIntent())
    if (this is Parcelable) {
        it.putParcelable(EXTRA_ROUTE, this)
    }
}

internal const val EXTRA_FILL_IN_INTENT: String = "com.freeletics.mad.navigation.FILL_IN_INTENT"
