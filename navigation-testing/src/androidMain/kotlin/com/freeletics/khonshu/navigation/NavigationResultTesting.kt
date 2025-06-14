package com.freeletics.khonshu.navigation

import androidx.savedstate.serialization.encodeToSavedState
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import kotlinx.serialization.serializer

/**
 * Send a fake result to collectors of this request. Can be used to test the result handling
 * logic.
 */
public inline fun <reified R> NavigationResultRequest<R>.sendResult(result: R) {
    val serializer = NavigationResult.serializer(serializer<R>())
    savedStateHandle[key.requestKey] = encodeToSavedState(serializer, NavigationResult(result))
}

/**
 * Creates a [NavigationResultRequest.Key] for testing purposes. This should only be used for
 * testing the result sender that retrieves a key as part of its argument.
 *
 * To test the result receiver use a real key from a request obtained by
 * [registerForNavigationResult].
 */
@OptIn(InternalNavigationCodegenApi::class)
public inline fun <reified R> fakeNavigationResultKey(): NavigationResultRequest.Key<R> {
    return NavigationResultRequest.Key(StackEntry.Id(""), R::class.qualifiedName!!)
}
