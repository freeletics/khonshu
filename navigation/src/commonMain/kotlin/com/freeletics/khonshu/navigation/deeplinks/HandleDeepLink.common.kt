package com.freeletics.khonshu.navigation.deeplinks

import androidx.compose.runtime.Composable
import com.freeletics.khonshu.navigation.NavDestination
import kotlinx.collections.immutable.ImmutableSet

@Composable
internal expect fun obtainLaunchInfo(destinations: ImmutableSet<NavDestination<*>>): LaunchInfo
