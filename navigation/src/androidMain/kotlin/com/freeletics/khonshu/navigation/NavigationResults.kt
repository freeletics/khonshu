package com.freeletics.khonshu.navigation

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.parcelize.Parcelize

/**
 * Class returned from [ResultNavigator.registerForNavigationResult].
 *
 * The [key] can be passed to other destinations that can then call [ResultNavigator.deliverNavigationResult] with it
 * to deliver a `result` [R] that will then be emitted by [results].
 */
public class NavigationResultRequest<R : Parcelable> @InternalNavigationTestingApi constructor(
    public val key: Key<R>,
    @property:InternalNavigationTestingApi
    public val savedStateHandle: SavedStateHandle,
) {
    /**
     * Emits any result that was passed to [ResultNavigator.deliverNavigationResult] with the matching
     * [key].
     *
     * Results will only be delivered to one collector at a time.
     */
    public val results: Flow<R>
        get() = savedStateHandle.getStateFlow<NavigationResult<R>>(key.requestKey, NavigationResult(null))
            .mapNotNull {
                if (it.value != null) {
                    savedStateHandle[key.requestKey] = NavigationResult(null)
                    it.value
                } else {
                    null
                }
            }

    /**
     * Use to identify where the result should be delivered to.
     */
    @Poko
    @Parcelize
    public class Key<R : Parcelable> @InternalNavigationTestingApi constructor(
        internal val stackEntryId: StackEntry.Id,
        @property:InternalNavigationTestingApi
        public val requestKey: String,
    ) : Parcelable
}

@Parcelize
@InternalNavigationTestingApi
public data class NavigationResult<R : Parcelable>(
    val value: R?,
) : Parcelable
