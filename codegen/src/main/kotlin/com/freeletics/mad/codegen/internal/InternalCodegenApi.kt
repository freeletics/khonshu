package com.freeletics.mad.codegen.internal

/**
 * Marks runtime APIs as well as generated code that should only be used by other generated code.
 * Code marked with [InternalCodegenApi] has no guarantees about API stability and can be changed
 * at any time.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
public annotation class InternalCodegenApi
