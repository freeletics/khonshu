package com.freeletics.khonshu.navigation

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.NavigationResultRequest
import com.freeletics.khonshu.navigation.NavigationResultRequest as Request
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.parcelize.Parcelize

/**
 * Register for receiving navigation results.
 *
 * The returned [NavigationResultRequest] has a [NavigationResultRequest.Key]. This `key` should
 * be passed to different destinations which can then use it to call [deliverNavigationResult]. A
 * result passed to `deliverNavigationResult` will then be emitted by the `Flow` returned from
 * [NavigationResultRequest.results].
 */
public inline fun <reified T : BaseRoute, reified O : Parcelable> Navigator.registerForNavigationResult(): Request<O> {
    return registerForNavigationResult(
        id = DestinationId(T::class),
        resultType = O::class.qualifiedName!!,
    )
}

@PublishedApi
internal fun <T : BaseRoute, O : Parcelable> Navigator.registerForNavigationResult(
    id: DestinationId<T>,
    resultType: String,
): Request<O> {
    val requestKey = "${id.route.qualifiedName!!}-$resultType"
    val entry = getTopEntryFor(id)
    val key = Request.Key<O>(entry.id, requestKey)
    return Request(key, entry.savedStateHandle)
}

/**
 * Delivers the [result] to the destination that created [key].
 *
 * See [registerForNavigationResult].
 */
public fun <O : Parcelable> Navigator.deliverNavigationResult(
    key: Request.Key<O>,
    result: O,
) {
    val entry = getEntryFor(key.stackEntryId)
    entry.savedStateHandle[key.requestKey] = NavigationResult(result)
}

/**
 * Class returned from [registerForNavigationResult].
 *
 * The [key] can be passed to other destinations that can then call [deliverNavigationResult] with it
 * to deliver a `result` [R] that will then be emitted by [results].
 */
public class NavigationResultRequest<R : Parcelable> @InternalNavigationTestingApi constructor(
    public val key: Key<R>,
    @property:InternalNavigationTestingApi
    public val savedStateHandle: SavedStateHandle,
) {
    /**
     * Emits any result that was passed to [deliverNavigationResult] with the matching [key].
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
