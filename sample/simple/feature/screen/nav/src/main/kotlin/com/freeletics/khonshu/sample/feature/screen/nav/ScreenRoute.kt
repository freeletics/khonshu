package com.freeletics.khonshu.sample.feature.screen.nav

import com.freeletics.khonshu.navigation.NavRoute
import kotlinx.serialization.Serializable

@Serializable
data class ScreenRoute(val number: Int) : NavRoute
