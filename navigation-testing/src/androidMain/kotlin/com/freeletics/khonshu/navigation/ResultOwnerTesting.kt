package com.freeletics.khonshu.navigation

import android.os.Parcelable
import com.freeletics.khonshu.navigation.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi

/**
 * Send a fake result to collectors of this request. Can be used to test the result handling
 * logic.
 */
public fun <O> ActivityResultRequest<*, O>.sendResult(result: O) {
    onResult(result)
}

/**
 * Send a fake result to collectors of this request. Can be used to test the result handling
 * logic.
 */
public fun PermissionsResultRequest.sendResult(permission: String, result: PermissionResult) {
    onResult(mapOf(permission to result))
}

/**
 * Send a fake result to collectors of this request. Can be used to test the result handling
 * logic.
 */
public fun PermissionsResultRequest.sendResult(vararg pairs: Pair<String, PermissionResult>) {
    onResult(mapOf(*pairs))
}

/**
 * Send a fake result to collectors of this request. Can be used to test the result handling
 * logic.
 */
public fun PermissionsResultRequest.sendResult(result: Map<String, PermissionResult>) {
    onResult(result)
}

/**
 * Send a fake result to collectors of this request. Can be used to test the result handling
 * logic.
 */
public fun <R : Parcelable> NavigationResultRequest<R>.sendResult(result: R) {
    savedStateHandle[key.requestKey] = result
}

/**
 * Creates a [NavigationResultRequest.Key] for testing purposes. This should only be used for
 * testing the result sender that retrieves a key as part of its argument.
 *
 * To test the result receiver use a real key from a request obtained by
 * [ResultNavigator.registerForNavigationResult].
 */
@OptIn(InternalNavigationCodegenApi::class)
public inline fun <reified R : Parcelable> fakeNavigationResultKey(): NavigationResultRequest.Key<R> {
    return NavigationResultRequest.Key(DestinationId(NavRoute::class), R::class.qualifiedName!!)
}
