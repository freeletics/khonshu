package com.freeletics.mad.whetstone.internal

import com.freeletics.mad.whetstone.NavEntryComponentGetter
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * Used when binding a [NavEntryComponentGetter] into a map. The value is the fully qualified name
 * of the scope class.
 *
 * The map can be used to easily implement an [android.content.Context.getSystemService] override
 * where a scope was passed into to retrieve the component for that scope.
 *
 * To be used in generated code.
 */
@MapKey
@InternalWhetstoneApi
public annotation class NavEntryComponentGetterKey(val value: String)
