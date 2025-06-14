package com.freeletics.khonshu.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.freeletics.khonshu.navigation.NavigationResultRequest
import com.freeletics.khonshu.navigation.NavigationResultRequest as Request
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Register for receiving navigation results.
 *
 * The returned [NavigationResultRequest] has a [NavigationResultRequest.Key]. This `key` should
 * be passed to different destinations which can then use it to call [deliverNavigationResult]. A
 * result passed to `deliverNavigationResult` will then be emitted by the `Flow` returned from
 * [NavigationResultRequest.results].
 */
public inline fun <reified T : BaseRoute, reified O> Navigator.registerForNavigationResult(): Request<O> {
    return registerForNavigationResult(
        id = DestinationId(T::class),
        resultType = O::class.qualifiedName!!,
        serializer = serializer(),
    )
}

@PublishedApi
internal fun <T : BaseRoute, O> Navigator.registerForNavigationResult(
    id: DestinationId<T>,
    resultType: String,
    serializer: KSerializer<O>,
): Request<O> {
    val requestKey = "${id.route.qualifiedName!!}-$resultType"
    val entry = getTopEntryFor(id)
    val key = Request.Key<O>(entry.id, requestKey)
    return Request(key, entry.savedStateHandle, serializer)
}

/**
 * Delivers the [result] to the destination that created [key].
 *
 * See [registerForNavigationResult].
 */
public inline fun <reified O> Navigator.deliverNavigationResult(
    key: Request.Key<O>,
    result: O,
) {
    deliverNavigationResult(key, result, serializer())
}

@PublishedApi
internal fun <O> Navigator.deliverNavigationResult(
    key: Request.Key<O>,
    result: O,
    serializer: KSerializer<O>,
) {
    val entry = getEntryFor(key.stackEntryId)
    entry.savedStateHandle[key.requestKey] = encodeToSavedState(
        NavigationResult.serializer(serializer),
        NavigationResult(result),
    )
}

/**
 * Class returned from [registerForNavigationResult].
 *
 * The [key] can be passed to other destinations that can then call [deliverNavigationResult] with it
 * to deliver a `result` [R] that will then be emitted by [results].
 */
public class NavigationResultRequest<R> @InternalNavigationTestingApi constructor(
    public val key: Key<R>,
    @property:InternalNavigationTestingApi
    public val savedStateHandle: SavedStateHandle,
    resultSerializer: KSerializer<R>,
) {
    private val serializer = NavigationResult.serializer(resultSerializer)

    private val emptyValue
        get() = encodeToSavedState(serializer, NavigationResult(null))

    /**
     * Emits any result that was passed to [deliverNavigationResult] with the matching [key].
     *
     * Results will only be delivered to one collector at a time.
     */
    public val results: Flow<R>
        get() = savedStateHandle.getStateFlow(key.requestKey, emptyValue)
            .mapNotNull {
                val decoded = decodeFromSavedState(serializer, it)
                if (decoded.value != null) {
                    savedStateHandle[key.requestKey] = emptyValue
                    decoded.value
                } else {
                    null
                }
            }

    /**
     * Use to identify where the result should be delivered to.
     */
    @Poko
    @Serializable
    public class Key<R> @InternalNavigationTestingApi constructor(
        @PublishedApi
        internal val stackEntryId: StackEntry.Id,
        @property:InternalNavigationTestingApi
        public val requestKey: String,
    )
}

@Serializable
@InternalNavigationTestingApi
public data class NavigationResult<R>(
    val value: R?,
)
