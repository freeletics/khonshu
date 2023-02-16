package com.freeletics.mad.navigator.compose.test

import android.os.Parcelable
import com.freeletics.mad.navigator.ExternalActivityRoute
import com.freeletics.mad.navigator.InternalActivityRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
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
internal class ThirdRoute(val number: Int) : NavRoute

@Poko
@Parcelize
internal class SimpleRoot(val number: Int) : NavRoot

@Poko
@Parcelize
internal class OtherRoot(val number: Int) : NavRoot

@Poko
@Parcelize
internal class SimpleActivity(val number: Int) : InternalActivityRoute()

@Poko
@Parcelize
internal class OtherActivity(val number: Int) : ExternalActivityRoute

@Poko
@Parcelize
internal class TestParcelable(val value: Int) : Parcelable
