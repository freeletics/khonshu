package com.freeletics.sample.screen.nav

import com.freeletics.khonshu.navigation.NavRoute
import kotlinx.serialization.Serializable

@Serializable
data class ScreenRoute(val number: Int) : NavRoute
