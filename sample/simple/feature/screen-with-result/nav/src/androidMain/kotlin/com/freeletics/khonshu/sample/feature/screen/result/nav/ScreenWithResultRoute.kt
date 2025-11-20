package com.freeletics.khonshu.sample.feature.screen.result.nav

import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResultRequest
import kotlinx.serialization.Serializable

@Serializable
data class ScreenWithResultRoute(val key: NavigationResultRequest.Key<Result>) : NavRoute

@Serializable
data class Result(val data: String)
