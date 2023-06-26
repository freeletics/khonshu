package com.freeletics.mad.codegen.internal

@InternalCodegenApi
public interface NavDestinationComponent {
    public val navEntryComponentGetters: @JvmSuppressWildcards Map<Class<*>, NavEntryComponentGetter>
}
