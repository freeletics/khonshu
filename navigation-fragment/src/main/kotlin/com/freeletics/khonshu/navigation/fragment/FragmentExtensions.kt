package com.freeletics.khonshu.navigation.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.internal.AndroidXNavigationExecutor
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import com.freeletics.khonshu.navigation.internal.requireRoute

public fun <T : BaseRoute> Fragment.requireRoute(): T = requireArguments().requireRoute()

@InternalNavigationApi
public fun Fragment.findNavigationExecutor(): NavigationExecutor {
    val navHostFragment = findNavHostFragment(this)
    val viewModel by lazy(LazyThreadSafetyMode.NONE) { ViewModelProvider(navHostFragment)[HandleNavigationViewModel::class.java] }

    return AndroidXNavigationExecutor(
        controller = findNavController(),
        onSaveStartRoute = { viewModel.onSaveStartRoute(it) },
    )
}

/**
 * Find a [NavController] given a local [Fragment].
 *
 * This method will locate the [NavController] associated with this Fragment,
 * looking first for a [NavHostFragment] along the given Fragment's parent chain.
 * If a [NavController] is not found, this method will look for one along this
 * Fragment's [view hierarchy][Fragment.getView] as specified by
 * [Navigation.findNavController].
 *
 * @param fragment the locally scoped Fragment for navigation
 * @return the locally scoped [NavController] for navigating from this [Fragment]
 * @throws IllegalStateException if the given Fragment does not correspond with a
 * [NavHost] or is not within a NavHost.
 */
internal fun findNavHostFragment(fragment: Fragment): NavHostFragment {
    var findFragment: Fragment? = fragment
    while (findFragment != null) {
        if (findFragment is NavHostFragment) {
            return findFragment
        }
        val primaryNavFragment = findFragment.parentFragmentManager
            .primaryNavigationFragment
        if (primaryNavFragment is NavHostFragment) {
            return primaryNavFragment
        }
        findFragment = findFragment.parentFragment
    }

    throw IllegalStateException("Fragment $fragment does not have a parent NavHostFragment")
}
