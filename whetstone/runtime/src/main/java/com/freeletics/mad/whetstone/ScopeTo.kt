package com.freeletics.mad.whetstone

import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

/**
 * A Dagger [dagger.Component] or [dagger.Subcomponent] [Scope]. This is used to scope classes
 * to an entry on the backstack.
 *
 * In the case of [androidx.fragment.app.Fragment] this means that classes annotated with
 * [ScopeTo] will survive configuration changes and will be kept alive while the `Fragment`
 * is on the backstack. The life time of the scope would end when the `Fragment` is popped from the
 * backstack.
 */
@Scope
@Retention(RUNTIME)
@MustBeDocumented
annotation class ScopeTo(val marker: KClass<*>)
