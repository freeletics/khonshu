@file:Suppress("ktlint:standard:filename")

package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable

public actual abstract class PlatformNavigator

@Composable
public actual fun PlatformNavigatorEffect(navigator: PlatformNavigator) {
}
