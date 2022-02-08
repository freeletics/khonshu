package com.freeletics.mad.navigator.fragment

import androidx.fragment.app.Fragment
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.internal.toNavRoute

public fun <T : NavRoute> Fragment.requireNavRoute(): T = requireArguments().toNavRoute()

public fun <T : NavRoot> Fragment.requireNavRoot(): T = requireArguments().toNavRoute()
