package com.freeletics.khonshu.navigation.internal

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@OptIn(ExperimentalNativeApi::class)
internal actual typealias WeakReference<T> = WeakReference<T>
