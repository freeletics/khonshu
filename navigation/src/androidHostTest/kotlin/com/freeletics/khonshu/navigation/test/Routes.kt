package com.freeletics.khonshu.navigation.test

import android.content.Context
import android.content.Intent
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.activity.ExternalActivityRoute
import com.freeletics.khonshu.navigation.activity.InternalActivityRoute
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable

@Poko
@Serializable
internal class SimpleRoute(val number: Int) : NavRoute

@Poko
@Serializable
internal class OtherRoute(val number: Int) : NavRoute

@Poko
@Serializable
internal class ThirdRoute(val number: Int) : NavRoute

@Poko
@Serializable
internal class FourthRoute(val number: Int) : NavRoute

@Poko
@Serializable
internal class DeepLinkRoute(
    val pathParameters: Map<String, String>,
    val queryParameters: Map<String, String>,
) : NavRoute

@Poko
@Serializable
internal class SimpleRoot(val number: Int) : NavRoot

@Poko
@Serializable
internal class OtherRoot(val number: Int) : NavRoot

@Poko
@Serializable
internal class SimpleActivity(val number: Int) : InternalActivityRoute() {
    override fun buildIntent(context: Context): Intent = Intent()
}

@Poko
@Serializable
internal class OtherActivity(val number: Int) : ExternalActivityRoute {
    override fun buildIntent(context: Context): Intent = Intent()
}

@Poko
@Serializable
internal class TestParcelable(val value: Int)
