package com.freeletics.mad.navigator.fragment

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.freeletics.mad.navigator.NavEventNavigator

/**
 * Delivers a `Fragment` result to a requester that started this `Fragment` with
 * [NavEventNavigator.navigateForResult].
 */
@VisibleForTesting(otherwise = PACKAGE_PRIVATE)
public data class FragmentResultEvent(
    internal val requestKey: String,
    internal val result: Parcelable
)
