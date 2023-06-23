package com.freeletics.khonshu.navigation.fragment

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.internal.AndroidXNavigationExecutor
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import com.freeletics.khonshu.navigation.internal.requireRoute

public fun <T : BaseRoute> Fragment.requireRoute(): T = requireArguments().requireRoute()

@InternalNavigationApi
public fun Fragment.findNavigationExecutor(): NavigationExecutor {
    return AndroidXNavigationExecutor(findNavController())
}
