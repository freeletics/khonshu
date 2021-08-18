package com.freeletics.mad.whetstone.internal

import androidx.compose.runtime.ProvidedValue
import dagger.Module
import dagger.multibindings.Multibinds

@Module
@InternalWhetstoneApi
interface ComposeProviderValueModule {
    @Multibinds
    fun bindProvidedValues(): Set<ProvidedValue<*>>
}
