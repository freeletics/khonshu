package com.freeletics.khonshu.navigation

import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.RequestPermissionsContract
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

/**
 * A base class for anything that exposes a [Flow] of [results]. Results will only be delivered
 * to one collector at a time.
 */
public sealed interface ResultOwner<R> {
    public val results: Flow<R>
}
