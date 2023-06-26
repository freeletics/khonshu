package com.freeletics.mad.whetstone.internal

@InternalWhetstoneApi
public interface NavDestinationComponent {
    public val navEntryComponentGetters: @JvmSuppressWildcards Map<Class<*>, NavEntryComponentGetter>
}
