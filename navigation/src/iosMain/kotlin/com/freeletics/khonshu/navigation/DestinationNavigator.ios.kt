package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable

public actual abstract class PlatformNavigator actual constructor()

@Composable
public actual fun PlatformNavigatorEffect(navigator: PlatformNavigator) {}
