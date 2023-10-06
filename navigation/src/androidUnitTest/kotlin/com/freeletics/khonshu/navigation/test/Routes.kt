package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.InternalActivityRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.internal.Parcelable
import dev.drewhamilton.poko.Poko
import kotlinx.parcelize.Parcelize

@Poko
@Parcelize
internal class SimpleRoute(val number: Int) : NavRoute

@Poko
@Parcelize
internal class OtherRoute(val number: Int) : NavRoute

@Poko
@Parcelize
internal class DeepLinkRoute(
    val pathParameters: Map<String, String>,
    val queryParameters: Map<String, String>,
) : NavRoute

@Poko
@Parcelize
internal class SimpleRoot(val number: Int) : NavRoot

@Poko
@Parcelize
internal class SimpleActivity(val number: Int) : InternalActivityRoute()

@Poko
@Parcelize
internal class TestParcelable(val value: Int) : Parcelable
