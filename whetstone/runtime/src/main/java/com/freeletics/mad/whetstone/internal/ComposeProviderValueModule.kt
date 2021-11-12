package com.freeletics.mad.whetstone.internal

import androidx.compose.runtime.ProvidedValue
import dagger.Module
import dagger.multibindings.Multibinds

@Module
@InternalWhetstoneApi
public interface ComposeProviderValueModule {
    @Multibinds
    public fun bindProvidedValues(): Set<ProvidedValue<*>>
}
