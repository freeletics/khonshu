package com.freeletics.khonshu.navigation.internal

/**
 * Code marked with [InternalNavigationApi] has no guarantees about API stability and can be changed
 * at any time.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
public annotation class InternalNavigationApi

/**
 * Code marked with [InternalNavigationCodegenApi] has no guarantees about API stability and can be changed
 * at any time.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
public annotation class InternalNavigationCodegenApi

/**
 * Code marked with [InternalNavigationTestingApi] has no guarantees about API stability and can be changed
 * at any time.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
public annotation class InternalNavigationTestingApi
