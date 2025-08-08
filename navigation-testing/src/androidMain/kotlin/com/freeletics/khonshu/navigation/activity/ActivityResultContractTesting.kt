package com.freeletics.khonshu.navigation.activity

import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest.PermissionResult

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
