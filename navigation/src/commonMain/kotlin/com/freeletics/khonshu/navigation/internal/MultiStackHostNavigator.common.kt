package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.MultiStack
import com.freeletics.khonshu.navigation.internal.StackEntryStoreHolder
import com.freeletics.khonshu.navigation.internal.createMultiStack
import com.freeletics.khonshu.navigation.internal.rememberMultiStack

internal expect class MultiStackHostNavigator(stack: MultiStack) : HostNavigator
