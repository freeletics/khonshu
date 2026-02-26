package com.freeletics.khonshu.navigation.deeplinks

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import dev.drewhamilton.poko.Poko

/**
 * Represents information on how the app was launched in the form of either a list of routes
 * or a uri. This information can then be used for deep linking.
 */
@Poko
public class LaunchInfo(
    public val routes: List<BaseRoute>?,
    public val uri: Uri?,
)

@Composable
internal expect fun obtainLaunchInfo(destinations: Set<NavDestination<*>>): LaunchInfo
