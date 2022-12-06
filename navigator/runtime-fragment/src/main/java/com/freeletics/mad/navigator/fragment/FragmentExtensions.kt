package com.freeletics.mad.navigator.fragment

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.internal.AndroidXNavigationExecutor
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.NavigationExecutor
import com.freeletics.mad.navigator.internal.requireRoute

public fun <T : BaseRoute> Fragment.requireRoute(): T = requireArguments().requireRoute()

@InternalNavigatorApi
public fun Fragment.findNavigationExectuor(): NavigationExecutor {
    return AndroidXNavigationExecutor(findNavController())
}
