package com.freeletics.mad.whetstone.internal

//TODO generate something that extends this and is contributed to destination component
@InternalWhetstoneApi
public interface DestinationComponent {
    public val navEntryComponentGetters: @JvmSuppressWildcards Map<Class<*>, NavEntryComponentGetter>
}
