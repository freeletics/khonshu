package com.freeletics.mad.navigation.fragment

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigation.BaseRoute
import com.freeletics.mad.navigation.internal.AndroidXNavigationExecutor
import com.freeletics.mad.navigation.internal.InternalNavigationApi
import com.freeletics.mad.navigation.internal.NavigationExecutor
import com.freeletics.mad.navigation.internal.requireRoute

public fun <T : BaseRoute> Fragment.requireRoute(): T = requireArguments().requireRoute()

@InternalNavigationApi
public fun Fragment.findNavigationExecutor(): NavigationExecutor {
    return AndroidXNavigationExecutor(findNavController())
}
