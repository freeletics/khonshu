package com.freeletics.mad.navigator.fragment

import androidx.fragment.app.Fragment
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.internal.requireRoute

public fun <T : BaseRoute> Fragment.requireRoute(): T = requireArguments().requireRoute()
