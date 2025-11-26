package com.freeletics.khonshu.codegen.internal

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.reflect.KClass

@InternalCodegenApi
public interface HostGraphProvider {
    public fun <T> provide(scope: KClass<*>): T
}

@InternalCodegenApi
public val LocalHostGraphProvider: ProvidableCompositionLocal<HostGraphProvider> = staticCompositionLocalOf {
    throw IllegalStateException("HostGraphProvider was not provided")
}
