package com.freeletics.khonshu.navigation.test

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.freeletics.khonshu.navigation.ExternalActivityRoute
import com.freeletics.khonshu.navigation.InternalActivityRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import dev.drewhamilton.poko.Poko
import kotlinx.parcelize.Parcelize

@Poko
@Parcelize
internal class SimpleRoute(val number: Int) : NavRoute, Parcelable

@Poko
@Parcelize
internal class OtherRoute(val number: Int) : NavRoute, Parcelable

@Poko
@Parcelize
internal class ThirdRoute(val number: Int) : NavRoute, Parcelable

@Poko
@Parcelize
internal class DeepLinkRoute(
    val pathParameters: Map<String, String>,
    val queryParameters: Map<String, String>,
) : NavRoute, Parcelable

@Poko
@Parcelize
internal class SimpleRoot(val number: Int) : NavRoot, Parcelable

@Poko
@Parcelize
internal class OtherRoot(val number: Int) : NavRoot, Parcelable

@Poko
@Parcelize
internal class SimpleActivity(val number: Int) : InternalActivityRoute() {
    override fun buildIntent(context: Context): Intent = Intent()
}

@Poko
@Parcelize
internal class OtherActivity(val number: Int) : ExternalActivityRoute {
    override fun buildIntent(context: Context): Intent = Intent()
}

@Poko
@Parcelize
internal class TestParcelable(val value: Int) : Parcelable
