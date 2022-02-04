package com.freeletics.mad.navigator.fragment

import com.freeletics.mad.navigator.ResultOwner

/**
 * Class that exposes a [results] [Flow] that can be used to observe incoming fragment results for
 * the given [requestKey].
 *
 * See [ResultOwner] and
 * [com.freeletics.mad.navigator.fragment.FragmentNavEventNavigator.registerForFragmentResult].
 */
public class FragmentResultRequest<O> internal constructor(
    internal val requestKey: String
) : ResultOwner<O>()
