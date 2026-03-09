@file:Suppress("ktlint:standard:filename")
package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable
import com.freeletics.khonshu.navigation.activity.ActivityNavigator
import com.freeletics.khonshu.navigation.activity.ActivityNavigatorEffect

public actual typealias PlatformNavigator = ActivityNavigator

@Composable
public actual fun PlatformNavigatorEffect(navigator: PlatformNavigator) {
    ActivityNavigatorEffect(navigator)
}
