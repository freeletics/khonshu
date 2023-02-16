package com.freeletics.mad.navigator.compose.test

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
