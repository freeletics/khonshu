package com.freeletics.khonshu.sample.feature.screen.result.nav

import android.os.Parcelable
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResultRequest
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenWithResultRoute(val key: NavigationResultRequest.Key<Result>) : NavRoute

@Parcelize
data class Result(val data: String) : Parcelable
