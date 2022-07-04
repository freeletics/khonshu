package com.freeletics.mad.whetstone.internal

@InternalWhetstoneApi
public interface DestinationComponent {
    public val navEntryComponentGetters: @JvmSuppressWildcards Map<Class<*>, NavEntryComponentGetter>
}
